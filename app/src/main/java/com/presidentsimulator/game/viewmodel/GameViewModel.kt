package com.presidentsimulator.game.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.presidentsimulator.game.data.ActiveCrisisState
import com.presidentsimulator.game.data.AgendaItem
import com.presidentsimulator.game.data.DailyChallenge
import com.presidentsimulator.game.data.DailyChallengeInfo
import com.presidentsimulator.game.data.DailyScore
import com.presidentsimulator.game.data.CabinetEngine
import com.presidentsimulator.game.data.CabinetPortfolio
import com.presidentsimulator.game.data.DisasterEngine
import com.presidentsimulator.game.data.ScenarioCatalog
import com.presidentsimulator.game.data.SpeechEngine
import com.presidentsimulator.game.data.SpeechTheme
import com.presidentsimulator.game.data.TermEngine
import com.presidentsimulator.game.data.LegacyLedger
import com.presidentsimulator.game.data.MandateEngine
import com.presidentsimulator.game.data.MandateGoal
import com.presidentsimulator.game.data.OppositionEngine
import com.presidentsimulator.game.data.PressDesk
import com.presidentsimulator.game.data.ResponseFocus
import com.presidentsimulator.game.data.DeploymentStatus
import com.presidentsimulator.game.data.EventChoice
import com.presidentsimulator.game.data.EventConsequence
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.data.EventRepository
import com.presidentsimulator.game.data.GameEvent
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.ResearchState
import com.presidentsimulator.game.data.InfrastructureType
import com.presidentsimulator.game.data.SectorInvestment
import com.presidentsimulator.game.data.awardSectorXp
import com.presidentsimulator.game.data.MissionType
import com.presidentsimulator.game.data.SaveLoadFeedback
import com.presidentsimulator.game.data.SecurityProtocol
import com.presidentsimulator.game.data.SocietyMinistry
import com.presidentsimulator.game.data.ResolutionType
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.data.TradeType
import com.presidentsimulator.game.data.TreatyType
import com.presidentsimulator.game.data.WarOutcome
import com.presidentsimulator.game.data.CovertMission
import com.presidentsimulator.game.data.Ideology
import com.presidentsimulator.game.data.LoanEngine
import com.presidentsimulator.game.data.MilitaryFacilityType
import com.presidentsimulator.game.data.StateReligion as SetupReligion
import com.presidentsimulator.game.data.TerritoryStatus
import com.presidentsimulator.game.data.VictoryEngine
import com.presidentsimulator.game.data.VictoryPath
import com.presidentsimulator.game.data.VictoryThresholds
import com.presidentsimulator.game.data.effectiveIdeology
import com.presidentsimulator.game.data.effectiveTitle
import com.presidentsimulator.game.data.pushNews
import com.presidentsimulator.game.data.MissionStatus
import com.presidentsimulator.game.data.PlayableNationCatalog
import com.presidentsimulator.game.data.TechCatalog
import com.presidentsimulator.game.data.StoryArcEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Owns the global [GameState], drives the monthly simulation tick,
 * and mediates all player actions including crisis resolution.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(GameState.initial())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _currentActiveEvent = MutableStateFlow<GameEvent?>(null)
    val currentActiveEvent: StateFlow<GameEvent?> = _currentActiveEvent.asStateFlow()

    private val _timeSpeedMode = MutableStateFlow(TimeSpeedMode.PAUSED)
    val timeSpeedMode: StateFlow<TimeSpeedMode> = _timeSpeedMode.asStateFlow()

    private val _saveLoadFeedback = MutableStateFlow(SaveLoadFeedback())
    val saveLoadFeedback: StateFlow<SaveLoadFeedback> = _saveLoadFeedback.asStateFlow()

    private val _missionResults = MutableStateFlow<List<CovertMission>>(emptyList())
    val missionResults: StateFlow<List<CovertMission>> = _missionResults.asStateFlow()

    private val _warOutcome = MutableStateFlow<WarOutcome?>(null)
    val warOutcome: StateFlow<WarOutcome?> = _warOutcome.asStateFlow()

    /** Whether a persisted save exists so the launch screen can offer Continue. */
    private val _hasSave = MutableStateFlow(false)
    val hasSave: StateFlow<Boolean> = _hasSave.asStateFlow()

    /** Today's Daily Seed Challenge facts (seed and nation are derived from the date). */
    private val _dailyInfo = MutableStateFlow<DailyChallengeInfo?>(null)
    val dailyInfo: StateFlow<DailyChallengeInfo?> = _dailyInfo.asStateFlow()

    /** Local daily-challenge leaderboard, best score first. */
    private val _dailyBoard = MutableStateFlow<List<DailyScore>>(emptyList())
    val dailyBoard: StateFlow<List<DailyScore>> = _dailyBoard.asStateFlow()

    /** How many past months the player can roll back right now. */
    private val _undoCount = MutableStateFlow(0)
    val undoCount: StateFlow<Int> = _undoCount.asStateFlow()

    /** One-shot feedback (formerly Toasts) rendered by the UI layer — see [UiEvent]. */
    private val _uiEvents = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvents: Flow<UiEvent> = _uiEvents.receiveAsFlow()

    /** Pre-tick snapshots of the most recent months, newest last. */
    private val undoStack = ArrayDeque<GameState>()

    /** True while showing the launch/new-game screen. */
    private val _showLaunchScreen = MutableStateFlow(true)
    val showLaunchScreen: StateFlow<Boolean> = _showLaunchScreen.asStateFlow()

    private var autoTickJob: Job? = null
    /** Serializes [advanceTimeTick] so the auto-ticker and End Turn button can never interleave. */
    private val tickMutex = Mutex()
    /** True while an async save is being written; new save requests coalesce into this. */
    private val saveInFlight = AtomicBoolean(false)
    /** Months simulated since the last auto-save; checkpoint every [AUTO_SAVE_TICK_INTERVAL]. */
    private var ticksSinceLastSave = 0
    /** Wall-clock time of the last write, used to throttle automatic checkpoints. */
    @Volatile
    private var lastSaveAtMs = 0L

    /**
     * Randomness source for the whole simulation. Rebuilt from [GameState.campaignSeed] by
     * [reseed] so a campaign can be replayed exactly; engines are pure state→state functions,
     * so swapping them is safe outside of a tick.
     */
    private var random: Random = Random.Default
    private var diplomacyEngine = DiplomacyEngine(random)
    private val productionLawEngine = ProductionLawEngine()
    private val analyticsEngine = AnalyticsSaveEngine()
    private var securityEngine = EspionageSecurityEngine(random)
    private val advancementEngine = AdvancementEngine()
    private var tradeEngine = TradeMarketEngine(random)
    private var governanceEngine = GovernanceEngine(random)
    private val demographicsEngine = DemographicsCampaignEngine()
    private var monthlyPipeline = buildPipeline()

    private fun buildPipeline() = MonthlySimulationPipeline(
        random = random,
        diplomacy = diplomacyEngine,
        productionLaw = productionLawEngine,
        analytics = analyticsEngine,
        security = securityEngine,
        advancement = advancementEngine,
        trade = tradeEngine,
        governance = governanceEngine,
        demographics = demographicsEngine,
        advanceDate = ::advanceSimulationDate,
        applyPopulationChange = ::applyPopulationPhysics,
    )

    /** Rebuilds every random-consuming engine from [seed]. Call only while time is paused. */
    private fun reseed(seed: Long) {
        random = Random(seed)
        diplomacyEngine = DiplomacyEngine(random)
        securityEngine = EspionageSecurityEngine(random)
        tradeEngine = TradeMarketEngine(random)
        governanceEngine = GovernanceEngine(random)
        monthlyPipeline = buildPipeline()
    }

    private val savePrefs = application.getSharedPreferences(
        AnalyticsSaveEngine.PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    private fun notifyUser(message: String, success: Boolean) {
        _uiEvents.trySend(UiEvent.Notice(message, success))
    }

    private fun applyActionWithFeedback(successMsg: String, failMsg: String, block: (GameState) -> GameState) {
        var success = false
        _state.update { old ->
            val new = block(old)
            // Structural comparison: an engine that copies on a failed action still
            // compares equal, so it can no longer report a false success.
            if (new != old) success = true
            new
        }
        notifyUser(if (success) successMsg else failMsg, success)
    }
    

    
    private val _showTutorial = MutableStateFlow(false)
    val showTutorial: StateFlow<Boolean> = _showTutorial.asStateFlow()

    /** One-shot request to open the world-market (shop) dialog, e.g. from an Economy tile. */
    private val _openWorldMarket = MutableStateFlow(false)
    val openWorldMarket: StateFlow<Boolean> = _openWorldMarket.asStateFlow()

    /** Opens the world-market shop dialog on the next composition. */
    fun openWorldMarket() {
        _openWorldMarket.value = true
    }

    fun dismissWorldMarket() {
        _openWorldMarket.value = false
    }

    fun triggerTutorial() {
        _showTutorial.value = true
    }
    
    fun dismissTutorial() {
        _showTutorial.value = false
    }

    init {
        if (!hasAutomatedSave()) _showTutorial.value = true
        _hasSave.value = hasAutomatedSave()
        refreshDailyChallenge()
        startActionSaveObserver()
    }

    /**
     * Debounced action save: when the state stops changing for
     * [ACTION_SAVE_DEBOUNCE_MS] (the player acted, then paused), persist it so a
     * mid-month process death cannot eat the action. While the ticker runs,
     * emissions never go quiet — [maybeAutoSave] checkpoints every N ticks instead.
     */
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private fun startActionSaveObserver() {
        viewModelScope.launch {
            state
                .debounce(ACTION_SAVE_DEBOUNCE_MS)
                .collect {
                    if (!_showLaunchScreen.value) saveGameProgress()
                }
        }
    }

    // ── Time engine ──────────────────────────────────────────────────────────

    /**
     * Advances one simulation month:
     * production/law physics, budget settlement, population dynamics,
     * geopolitics / war resolution, then a 15% roll for a macro event.
     */
    fun advanceTimeTick() {
        if (_currentActiveEvent.value != null) return
        if (_state.value.gameOver.isGameOver) return
        if (_missionResults.value.isNotEmpty()) return
        if (_warOutcome.value != null) return
        if (_state.value.agenda.needsBriefing) return
        if (_state.value.demographics.election.hasPendingNight) return

        if (!tickMutex.tryLock()) return
        try {
            val stateBeforeTick = _state.value
            val beforeMissions = _state.value.espionage.activeMissions.associateBy { it.id }

            _state.update { current ->
                if (current.gameOver.isGameOver) return@update current
                monthlyPipeline.advance(current)
            }

            val after = _state.value
            diplomacyEngine.consumeLastResolvedWar()?.let { outcome ->
                _warOutcome.value = outcome
                _state.update { LegacyLedger.recordWarOutcome(it, outcome.victory, outcome.targetName) }
                pauseTimeAdvance()
            }

            if (after.agenda.needsBriefing) {
                pauseTimeAdvance()
            }

            if (after.gameOver.isGameOver) {
                maybeRecordDailyScore()
                pauseTimeAdvance()
            } else {
                val newlyResolved = after.espionage.activeMissions.filter { mission ->
                    val prior = beforeMissions[mission.id]
                    prior?.status == MissionStatus.ACTIVE &&
                        (mission.status == MissionStatus.SUCCESS || mission.status == MissionStatus.FAILED)
                }

                if (newlyResolved.isNotEmpty()) {
                    _missionResults.value = _missionResults.value + newlyResolved
                    pauseTimeAdvance()
                }

                if (after.demographics.election.hasPendingNight) {
                    pauseTimeAdvance()
                }

                // Debounced auto-save (checkpoint every N ticks, async, coalesced).
                maybeAutoSave()
                maybeTriggerEvent()
                if (_currentActiveEvent.value != null) {
                    pauseTimeAdvance()
                    // Persist the blocking event as well as the state changes that created it.
                    saveGameProgress()
                }
            }

            // Keep the pre-tick snapshot so the player can roll the month back.
            if (_state.value !== stateBeforeTick) rememberForUndo(stateBeforeTick)
        } finally {
            tickMutex.unlock()
        }
    }

    fun dismissMissionResult() {
        val current = _missionResults.value
        if (current.isNotEmpty()) {
            _missionResults.value = current.drop(1)
        }
    }

    fun clearWarOutcome() {
        _warOutcome.value = null
    }

    // ── Domination: conquest, victory path, military industry, loans ────────

    /** Applies the player's post-war fate choice for the defeated nation. */
    fun resolveConquest(targetCountryId: String, status: TerritoryStatus) {
        _state.update { VictoryEngine.resolveConquest(it, targetCountryId, status) }
    }

    fun chooseVictoryPath(path: VictoryPath) {
        if (_state.value.victoryPath.chosenPath != null) return
        _state.update { current ->
            current
                .copy(victoryPath = current.victoryPath.copy(chosenPath = path))
                .pushNews(
                    when (path) {
                        VictoryPath.MILITARY_DOMINANCE ->
                            "${current.effectiveTitle()} vows to bend the world to ${current.playerNation.name}'s will"
                        VictoryPath.RELIGIOUS_DOMINANCE ->
                            "${current.society.stateReligion.displayName} enters the global stage with a mission"
                        VictoryPath.IDEOLOGICAL_DOMINANCE ->
                            "${current.playerNation.name} pledges to spread ${current.effectiveIdeology().displayName} worldwide"
                    },
                    tag = "DOMINION",
                )
        }
    }

    fun buildMilitaryFacility(type: MilitaryFacilityType, amount: Int) {
        if (_currentActiveEvent.value != null) return
        if (amount <= 0) return
        val current = _state.value
        val cost = type.unitCost * amount
        if (current.vitals.budget < cost) {
            notifyUser(
                "Insufficient Defense Budget: ${amount}x ${type.displayName} requires ${cost.toBudgetString()} (Treasury: ${current.vitals.budget.toBudgetString()})",
                success = false
            )
            return
        }
        val industry = current.militaryIndustry
        val updated = when (type) {
            MilitaryFacilityType.ARSENAL -> industry.copy(arsenals = industry.arsenals + amount)
            MilitaryFacilityType.AIRFIELD -> industry.copy(airfields = industry.airfields + amount)
            MilitaryFacilityType.SHIPYARD -> industry.copy(shipyards = industry.shipyards + amount)
        }
        _state.value = current
            .copy(
                vitals = current.vitals.copy(budget = current.vitals.budget - cost),
                militaryIndustry = updated,
            )
            .pushNews(
                "Construction: $amount ${type.displayName.lowercase()}(s) commissioned",
                tag = "MILITARY",
            )
        notifyUser(
            "Facility Commissioned: $amount ${type.displayName}(s) for ${cost.toBudgetString()}",
            success = true
        )
    }

    fun availableLoan(): Long = LoanEngine.availableLoan(_state.value)

    fun canTakeLoan(): Boolean = LoanEngine.canTakeLoan(_state.value)

    fun takeLoan(amount: Long) {
        if (_currentActiveEvent.value != null) return
        val available = availableLoan()
        if (amount > available || !canTakeLoan()) {
            notifyUser("Loan Application Rejected: Sovereign credit limit reached. Max available: ${available.toBudgetString()}", success = false)
            return
        }
        applyActionWithFeedback(
            "Loan Approved: +${amount.toBudgetString()} credited to National Treasury.",
            "Failed to disburse loan funds."
        ) {
            LoanEngine.takeLoan(it, amount)
        }
    }

    fun repayLoan(amount: Long) {
        if (_currentActiveEvent.value != null) return
        val current = _state.value
        if (current.vitals.budget < amount) {
            notifyUser("Insufficient Funds: Debt repayment requires ${amount.toBudgetString()} (Treasury: ${current.vitals.budget.toBudgetString()})", success = false)
            return
        }
        applyActionWithFeedback(
            "Public Debt Reduced by ${amount.toBudgetString()}.",
            "Failed to execute debt repayment."
        ) {
            LoanEngine.repayLoan(it, amount)
        }
    }

    fun setLeaderTitle(title: String) {
        _state.update { it.copy(setup = it.setup.copy(leaderTitle = title.take(24))) }
    }

    fun setSetupIdeology(ideologyId: String) {
        _state.update {
            it.copy(
                setup = it.setup.copy(ideologyId = ideologyId),
                legal = if (ideologyId.isBlank()) it.legal else it.legal.copy(
                    ideology = runCatching { Ideology.valueOf(ideologyId) }.getOrDefault(it.legal.ideology),
                ),
            )
        }
    }

    fun setSetupReligion(religionId: String) {
        _state.update {
            it.copy(
                setup = it.setup.copy(religionId = religionId),
                society = if (religionId.isBlank()) it.society else it.society.copy(
                    stateReligion = runCatching { SetupReligion.valueOf(religionId) }.getOrDefault(it.society.stateReligion),
                ),
            )
        }
    }

    /** Goal for the total-war scoreboard on the dashboard. */
    fun conquestGoalProgress(): Pair<Int, Int> =
        _state.value.territory.controlledCount to VictoryThresholds.NATIONS_TO_CONTROL


    // ── Global governance ────────────────────────────────────────────────────

    fun proposeResolution(type: ResolutionType, targetCountryId: String? = null) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Resolution Proposed.", "Failed: Insufficient Capital or Cooldown.") { governanceEngine.proposeResolution(it, type, targetCountryId) }
    }

    fun bribeCountryVote(countryId: String, voteFor: Boolean) {
        if (_currentActiveEvent.value != null) return
        _state.update { governanceEngine.bribeCountryVote(it, countryId, voteFor) }
    }

    fun formAlliance(name: String, invitees: List<String>) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Alliance Formed.", "Failed: Not enough influence or no invitees.") {
            governanceEngine.formAlliance(it, name, invitees)
        }
    }

    fun dissolveAlliance(allianceId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { governanceEngine.dissolveAlliance(it, allianceId) }
    }

    // ── Trade & markets ──────────────────────────────────────────────────────

    fun proposeTradeDeal(
        partnerCountryId: String,
        commodity: TradeCommodity,
        amount: Long,
        type: TradeType,
    ) {
        if (_currentActiveEvent.value != null) return
        var success = false
        _state.update { old -> 
            val new = tradeEngine.proposeTradeDeal(old, partnerCountryId, commodity, amount, type)
            if (new !== old) success = true
            new
        }
        if (success) notifyUser("Trade Deal Proposed.", true)
        else notifyUser("Trade Deal Failed (Check requirements).", false)
    }

    fun cancelTradeDeal(dealId: String) {
        if (_currentActiveEvent.value != null) return
        var success = false
        _state.update { old ->
            val new = tradeEngine.cancelTradeDeal(old, dealId)
            if (new !== old) success = true
            new
        }
        if (success) notifyUser("Trade Deal Cancelled.", true)
    }

    fun setTariffRate(rate: Float) {
        if (_currentActiveEvent.value != null) return
        _state.update { tradeEngine.setTariffRate(it, rate) }
    }

    fun setGoodsExportQuota(quota: Float) {
        if (_currentActiveEvent.value != null) return
        _state.update { tradeEngine.setGoodsExportQuota(it, quota) }
    }

    fun buyFromMarket(commodity: TradeCommodity, amount: Long = TradeMarketEngine.SPOT_BUNDLE_SIZE) {
        if (_currentActiveEvent.value != null) return
        _state.update { tradeEngine.buyFromMarket(it, commodity, amount) }
    }

    fun sellToMarket(commodity: TradeCommodity, amount: Long = TradeMarketEngine.SPOT_BUNDLE_SIZE) {
        if (_currentActiveEvent.value != null) return
        _state.update { tradeEngine.sellToMarket(it, commodity, amount) }
    }

    fun forecastTariffRevenue(rate: Float): Long =
        tradeEngine.forecastTariffRevenue(_state.value, rate)

    fun forecastTariffApprovalPenalty(rate: Float): Float =
        tradeEngine.forecastTariffApprovalPenalty(rate)

    fun negotiatedDealPrice(
        partnerCountryId: String,
        commodity: TradeCommodity,
        type: TradeType,
    ): Long {
        val rival = _state.value.diplomacy.rivalById(partnerCountryId) ?: return 0L
        val quote = _state.value.market.quote(commodity)
        return tradeEngine.negotiatedPrice(quote.currentPrice, rival.relationshipScore, type)
    }

    /** Neutral spot-market price for the HUD shop (no partner to negotiate with). */
    fun negotiatedDealPrice(commodity: TradeCommodity): Long {
        val quote = _state.value.market.quote(commodity)
        return tradeEngine.negotiatedPrice(quote.currentPrice, 0, TradeType.IMPORT)
    }

    /** National stockpile of a commodity, for the HUD shop's sell buttons. */
    fun stockOf(commodity: TradeCommodity): Long =
        TradeMarketEngine.stockOf(_state.value.production, commodity)

    // ── Science & society ────────────────────────────────────────────────────

    fun unlockTechnology(techId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { advancementEngine.unlockTechnology(it, techId) }
    }

    fun startResearch(techId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { advancementEngine.startResearch(it, techId) }
    }

    fun allocateExtraResearchFunding() {
        if (_currentActiveEvent.value != null) return
        val current = _state.value
        val cost = AdvancementEngine.EXTRA_RESEARCH_FUNDING_COST
        if (current.vitals.budget < cost) {
            notifyUser("Insufficient Funds: Research grant requires ${cost.toBudgetString()} (Treasury: ${current.vitals.budget.toBudgetString()})", success = false)
            return
        }
        applyActionWithFeedback(
            "Research Accelerated: Project time reduced by 1 month.",
            "Failed to allocate extra research funds."
        ) { advancementEngine.allocateExtraResearchFunding(it) }
    }

    fun canStartResearch(techId: String): Boolean =
        AdvancementEngine.canStartResearch(_state.value, techId)

    fun canAllocateExtraResearchFunding(): Boolean {
        val research = _state.value.research
        return research.activeTechId != null &&
            research.extraFundingTier < ResearchState.MAX_EXTRA_FUNDING_TIER &&
            _state.value.vitals.budget >= AdvancementEngine.EXTRA_RESEARCH_FUNDING_COST
    }

    fun adjustMinistryFunding(ministry: SocietyMinistry, newFundingLevel: Float) {
        if (_currentActiveEvent.value != null) return
        _state.update { advancementEngine.adjustMinistryFunding(it, ministry, newFundingLevel) }
    }

    fun changeStateReligion(religion: com.presidentsimulator.game.data.StateReligion) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("State Religion updated.", "That is already the state religion.") {
            advancementEngine.changeStateReligion(it, religion)
        }
    }

    fun buildUniversity() {
        if (_currentActiveEvent.value != null) return
        val current = _state.value
        val cost = AdvancementEngine.UNIVERSITY_COST
        if (current.vitals.budget < cost) {
            notifyUser("Insufficient Funds: University construction requires ${cost.toBudgetString()} (Treasury: ${current.vitals.budget.toBudgetString()})", success = false)
            return
        }
        applyActionWithFeedback(
            "National University Established (+${AdvancementEngine.SCIENCE_PER_UNIVERSITY} Science/mo).",
            "Failed to build university."
        ) { advancementEngine.buildUniversity(it) }
    }

    fun projectedSciencePerTick(): Long =
        advancementEngine.calculateScienceGenerated(_state.value)

    fun ministryForecast(ministry: SocietyMinistry): String =
        advancementEngine.forecastMinistryText(_state.value.society, ministry)

    fun canUnlockTechnology(techId: String): Boolean =
        AdvancementEngine.canUnlock(_state.value, techId)

    // ── Espionage & internal security ────────────────────────────────────────

    fun deploySpy(targetCountryId: String, missionType: MissionType) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback(
            "${missionType.displayName} launched.",
            "Failed: no free agents, budget, or intelligence.",
        ) { securityEngine.deploySpy(it, targetCountryId, missionType) }
    }

    fun fundInternalSecurity(amount: Int) {
        if (_currentActiveEvent.value != null) return
        _state.update { securityEngine.fundInternalSecurity(it, amount) }
    }

    fun toggleSecurityProtocol(protocol: SecurityProtocol) {
        if (_currentActiveEvent.value != null) return
        _state.update { securityEngine.toggleSecurityProtocol(it, protocol) }
    }

    fun recruitSpy() {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("New agent recruited.", "Insufficient budget to recruit an agent.") {
            securityEngine.recruitSpy(it)
        }
    }

    fun cancelCovertMission(missionId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { securityEngine.cancelCovertMission(it, missionId) }
    }

    fun estimateMissionSuccess(targetCountryId: String, missionType: MissionType): Float {
        val rival = _state.value.diplomacy.rivalById(targetCountryId) ?: return 0f
        return securityEngine.estimateSuccessProbability(
            state = _state.value,
            rivalMilitaryStrength = rival.militaryStrength,
            missionType = missionType,
        )
    }

    fun canDeploySpy(targetCountryId: String, missionType: MissionType): Boolean =
        EspionageSecurityEngine.canDeploy(_state.value, targetCountryId, missionType)

    // ── Analytics & save/restore ──────────────────────────────────────────────

    fun currentGdp(): Long = analyticsEngine.calculateGDP(_state.value)

    fun exportGameStateToJson(): String =
        analyticsEngine.exportGameStateToJson(_state.value)

    fun importGameStateFromJson(jsonString: String): Boolean {
        return try {
            val restored = analyticsEngine.importGameStateFromJson(jsonString)
            // Stop the ticker *before* swapping engines so a tick can't observe a half-swapped set.
            // No checkpoint here: the state in memory is about to be replaced, and saving it
            // would overwrite the automated save with the outgoing campaign.
            pauseTimeAdvance(checkpoint = false)
            reseed(restored.campaignSeed)
            _state.value = restored
            _currentActiveEvent.value = restored.crisis.pendingEventId
                ?.let { pendingId -> StoryArcEngine.nextEvent(restored) ?: EventRepository.byId(pendingId) }
            _missionResults.value = emptyList()
            _showLaunchScreen.value = false
            _hasSave.value = true
            clearUndoHistory()
            _saveLoadFeedback.value = analyticsEngine.feedbackForLoad(jsonString)
            true
        } catch (error: Exception) {
            _saveLoadFeedback.value =
                analyticsEngine.feedbackForLoadFailure(error.message ?: "invalid payload")
            false
        }
    }

    fun saveGameProgress() {
        // Coalesce: if a save is already running or queued, the state it captures is
        // guaranteed to be at least as new as what this request saw.
        if (!saveInFlight.compareAndSet(false, true)) return
        val stateSnapshot = _state.value
        ticksSinceLastSave = 0
        lastSaveAtMs = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = analyticsEngine.exportGameStateToJson(stateSnapshot)
                savePrefs.edit()
                    .putString(AnalyticsSaveEngine.KEY_AUTOMATED_SAVE, payload)
                    .putInt(AnalyticsSaveEngine.KEY_LAST_PAYLOAD_BYTES, payload.length)
                    .apply()
                _saveLoadFeedback.value = analyticsEngine.feedbackForSave(payload)
                _hasSave.value = true
            } finally {
                saveInFlight.set(false)
            }
        }
    }

    fun loadLastAutomatedSave() {
        val payload = savePrefs.getString(AnalyticsSaveEngine.KEY_AUTOMATED_SAVE, null)
        if (payload.isNullOrBlank()) {
            _saveLoadFeedback.value = analyticsEngine.feedbackForMissingSave()
            return
        }
        importGameStateFromJson(payload)
    }

    fun hasAutomatedSave(): Boolean =
        !savePrefs.getString(AnalyticsSaveEngine.KEY_AUTOMATED_SAVE, null).isNullOrBlank()

    private val _saveSlots = MutableStateFlow<List<SaveSlotInfo>>(emptyList())
    val saveSlots: StateFlow<List<SaveSlotInfo>> = _saveSlots.asStateFlow()

    /** Refreshes slot metadata off the main thread; call before showing the launch screen. */
    fun refreshSaveSlots() {
        viewModelScope.launch(Dispatchers.IO) {
            _saveSlots.value = computeSaveSlots()
        }
    }

    private fun computeSaveSlots(): List<SaveSlotInfo> =
        (1..AnalyticsSaveEngine.SLOT_COUNT).map { slot ->
            val payload = savePrefs.getString(AnalyticsSaveEngine.slotKey(slot), null)
            if (payload.isNullOrBlank()) {
                SaveSlotInfo(slotIndex = slot, occupied = false, label = "Slot $slot — Empty")
            } else {
                try {
                    val snap = analyticsEngine.importGameStateFromJson(payload)
                    SaveSlotInfo(
                        slotIndex = slot,
                        occupied = true,
                        year = snap.year,
                        month = snap.month,
                        label = "Slot $slot — ${snap.month}/${snap.year}",
                    )
                } catch (_: Exception) {
                    SaveSlotInfo(slotIndex = slot, occupied = true, label = "Slot $slot — Corrupt?")
                }
            }
        }

    fun saveToSlot(slot: Int) {
        if (slot !in 1..AnalyticsSaveEngine.SLOT_COUNT) return
        val payload = analyticsEngine.exportGameStateToJson(_state.value)
        savePrefs.edit()
            .putString(AnalyticsSaveEngine.slotKey(slot), payload)
            .putInt(AnalyticsSaveEngine.KEY_LAST_PAYLOAD_BYTES, payload.length)
            .apply()
        _saveLoadFeedback.value = SaveLoadFeedback(
            message = "Saved to slot $slot (${AnalyticsSaveEngine.formatBytes(payload.length)}).",
            payloadBytes = payload.length,
            success = true,
        )
        _hasSave.value = true
    }

    fun loadFromSlot(slot: Int) {
        if (slot !in 1..AnalyticsSaveEngine.SLOT_COUNT) return
        val payload = savePrefs.getString(AnalyticsSaveEngine.slotKey(slot), null)
        if (payload.isNullOrBlank()) {
            _saveLoadFeedback.value = SaveLoadFeedback(
                message = "Slot $slot is empty.",
                payloadBytes = 0,
                success = false,
            )
            return
        }
        if (importGameStateFromJson(payload)) {
            _saveLoadFeedback.value = SaveLoadFeedback(
                message = "Loaded slot $slot (${AnalyticsSaveEngine.formatBytes(payload.length)}).",
                payloadBytes = payload.length,
                success = true,
            )
        }
    }

    fun acknowledgeBriefing() {
        _state.update { current ->
            if (!current.agenda.needsBriefing) return@update current
            current.copy(
                agenda = current.agenda.copy(
                    acknowledgedMonthKey = current.agenda.monthKey,
                ),
            )
        }
    }

    fun actOnAgendaItem(item: AgendaItem) {
        _state.update { current ->
            val acted = (current.agenda.actedItemIds + item.id).distinct().takeLast(24)
            current.copy(
                agenda = current.agenda.copy(
                    actedItemIds = acted,
                    acknowledgedMonthKey = current.agenda.monthKey,
                ),
            )
        }
    }

    fun confirmElectionNight() {
        _state.update { demographicsEngine.confirmElectionNight(it) }
        if (_state.value.gameOver.isGameOver) {
            maybeRecordDailyScore()
            pauseTimeAdvance()
        }
    }

    fun spinPressHeadline(headlineId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { PressDesk.spinHeadline(it, headlineId) }
    }

    fun suppressPressHeadline(headlineId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { PressDesk.suppressHeadline(it, headlineId) }
    }

    fun appointCabinetCandidate(candidateId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { CabinetEngine.appointCandidate(it, candidateId) }
    }

    fun fireCabinetMinister(portfolio: CabinetPortfolio) {
        if (_currentActiveEvent.value != null) return
        _state.update { CabinetEngine.fireMinister(it, portfolio) }
    }

    fun reshuffleCabinetCandidates() {
        if (_currentActiveEvent.value != null) return
        _state.update { CabinetEngine.reshuffleCandidates(it) }
    }

    fun negotiateWithOpposition() {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Negotiations held. Stability changed.", "Failed: Cooldown active or insufficient funds.") {
            OppositionEngine.negotiate(it)
        }
    }

    fun smearOpposition() {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Smear campaign initiated.", "Failed: Cooldown active or insufficient funds.") {
            OppositionEngine.smear(it, random)
        }
    }

    fun concedeToOpposition() {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Concessions made to opposition.", "Failed: Cooldown active or insufficient funds.") {
            OppositionEngine.concedePlatform(it)
        }
    }

    fun makeMandateCommitment(goal: MandateGoal) {
        if (_currentActiveEvent.value != null || _state.value.gameOver.isGameOver) return
        _state.update { MandateEngine.makeCommitment(it, goal) }
    }

    fun allocateDisasterResponse(focus: ResponseFocus) {
        if (_currentActiveEvent.value != null) return
        _state.update { DisasterEngine.allocateResponse(it, focus) }
    }

    fun deliverSpeech(theme: SpeechTheme) {
        if (_currentActiveEvent.value != null) return
        _state.update { SpeechEngine.deliverSpeech(it, theme) }
    }

    fun holdPressConference() {
        if (_currentActiveEvent.value != null) return
        _state.update { SpeechEngine.holdPressConference(it) }
    }

    fun nameSuccessor(name: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { TermEngine.nameSuccessor(it, name) }
    }

    fun extendTermLimit() {
        if (_currentActiveEvent.value != null) return
        _state.update { TermEngine.extendTermLimit(it) }
    }

    fun continueGame() {
        if (_hasSave.value) {
            loadLastAutomatedSave()
            _showLaunchScreen.value = false
        }
    }

    fun playableNations(): List<PlayableNationCatalog.NationDefinition> =
        PlayableNationCatalog.all()

    fun startNewGame(
        countryId: String = "us",
        scenarioId: String = "standard",
        challengeId: String = "standard",
        seedOverride: Long? = null,
        dailyEpochDay: Long = 0L,
    ) {
        // A fresh, non-zero seed makes every campaign of this setup replay identically.
        val seed = seedOverride ?: Random.Default.nextLong(1, Long.MAX_VALUE)
        val applied = ScenarioCatalog.apply(GameState.initial(countryId), scenarioId, challengeId = challengeId)
        val seeded = applied.copy(
            campaignSeed = seed,
            scenario = applied.scenario.copy(dailyChallengeEpochDay = dailyEpochDay),
        )
        reseed(seed)
        _state.value = seeded
        _currentActiveEvent.value = null
        _missionResults.value = emptyList()
        clearUndoHistory()
        pauseTimeAdvance()
        _showLaunchScreen.value = false
        saveGameProgress()
    }

    fun returnToLaunch() {
        pauseTimeAdvance()
        _currentActiveEvent.value = null
        _missionResults.value = emptyList()
        clearUndoHistory()
        _hasSave.value = hasAutomatedSave()
        _showLaunchScreen.value = true
        refreshDailyChallenge()
    }

    // ── Daily Seed Challenge ─────────────────────────────────────────────────

    /** Starts today's shared-seed, shared-nation run (same for every player). */
    fun startDailyChallenge() {
        val day = LocalDate.now().toEpochDay()
        startNewGame(
            countryId = DailyChallenge.nationIdFor(day),
            scenarioId = "standard",
            challengeId = "standard",
            seedOverride = DailyChallenge.seedFor(day),
            dailyEpochDay = day,
        )
    }

    private fun refreshDailyChallenge() {
        val day = LocalDate.now().toEpochDay()
        val nationId = DailyChallenge.nationIdFor(day)
        val nation = PlayableNationCatalog.byId(nationId)
        val board = DailyChallenge.decode(
            savePrefs.getString(AnalyticsSaveEngine.KEY_DAILY_SCORES, null),
        )
        _dailyBoard.value = board
        _dailyInfo.value = DailyChallengeInfo(
            epochDay = day,
            nationId = nationId,
            nationName = nation?.name ?: nationId,
            flagEmoji = nation?.flagEmoji ?: "",
            bestScore = board.filter { it.epochDay == day }.maxOfOrNull { it.score } ?: 0,
        )
    }

    /** Persists today's daily result at most once, keeping the best attempt per day. */
    private fun maybeRecordDailyScore() {
        val current = _state.value
        if (!current.gameOver.isGameOver) return
        val day = current.scenario.dailyChallengeEpochDay
        if (day <= 0L) return
        val board = DailyChallenge.decode(
            savePrefs.getString(AnalyticsSaveEngine.KEY_DAILY_SCORES, null),
        )
        val entry = DailyScore(
            epochDay = day,
            nationId = current.playerNation.id,
            score = LegacyLedger.campaignScore(current),
            victory = current.gameOver.isVictory,
        )
        if (board.any { it.epochDay == day && it.score >= entry.score }) return
        val updated = DailyChallenge.record(board, entry)
        savePrefs.edit()
            .putString(AnalyticsSaveEngine.KEY_DAILY_SCORES, DailyChallenge.encode(updated))
            .apply()
        refreshDailyChallenge()
    }

    // ── Production & law actions ──────────────────────────────────────────────

    fun enactLaw(lawId: String) {
        if (_currentActiveEvent.value != null) return
        val law = com.presidentsimulator.game.data.LawCatalog.byId(lawId) ?: return
        val legal = _state.value.legal
        // Distinguish the real reason instead of always blaming the budget.
        if (legal.isActive(lawId)) {
            notifyUser("${law.name} is already in force.", false)
            return
        }
        if (legal.pendingLaws.any { it.lawId == lawId }) {
            notifyUser("${law.name} is already before parliament.", false)
            return
        }
        applyActionWithFeedback(
            "Sent to parliament: ${law.name}.",
            "Failed: needs ${law.activationCost.toBudgetString()}.",
        ) { productionLawEngine.enactLaw(it, lawId) }
    }

    fun negotiatePendingLaw(lawId: String) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback(
            "Compromise bought — support increased.",
            "Failed: needs ${ProductionLawEngine.BILL_COMPROMISE_COST.toBudgetString()} or compromise limit reached.",
        ) { productionLawEngine.negotiatePendingLaw(it, lawId) }
    }

    fun repealLaw(lawId: String) {
        if (_currentActiveEvent.value != null) return
        val law = com.presidentsimulator.game.data.LawCatalog.byId(lawId) ?: return
        if (_state.value.legal.pendingLaws.any { it.lawId == lawId }) {
            notifyUser("${law.name} already has a bill before parliament.", false)
            return
        }
        applyActionWithFeedback(
            "Repeal bill filed: ${law.name}.",
            "Failed: ${law.name} is not in force.",
        ) { productionLawEngine.repealLaw(it, lawId) }
    }

    fun buildPowerPlant(amount: Int) {
        if (amount <= 0) return
        val current = _state.value
        val cost = InfrastructureType.POWER_PLANT.unitCost * amount
        if (current.vitals.budget < cost) {
            notifyUser("Insufficient Funds: Power plant requires ${cost.toBudgetString()} (Treasury: ${current.vitals.budget.toBudgetString()})", success = false)
            return
        }
        applyActionWithFeedback(
            "Power Plant Online: +${amount * 120}MW generated for ${cost.toBudgetString()}.",
            "Failed: Insufficient budget (${cost.toBudgetString()})."
        ) { productionLawEngine.buildPowerPlant(it, amount) }
    }

    fun buildMine(amount: Int) {
        if (amount <= 0) return
        val current = _state.value
        val cost = InfrastructureType.MINE.unitCost * amount
        if (current.vitals.budget < cost) {
            notifyUser("Insufficient Funds: Mine development requires ${cost.toBudgetString()} (Treasury: ${current.vitals.budget.toBudgetString()})", success = false)
            return
        }
        applyActionWithFeedback(
            "Extraction Mine Online: +$amount mine(s) for ${cost.toBudgetString()}.",
            "Failed: Insufficient budget (${cost.toBudgetString()})."
        ) { productionLawEngine.buildMine(it, amount) }
    }

    fun canEnactLaw(lawId: String): Boolean {
        val law = com.presidentsimulator.game.data.LawCatalog.byId(lawId) ?: return false
        return ProductionLawEngine.canEnact(_state.value, law)
    }

    fun parliamentSupportFor(lawId: String): Float {
        val law = com.presidentsimulator.game.data.LawCatalog.byId(lawId) ?: return 0f
        return com.presidentsimulator.game.data.ParliamentarySupport.score(_state.value, law)
    }

    fun campaignCooldownMonths(action: CampaignAction): Int =
        demographicsEngine.campaignCooldownMonths(_state.value, action)

    fun isElectionSeason(): Boolean =
        demographicsEngine.isElectionSeason(_state.value)

    // ── Military & diplomacy actions ─────────────────────────────────────────

    fun declareWar(targetCountryId: String, warGoal: com.presidentsimulator.game.data.WarGoal = com.presidentsimulator.game.data.WarGoal.REPARATIONS) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("War Declared!", "Failed: Invalid Target or Missing Requirements.") { diplomacyEngine.declareWar(it, targetCountryId, warGoal) }
    }

    fun claimWarSettlement() {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.claimWarSettlement(it) }
    }

    fun negotiateTreaty(targetCountryId: String, type: TreatyType) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Treaty Signed.", "Failed: Insufficient Capital/Relations.") { diplomacyEngine.negotiateTreaty(it, targetCountryId, type) }
    }

    fun breakTreaty(targetCountryId: String, type: TreatyType) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Treaty Broken!", "Failed: That treaty is not in force.") { diplomacyEngine.breakTreaty(it, targetCountryId, type) }
    }

    fun setDefcon(level: Int) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.setDefcon(it, level) }
    }

    fun setIdeology(ideology: com.presidentsimulator.game.data.Ideology) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Ideology Shifted.", "Failed: Insufficient Budget ($3B required).") { productionLawEngine.setIdeology(it, ideology) }
    }

    fun cancelPendingLaw(lawId: String) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Bill withdrawn.", "Failed: no such bill before parliament.") {
            productionLawEngine.cancelPendingLaw(it, lawId)
        }
    }

    fun rushPendingLaw(lawId: String) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback(
            "Bill fast-tracked and resolved.",
            "Failed: needs ${ProductionLawEngine.RUSH_LAW_COST.toBudgetString()}.",
        ) { productionLawEngine.rushPendingLaw(it, lawId) }
    }

    fun sendForeignAid(targetCountryId: String) {
        if (_currentActiveEvent.value != null) return
        var success = false
        _state.update { old ->
            val new = diplomacyEngine.sendForeignAid(old, targetCountryId)
            if (new !== old) success = true
            new
        }
        if (success) notifyUser("Foreign Aid Sent.", true)
        else notifyUser("Action Failed (Insufficient Budget/Cooldown).", false)
    }

    fun canSendForeignAid(targetCountryId: String): Boolean =
        DiplomacyEngine.canSendForeignAid(_state.value, targetCountryId)

    fun conductStateVisit(targetCountryId: String) {
        if (_currentActiveEvent.value != null) return
        var success = false
        _state.update { old ->
            val new = diplomacyEngine.conductStateVisit(old, targetCountryId)
            if (new !== old) success = true
            new
        }
        if (success) notifyUser("State Visit Completed.", true)
        else notifyUser("Action Failed (Insufficient Capital/Cooldown).", false)
    }

    fun canConductStateVisit(targetCountryId: String): Boolean =
        DiplomacyEngine.canConductStateVisit(_state.value, targetCountryId)

    fun runCampaignAction(action: CampaignAction) {
        if (_currentActiveEvent.value != null) return
        val current = _state.value
        val cooldown = campaignCooldownMonths(action)
        if (cooldown > 0) {
            notifyUser("Campaign Cooldown Active: Available in $cooldown month(s).", success = false)
            return
        }
        val seasonPremium = if (isElectionSeason()) 1.25f else 1f
        val totalCost = (action.cost * seasonPremium).toLong()
        if (current.vitals.budget < totalCost) {
            notifyUser(
                "Insufficient Campaign Budget: ${action.displayName} requires ${totalCost.toBudgetString()} (Treasury: ${current.vitals.budget.toBudgetString()})",
                success = false
            )
            return
        }
        applyActionWithFeedback(
            "Campaign Event Held: ${action.displayName} (+${action.boost.toInt()}% approval boost).",
            "Campaign action failed."
        ) { demographicsEngine.runCampaignAction(it, action) }
    }

    // ---------------------------------------------------------
    // PILLAR 2: 12-SECTOR INDUSTRIAL MATRIX
    // ---------------------------------------------------------
    fun expandIndustrialPlant(sectorType: com.presidentsimulator.game.data.IndustrialSectorType, subPlantName: String) {
        val current = _state.value
        val sector = current.industrialMatrix.sectors[sectorType] ?: return
        val plant = sector.subPlants.find { it.name == subPlantName } ?: return
        val cost = plant.buildCostMillion * 1_000_000L

        if (current.vitals.budget < cost) {
            notifyUser("Insufficient Funds: Building ${plant.name} requires ${cost.toBudgetString()} (Treasury: ${current.vitals.budget.toBudgetString()})", success = false)
            return
        }

        _state.update { old ->
            val oldSector = old.industrialMatrix.sectors[sectorType] ?: return@update old
            val updatedSubPlants = oldSector.subPlants.map {
                if (it.name == subPlantName) it.copy(count = it.count + 1) else it
            }
            val updatedSector = oldSector.copy(
                facilitiesCount = oldSector.facilitiesCount + 1,
                workers = oldSector.workers + (plant.outputUnitBonus * 80L),
                monthlyOutputMillion = oldSector.monthlyOutputMillion + plant.outputUnitBonus,
                subPlants = updatedSubPlants,
            )
            old.copy(
                vitals = old.vitals.copy(budget = old.vitals.budget - cost),
                industrialMatrix = old.industrialMatrix.copy(
                    sectors = old.industrialMatrix.sectors + (sectorType to updatedSector),
                ),
            )
        }
        notifyUser("Construction Completed: ${plant.name} operational (+${plant.outputUnitBonus}M output/mo).", success = true)
    }

    fun modernizeIndustrialSector(sectorType: com.presidentsimulator.game.data.IndustrialSectorType) {
        val current = _state.value
        val sector = current.industrialMatrix.sectors[sectorType] ?: return
        val cost = sector.level * 4_500_000_000L

        if (current.vitals.budget < cost) {
            notifyUser("Insufficient Funds: Modernizing ${sector.type.title} requires ${cost.toBudgetString()}", success = false)
            return
        }

        _state.update { old ->
            val oldSector = old.industrialMatrix.sectors[sectorType] ?: return@update old
            val updatedSector = oldSector.copy(
                level = oldSector.level + 1,
                efficiencyMultiplier = oldSector.efficiencyMultiplier + 0.15f,
            )
            old.copy(
                vitals = old.vitals.copy(budget = old.vitals.budget - cost),
                industrialMatrix = old.industrialMatrix.copy(
                    sectors = old.industrialMatrix.sectors + (sectorType to updatedSector),
                ),
            )
        }
        notifyUser("${sector.type.title} Modernized: Efficiency increased to +${((sector.efficiencyMultiplier + 0.15f) * 100).toInt()}%.", success = true)
    }

    fun resolveIndustrialStrike(sectorType: com.presidentsimulator.game.data.IndustrialSectorType) {
        val current = _state.value
        val cost = 500_000_000L
        if (current.vitals.budget < cost) {
            notifyUser("Insufficient Funds: Labor settlement requires ${cost.toBudgetString()}", success = false)
            return
        }

        _state.update { old ->
            val oldSector = old.industrialMatrix.sectors[sectorType] ?: return@update old
            val updatedSector = oldSector.copy(isUnderStrike = false)
            old.copy(
                vitals = old.vitals.copy(
                    budget = old.vitals.budget - cost,
                    approval = (old.vitals.approval + 4f).coerceAtMost(100f),
                ),
                industrialMatrix = old.industrialMatrix.copy(
                    sectors = old.industrialMatrix.sectors + (sectorType to updatedSector),
                ),
            )
        }
        notifyUser("Labor Settlement Reached: Strikers returned to work in ${sectorType.title}.", success = true)
    }

    // ---------------------------------------------------------
    // PILLAR 3: CRISIS HOTLINE ENGINE
    // ---------------------------------------------------------
    fun resolveActiveCrisis(crisisId: String, optionId: String) {
        val current = _state.value
        val crisis = current.crises.activeCrises.find { it.id == crisisId } ?: return
        val option = crisis.options.find { it.id == optionId } ?: return

        if (current.vitals.budget < option.costBudget) {
            notifyUser("Insufficient Emergency Reserves: ${option.label} requires ${option.costBudget.toBudgetString()}", success = false)
            return
        }

        _state.update { old ->
            val remainingCrises = old.crises.activeCrises.filterNot { it.id == crisisId }
            old.copy(
                vitals = old.vitals.copy(
                    budget = old.vitals.budget - option.costBudget,
                    approval = (old.vitals.approval + 5f).coerceAtMost(100f),
                ),
                crises = old.crises.copy(
                    activeCrises = remainingCrises,
                    hotlineRinging = remainingCrises.isNotEmpty(),
                    totalCrisesResolved = old.crises.totalCrisesResolved + 1,
                ),
            )
        }
        notifyUser("Executive Directive Enacted: ${option.label} resolved ${crisis.type.displayName}.", success = true)
    }

    // ---------------------------------------------------------
    // PILLAR 4: PENTAGON WAR ROOM & NUCLEAR FOOTBALL
    // ---------------------------------------------------------
    fun assignMilitaryDoctrine(doctrine: com.presidentsimulator.game.data.MilitaryDoctrine) {
        _state.update { old ->
            old.copy(
                warRoom = old.warRoom.copy(selectedDoctrine = doctrine),
            )
        }
        notifyUser("General Staff Doctrine Adopted: ${doctrine.title}.", success = true)
    }

    fun setDefconLevel(level: Int) {
        _state.update { old ->
            old.copy(
                warRoom = old.warRoom.copy(
                    nuclearSilo = old.warRoom.nuclearSilo.copy(defconLevel = level),
                ),
            )
        }
        notifyUser("Strategic Alert Level Changed: DEFCON $level Active.", success = true)
    }

    fun toggleNuclearLaunchKeys() {
        val isArmed = !_state.value.warRoom.nuclearSilo.launchKeysInserted
        _state.update { old ->
            old.copy(
                warRoom = old.warRoom.copy(
                    nuclearSilo = old.warRoom.nuclearSilo.copy(
                        launchKeysInserted = isArmed,
                        twoPersonRuleConfirmed = isArmed,
                    ),
                ),
            )
        }
        if (isArmed) {
            notifyUser("WARNING: Nuclear launch circuits armed with presidential keycodes.", success = false)
        } else {
            notifyUser("Nuclear launch circuits disarmed and placed in safe state.", success = true)
        }
    }

    fun buildNuclearSilo() {
        val current = _state.value
        val cost = 10_000_000_000L
        if (current.vitals.budget < cost) {
            notifyUser("Insufficient Defense Funds: Hardened silo requires ${cost.toBudgetString()}", success = false)
            return
        }

        _state.update { old ->
            val silo = old.warRoom.nuclearSilo
            old.copy(
                vitals = old.vitals.copy(budget = old.vitals.budget - cost),
                warRoom = old.warRoom.copy(
                    nuclearSilo = silo.copy(
                        activeSilos = silo.activeSilos + 1,
                        nuclearWarheads = silo.nuclearWarheads + 4,
                        deterrenceIndex = (silo.deterrenceIndex + 3).coerceAtMost(100),
                    ),
                ),
            )
        }
        notifyUser("Strategic Construction Complete: New ICBM Silo operational (+4 warheads).", success = true)
    }

    // ---------------------------------------------------------
    // PILLAR 5: SOVEREIGN MINISTRIES (Police, Sports, Ecology)
    // ---------------------------------------------------------
    fun procurePoliceEquipment(equipmentName: String) {
        val current = _state.value
        val cost = when {
            equipmentName.contains("Cruiser", ignoreCase = true) -> 800_000_000L
            equipmentName.contains("SWAT", ignoreCase = true) -> 1_500_000_000L
            equipmentName.contains("Handguns", ignoreCase = true) -> 400_000_000L
            equipmentName.contains("Batons", ignoreCase = true) -> 200_000_000L
            equipmentName.contains("Prison", ignoreCase = true) -> 3_000_000_000L
            else -> 1_800_000_000L
        }

        if (current.vitals.budget < cost) {
            notifyUser("Insufficient Police Budget: $equipmentName requires ${cost.toBudgetString()}", success = false)
            return
        }

        _state.update { old ->
            val police = old.sovereignMinistries.police
            val updatedPolice = when {
                equipmentName.contains("Cruiser", ignoreCase = true) -> police.copy(
                    patrolCars = police.patrolCars + 150,
                    hooliganismRatePercent = (police.hooliganismRatePercent - 1.5f).coerceAtLeast(3f),
                )
                equipmentName.contains("SWAT", ignoreCase = true) -> police.copy(
                    swatArmoredVehicles = police.swatArmoredVehicles + 20,
                    violentCrimeRatePercent = (police.violentCrimeRatePercent - 1.8f).coerceAtLeast(2f),
                )
                equipmentName.contains("Handguns", ignoreCase = true) -> police.copy(
                    servicePistols = police.servicePistols + 500,
                    violentCrimeRatePercent = (police.violentCrimeRatePercent - 0.8f).coerceAtLeast(2f),
                )
                equipmentName.contains("Batons", ignoreCase = true) -> police.copy(
                    stunBatons = police.stunBatons + 1_000,
                    hooliganismRatePercent = (police.hooliganismRatePercent - 1.2f).coerceAtLeast(3f),
                )
                equipmentName.contains("Prison", ignoreCase = true) -> police.copy(
                    highSecurityPrisons = police.highSecurityPrisons + 1,
                    violentCrimeRatePercent = (police.violentCrimeRatePercent - 2.5f).coerceAtLeast(2f),
                )
                else -> police.copy(
                    prosecutorsOffices = police.prosecutorsOffices + 2,
                    corruptionIndexPercent = (police.corruptionIndexPercent - 3.0f).coerceAtLeast(5f),
                )
            }

            old.copy(
                vitals = old.vitals.copy(budget = old.vitals.budget - cost),
                sovereignMinistries = old.sovereignMinistries.copy(police = updatedPolice),
            )
        }
        notifyUser("Public Safety Procurement: $equipmentName deployed to active service.", success = true)
    }

    fun buildSportsArena(arenaName: String) {
        val current = _state.value
        val cost = when {
            arenaName.contains("Football", ignoreCase = true) -> 4_000_000_000L
            arenaName.contains("Hockey", ignoreCase = true) -> 3_500_000_000L
            arenaName.contains("Basketball", ignoreCase = true) -> 2_800_000_000L
            arenaName.contains("Velodrome", ignoreCase = true) -> 1_900_000_000L
            arenaName.contains("Aquatic", ignoreCase = true) -> 2_400_000_000L
            else -> 2_100_000_000L
        }

        if (current.vitals.budget < cost) {
            notifyUser("Insufficient Sports Budget: $arenaName requires ${cost.toBudgetString()}", success = false)
            return
        }

        _state.update { old ->
            val sports = old.sovereignMinistries.sports
            val updatedSports = when {
                arenaName.contains("Football", ignoreCase = true) -> sports.copy(footballStadiums = sports.footballStadiums + 1, publicHappinessBonus = sports.publicHappinessBonus + 1.2f)
                arenaName.contains("Hockey", ignoreCase = true) -> sports.copy(iceArenas = sports.iceArenas + 1, publicHappinessBonus = sports.publicHappinessBonus + 1.0f)
                arenaName.contains("Basketball", ignoreCase = true) -> sports.copy(basketballArenas = sports.basketballArenas + 1, publicHappinessBonus = sports.publicHappinessBonus + 0.8f)
                arenaName.contains("Velodrome", ignoreCase = true) -> sports.copy(velodromes = sports.velodromes + 1, publicHealthBonus = sports.publicHealthBonus + 0.6f)
                arenaName.contains("Aquatic", ignoreCase = true) -> sports.copy(olympicPools = sports.olympicPools + 1, publicHealthBonus = sports.publicHealthBonus + 0.9f)
                else -> sports.copy(racecourses = sports.racecourses + 1, publicHappinessBonus = sports.publicHappinessBonus + 0.7f)
            }

            old.copy(
                vitals = old.vitals.copy(
                    budget = old.vitals.budget - cost,
                    approval = (old.vitals.approval + 1.5f).coerceAtMost(100f),
                ),
                sovereignMinistries = old.sovereignMinistries.copy(sports = updatedSports),
            )
        }
        notifyUser("Sports Complex Inaugurated: $arenaName open to national athletes.", success = true)
    }

    fun buildEcologyFacility(facilityName: String) {
        val current = _state.value
        val cost = when {
            facilityName.contains("Water", ignoreCase = true) -> 3_000_000_000L
            facilityName.contains("Recycling", ignoreCase = true) -> 2_200_000_000L
            else -> 1_500_000_000L
        }

        if (current.vitals.budget < cost) {
            notifyUser("Insufficient Green Budget: $facilityName requires ${cost.toBudgetString()}", success = false)
            return
        }

        _state.update { old ->
            val eco = old.sovereignMinistries.ecology
            val updatedEco = when {
                facilityName.contains("Water", ignoreCase = true) -> eco.copy(
                    waterPurificationPlants = eco.waterPurificationPlants + 1,
                    publicHealthIndex = (eco.publicHealthIndex + 2.0f).coerceAtMost(100f),
                )
                facilityName.contains("Recycling", ignoreCase = true) -> eco.copy(
                    wasteRecyclingCenters = eco.wasteRecyclingCenters + 1,
                    airQualityIndex = (eco.airQualityIndex - 4).coerceAtLeast(15),
                )
                else -> eco.copy(
                    protectedNatureReserves = eco.protectedNatureReserves + 1,
                    airQualityIndex = (eco.airQualityIndex - 5).coerceAtLeast(15),
                )
            }

            old.copy(
                vitals = old.vitals.copy(budget = old.vitals.budget - cost),
                sovereignMinistries = old.sovereignMinistries.copy(ecology = updatedEco),
            )
        }
        notifyUser("Environmental Mandate Executed: $facilityName operational.", success = true)
    }

    // ---------------------------------------------------------
    // PILLAR 6: THE OVAL OFFICE RESOLUTE DESK & EXECUTIVE ORDERS
    // ---------------------------------------------------------
    fun toggleExecutiveOrder(orderType: com.presidentsimulator.game.data.ExecutiveOrderType) {
        val current = _state.value
        val decree = current.ovalOffice.decrees[orderType] ?: return
        val willEnact = !decree.isEnacted

        _state.update { old ->
            val updatedDecrees = old.ovalOffice.decrees + (orderType to decree.copy(
                isEnacted = willEnact,
                enactedYear = if (willEnact) old.year else null,
            ))
            val approvalDelta = if (willEnact) orderType.approvalImpact else -orderType.approvalImpact
            old.copy(
                vitals = old.vitals.copy(
                    approval = (old.vitals.approval + approvalDelta).coerceIn(0f, 100f),
                ),
                ovalOffice = old.ovalOffice.copy(decrees = updatedDecrees),
            )
        }
        if (willEnact) {
            notifyUser("Executive Decree Signed: ${orderType.title} now in force.", success = true)
        } else {
            notifyUser("Executive Decree Revoked: ${orderType.title} rescinded.", success = true)
        }
    }

    fun worldEconomicRank(): Int = analyticsEngine.worldEconomicRank(_state.value)

    fun signArmistice() {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.signArmistice(it) }
    }

    fun setDeployment(status: DeploymentStatus) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.setDeployment(it, status) }
    }

    fun setSalaryFunding(funding: Float) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.setSalaryFunding(it, funding) }
    }

    fun recruitPersonnel(amount: Long) {
        if (_currentActiveEvent.value != null || amount <= 0L) return
        val current = _state.value
        val perk = com.presidentsimulator.game.data.NationalPerkEffects.forNationId(current.playerNation.id)
        val lawRecruit = current.legal.combinedMilitaryRecruitModifier.coerceAtLeast(0.5f)
        val cost = (
            amount * DiplomacyEngine.RECRUIT_COST_PER_SOLDIER *
                com.presidentsimulator.game.data.NationalPerkEffects.recruitCostMultiplier(perk) / lawRecruit
        ).toLong().coerceAtLeast(1L)
        if (current.vitals.budget < cost) {
            notifyUser("Insufficient Recruitment Funds: Requires ${cost.toBudgetString()} (Treasury: ${current.vitals.budget.toBudgetString()})", success = false)
            return
        }
        applyActionWithFeedback(
            "Recruitment Order Confirmed: +$amount active personnel for ${cost.toBudgetString()}.",
            "Insufficient budget to recruit personnel."
        ) { diplomacyEngine.recruitPersonnel(it, amount) }
    }

    fun upgradeMilitaryTraining() {
        if (_currentActiveEvent.value != null) return
        val current = _state.value
        val level = current.military.trainingLevel.coerceIn(1, DiplomacyEngine.MAX_TRAINING_LEVEL)
        if (level >= DiplomacyEngine.MAX_TRAINING_LEVEL) {
            notifyUser("Armed Forces Already at Maximum Tactical Training Level (${DiplomacyEngine.MAX_TRAINING_LEVEL}).", success = false)
            return
        }
        val cost = DiplomacyEngine.MILITARY_TRAINING_BASE_COST * level
        if (current.vitals.budget < cost) {
            notifyUser("Insufficient Defense Budget: Combat readiness drills require ${cost.toBudgetString()} (Treasury: ${current.vitals.budget.toBudgetString()})", success = false)
            return
        }
        applyActionWithFeedback(
            "Combat Readiness Drills Completed (+10% training tier).",
            "Drill failed: Insufficient funds (${cost.toBudgetString()})."
        ) { diplomacyEngine.upgradeMilitaryTraining(it) }
    }

    fun setFrontlineFocus(countryId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.setFrontlineFocus(it, countryId) }
    }

    fun purchaseMilitaryHardware(hardware: MilitaryHardware, amount: Int) {
        if (amount <= 0) return
        val current = _state.value
        if (hardware == MilitaryHardware.NUCLEAR_ARSENAL && current.governance.nuclearEmbargoActive) {
            notifyUser("Procurement Blocked: UN Nuclear Non-Proliferation Sanctions in effect.", success = false)
            return
        }
        if (hardware != MilitaryHardware.NUCLEAR_ARSENAL && current.governance.weaponsBanActive) {
            notifyUser("Procurement Blocked: UN Security Council Weapons Embargo active.", success = false)
            return
        }
        val cost = hardware.unitCost * amount
        if (current.vitals.budget < cost) {
            notifyUser(
                "Insufficient Defense Budget: ${amount}x ${hardware.displayName} requires ${cost.toBudgetString()} (Treasury: ${current.vitals.budget.toBudgetString()})",
                success = false
            )
            return
        }
        applyActionWithFeedback(
            "Procurement Approved: ${amount}x ${hardware.displayName} acquired for ${cost.toBudgetString()}.",
            "Failed: Insufficient budget or weapons embargo active."
        ) { diplomacyEngine.purchaseHardware(it, amount, hardware) }
    }

    fun purchaseTanks(amount: Int) = purchaseMilitaryHardware(MilitaryHardware.TANKS, amount)
    fun purchaseJets(amount: Int) = purchaseMilitaryHardware(MilitaryHardware.FIGHTER_JETS, amount)
    fun purchaseShips(amount: Int) = purchaseMilitaryHardware(MilitaryHardware.NAVAL_SHIPS, amount)
    fun purchaseNukes(amount: Int) = purchaseMilitaryHardware(MilitaryHardware.NUCLEAR_ARSENAL, amount)

    fun launchOffensive() {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.launchOffensive(it) }
    }

    fun holdDefensiveLine() {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.holdDefensiveLine(it) }
    }

    fun canDeclareWar(targetCountryId: String): Boolean =
        DiplomacyEngine.canDeclareWar(_state.value, targetCountryId)

    fun canAffordTreaty(type: TreatyType): Boolean =
        DiplomacyEngine.treatyAffordable(_state.value, type)

    fun armisticeCost(): Long = DiplomacyEngine.armisticeCost(_state.value)

    fun setTimeSpeedMode(mode: TimeSpeedMode) {
        if (_state.value.gameOver.isGameOver && mode != TimeSpeedMode.PAUSED) return
        _timeSpeedMode.value = mode
        syncTimeTickJob()
        if (mode == TimeSpeedMode.PAUSED) {
            // Manual pause is an explicit checkpoint moment.
            saveGameProgress()
        }
    }

    private fun syncTimeTickJob() {
        autoTickJob?.cancel()
        autoTickJob = null

        val interval = _timeSpeedMode.value.intervalMs ?: return
        autoTickJob = viewModelScope.launch {
            while (isActive) {
                val blocked = _currentActiveEvent.value != null ||
                    _missionResults.value.isNotEmpty() ||
                    _warOutcome.value != null ||
                    _state.value.agenda.needsBriefing ||
                    _state.value.demographics.election.hasPendingNight ||
                    _state.value.gameOver.isGameOver
                if (!blocked) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { advanceTimeTick() }
                }
                delay(interval)
            }
        }
    }

    /**
     * When the app leaves the foreground ([MainActivity.onStop]): halt the ticker so
     * the campaign never simulates unattended in the background (§5.8), then checkpoint
     * so a subsequent process death cannot lose anything since the last save.
     */
    fun onAppBackgrounded() {
        if (_showLaunchScreen.value) return
        pauseTimeAdvance(checkpoint = false)
        saveGameProgress()
    }

    // ── Undo ───────────────────────────────────────────────────────────────

    private fun rememberForUndo(snapshot: GameState) {
        undoStack.addLast(snapshot)
        while (undoStack.size > MAX_UNDO_MONTHS) undoStack.removeFirst()
        _undoCount.value = undoStack.size
    }

    private fun clearUndoHistory() {
        undoStack.clear()
        _undoCount.value = 0
    }

    /**
     * Rolls back the most recent simulated month. Engines are pure state→state
     * functions, so restoring the pre-tick snapshot is exact. Blocked while any
     * modal (crisis, mission results, war outcome, election night) is up, because
     * those modals belong to the timeline being replaced.
     */
    fun undoLastMonth(): Boolean {
        if (_currentActiveEvent.value != null) return false
        if (_missionResults.value.isNotEmpty()) return false
        if (_warOutcome.value != null) return false
        if (_state.value.gameOver.isGameOver) return false
        if (_state.value.demographics.election.hasPendingNight) return false

        val snapshot = undoStack.removeLastOrNull() ?: return false
        if (snapshot.crisis.pendingEventId != null) {
            // Never restore into a state that expects a crisis dialog.
            undoStack.addLast(snapshot)
            _undoCount.value = undoStack.size
            return false
        }
        _undoCount.value = undoStack.size
        pauseTimeAdvance(checkpoint = false)
        _state.value = snapshot
        _currentActiveEvent.value = null
        _missionResults.value = emptyList()
        // The debounced action save persists the restored state shortly after.
        return true
    }

    /** Auto-save cadence while the ticker runs: one write every N simulated months. */
    private fun maybeAutoSave() {
        ticksSinceLastSave++
        if (ticksSinceLastSave >= AUTO_SAVE_TICK_INTERVAL) checkpointSave()
    }

    /**
     * Throttled automatic save: briefing/tick checkpoints must not serialize the payload
     * more than once per [AUTO_SAVE_MIN_INTERVAL_MS] while the player plays quickly.
     * Forced saves (manual pause, foreground loss, blocking events, quiet action debounce)
     * bypass this.
     */
    private fun checkpointSave() {
        if (System.currentTimeMillis() - lastSaveAtMs < AUTO_SAVE_MIN_INTERVAL_MS) return
        saveGameProgress()
    }

    private fun pauseTimeAdvance(checkpoint: Boolean = true) {
        _timeSpeedMode.value = TimeSpeedMode.PAUSED
        autoTickJob?.cancel()
        autoTickJob = null
        if (checkpoint) checkpointSave()
    }

    // ── Crisis engine ────────────────────────────────────────────────────────

    fun resolveEvent(choice: EventChoice) {
        val active = _currentActiveEvent.value ?: return
        if (choice !in active.choices) return

        _state.update { current ->
            val cooldown = if (StoryArcEngine.isStoryEvent(active.id)) current.crisis.eventCooldownMonths else EVENT_COOLDOWN_MONTHS
            val appliedChoice = choice.consequence.applyTo(
                current.copy(crisis = current.crisis.copy(pendingEventId = null, eventCooldownMonths = cooldown)),
            )
            val applied = StoryArcEngine.resolve(appliedChoice, active.id, active.choices.indexOf(choice))
            applied.copy(crisis = lingeringFrom(choice.consequence, active.title, applied.crisis))
        }
        _currentActiveEvent.value = null
    }

    private fun maybeTriggerEvent() {
        if (_currentActiveEvent.value != null) return
        val pendingId = _state.value.crisis.pendingEventId
        if (pendingId != null) {
            _currentActiveEvent.value = StoryArcEngine.nextEvent(_state.value) ?: EventRepository.byId(pendingId)
            return
        }
        if (_state.value.crisis.blocksNewEvents || _state.value.crisis.eventCooldownMonths > 0) return
        StoryArcEngine.nextEvent(_state.value)?.let { storyEvent ->
            _currentActiveEvent.value = storyEvent
            _state.update { it.copy(crisis = it.crisis.copy(pendingEventId = storyEvent.id)) }
            return
        }
        if (random.nextFloat() >= EVENT_CHANCE_PER_TICK) return
        val event = EventRepository.weightedEvent(_state.value, random)
        _currentActiveEvent.value = event
        _state.update {
            it.copy(crisis = it.crisis.copy(pendingEventId = event.id))
        }
    }

    private fun lingeringFrom(
        consequence: EventConsequence,
        title: String,
        current: ActiveCrisisState,
    ): ActiveCrisisState {
        val severe = kotlin.math.abs(consequence.approvalChange) >= 8f ||
            consequence.instabilityChange >= 8f ||
            consequence.populationChange <= -100_000L ||
            consequence.factoriesChange <= -2
        if (!severe) return current.copy(pendingEventId = null)
        return ActiveCrisisState(
            pendingEventId = null,
            eventCooldownMonths = current.eventCooldownMonths,
            lingeringMonths = 3,
            monthlyApprovalDelta = consequence.approvalChange * 0.12f,
            monthlyInstabilityDelta = consequence.instabilityChange * 0.2f,
            monthlyBudgetDelta = (consequence.budgetChange * 0.08).toLong(),
            label = title,
        )
    }

    // ── Economy actions ──────────────────────────────────────────────────────

    fun buildFactory(amount: Int) = buildInfrastructure(InfrastructureType.FACTORY, amount)

    fun buildFarm(amount: Int) = buildInfrastructure(InfrastructureType.FARM, amount)

    fun buildHousing(amount: Int) = buildInfrastructure(InfrastructureType.HOUSING, amount)

    fun buildInfrastructure(type: InfrastructureType, amount: Int) {
        if (_currentActiveEvent.value != null) return
        if (amount <= 0) return
        val current = _state.value
        val cost = type.unitCost * amount
        if (current.vitals.budget < cost) {
            notifyUser(
                "Insufficient Funds: Building ${amount}x ${type.name.lowercase().replace('_', ' ')} requires ${cost.toBudgetString()} (Treasury: ${current.vitals.budget.toBudgetString()})",
                success = false
            )
            return
        }

        when (type) {
            InfrastructureType.POWER_PLANT -> {
                buildPowerPlant(amount)
                return
            }
            InfrastructureType.MINE -> {
                buildMine(amount)
                return
            }
            else -> Unit
        }

        val economy = current.economy
        val updatedEconomy = when (type) {
            InfrastructureType.FACTORY ->
                economy.copy(factories = economy.factories + amount)
            InfrastructureType.FARM ->
                economy.copy(farms = economy.farms + amount)
            InfrastructureType.HOUSING ->
                economy.copy(housing = economy.housing + amount)
            InfrastructureType.POWER_PLANT,
            InfrastructureType.MINE,
            -> economy
        }

        val approvalBump = when (type) {
            InfrastructureType.HOUSING -> 0.4f * amount
            InfrastructureType.FARM -> 0.2f * amount
            InfrastructureType.FACTORY -> 0.1f * amount
            InfrastructureType.POWER_PLANT -> 0.15f * amount
            InfrastructureType.MINE -> 0.1f * amount
        }

        val sector = SectorInvestment.sectorForInfrastructure(type)
        val withSectorXp = if (sector != null) {
            current.awardSectorXp(sector, SectorInvestment.XP_PER_BUILD * amount)
        } else {
            current
        }

        _state.value = withSectorXp.copy(
            vitals = withSectorXp.vitals.copy(
                budget = withSectorXp.vitals.budget - cost,
                approval = (withSectorXp.vitals.approval + approvalBump).coerceIn(0f, 100f),
            ),
            economy = updatedEconomy,
        )
        notifyUser(
            "Construction Approved: +$amount ${type.name.lowercase().replace('_', ' ')} built for ${cost.toBudgetString()}",
            success = true
        )
    }

    fun maxAffordable(type: InfrastructureType): Int {
        val budget = _state.value.vitals.budget
        if (type.unitCost <= 0L) return 0
        return (budget / type.unitCost).toInt().coerceAtLeast(0)
    }

    fun adjustTaxes(newRate: Float) {
        if (_currentActiveEvent.value != null) return
        val clamped = newRate.coerceIn(MIN_TAX_RATE, MAX_TAX_RATE)
        _state.update { current ->
            val delta = clamped - current.economy.taxRate
            val approvalDelta = -delta * 100f * 0.8f
            current.copy(
                vitals = current.vitals.copy(
                    approval = (current.vitals.approval + approvalDelta).coerceIn(0f, 100f),
                ),
                economy = current.economy.copy(taxRate = clamped),
            )
        }
    }

    /** Voluntary repayment is capped to preserve a two-month operating reserve. */
    fun repayPublicDebt() {
        if (_currentActiveEvent.value != null) return
        _state.update { current ->
            val monthlyCosts = current.economy.totalExpenses +
                (current.military.monthlyUpkeep * current.cabinet.combinedEffects().militaryUpkeepMultiplier).toLong() +
                current.legal.totalUpkeep + current.internalSecurity.monthlyUpkeep + current.society.totalMinistryUpkeep +
                current.finance.monthlyInterestCost
            val protectedReserve = (monthlyCosts.coerceAtLeast(0L) * 2L).coerceAtMost(current.vitals.budget.coerceAtLeast(0L))
            val available = (current.vitals.budget - protectedReserve).coerceAtLeast(0L)
            val payment = (current.finance.publicDebt / 10L).coerceAtLeast(1L)
                .coerceAtMost(available).coerceAtMost(current.finance.publicDebt)
            if (payment <= 0L) return@update current
            current.copy(
                vitals = current.vitals.copy(budget = current.vitals.budget - payment),
                finance = current.finance.copy(
                    publicDebt = current.finance.publicDebt - payment,
                    creditScore = (current.finance.creditScore + 1).coerceAtMost(100),
                    ledger = (current.finance.ledger + com.presidentsimulator.game.data.FiscalEntry(
                        "Voluntary debt repayment", -payment, current.month, current.year,
                    )).takeLast(24),
                ),
            )
        }
    }

    fun projectNetIncome(taxRate: Float): Long {
        val current = _state.value
        return current.economy
            .copy(taxRate = taxRate.coerceIn(MIN_TAX_RATE, MAX_TAX_RATE))
            .netIncome(current.vitals.population)
    }

    fun projectTaxRevenue(taxRate: Float): Long {
        val current = _state.value
        return current.economy
            .copy(taxRate = taxRate.coerceIn(MIN_TAX_RATE, MAX_TAX_RATE))
            .taxRevenue(current.vitals.population)
    }

    // ── Internal physics ─────────────────────────────────────────────────────

    override fun onCleared() {
        pauseTimeAdvance()
        super.onCleared()
    }

    companion object {
        const val MIN_TAX_RATE = 0.00f
        const val MAX_TAX_RATE = 0.50f
        const val EVENT_CHANCE_PER_TICK = 0.005f

        /** Simulated months between background auto-saves while the ticker runs. */
        const val AUTO_SAVE_TICK_INTERVAL = 12

        /** Minimum wall-clock gap between *automatic* checkpoint saves. */
        const val AUTO_SAVE_MIN_INTERVAL_MS = 5_000L

        /** Quiet period after a player action before that action is auto-saved. */
        const val ACTION_SAVE_DEBOUNCE_MS = 2_000L

        /** Months the player can roll back (undo stack depth). */
        const val MAX_UNDO_MONTHS = 3

        /**
         * Months without random events after resolving one.
         * Default set to 36 months so random events are rare, meaningful crises
         * rather than relentless pop-up interruptions.
         */
        const val EVENT_COOLDOWN_MONTHS = 36
    }
}

/** One-shot effects surfaced by [GameViewModel] for the UI layer to render. */
sealed interface UiEvent {
    data class Notice(val message: String, val success: Boolean) : UiEvent
}

fun Long.toBudgetString(): String {
    val abs = kotlin.math.abs(this)
    val sign = if (this < 0) "-" else ""
    val body = when {
        abs >= 1_000_000_000_000L -> String.format(java.util.Locale.ROOT, "%.1fT", abs / 1_000_000_000_000.0)
        abs >= 1_000_000_000L -> String.format(java.util.Locale.ROOT, "%.1fB", abs / 1_000_000_000.0)
        abs >= 1_000_000L -> String.format(java.util.Locale.ROOT, "%.1fM", abs / 1_000_000.0)
        abs >= 1_000L -> String.format(java.util.Locale.ROOT, "%.1fK", abs / 1_000.0)
        else -> abs.toString()
    }
    return "$sign$$body"
}

fun Float.toApprovalString(): String = "${roundToInt()}%"

fun Long.toPopulationString(): String = when {
    this >= 1_000_000_000L -> String.format(java.util.Locale.ROOT, "%.2fB", this / 1_000_000_000.0)
    this >= 1_000_000L -> String.format(java.util.Locale.ROOT, "%.1fM", this / 1_000_000.0)
    this >= 1_000L -> String.format(java.util.Locale.ROOT, "%.1fK", this / 1_000.0)
    else -> toString()
}

fun Long.toArmyString(): String = when {
    this >= 1_000_000L -> String.format(java.util.Locale.ROOT, "%.2fM", this / 1_000_000.0)
    this >= 1_000L -> String.format(java.util.Locale.ROOT, "%.0fK", this / 1_000.0)
    else -> toString()
}
