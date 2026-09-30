package com.presidentsimulator.game.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.presidentsimulator.game.data.ActiveCrisisState
import com.presidentsimulator.game.data.AgendaItem
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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

    /** True while showing the launch/new-game screen. */
    private val _showLaunchScreen = MutableStateFlow(true)
    val showLaunchScreen: StateFlow<Boolean> = _showLaunchScreen.asStateFlow()

    private var autoTickJob: Job? = null
    /** Serializes [advanceTimeTick] so the auto-ticker and End Turn button can never interleave. */
    private val tickMutex = Mutex()
    /** True while an async save is being written; new save requests coalesce into this. */
    private val saveInFlight = AtomicBoolean(false)
    private val random = Random.Default
    private val diplomacyEngine = DiplomacyViewModel(random)
    private val productionLawEngine = ProductionLawViewModel()
    private val analyticsEngine = AnalyticsSaveViewModel()
    private val securityEngine = EspionageSecurityViewModel(random)
    private val advancementEngine = AdvancementViewModel()
    private val tradeEngine = TradeMarketViewModel(random)
    private val governanceEngine = GovernanceViewModel(random)
    private val demographicsEngine = DemographicsCampaignViewModel()
    private val monthlyPipeline = MonthlySimulationPipeline(
        random = random,
        diplomacy = diplomacyEngine,
        productionLaw = productionLawEngine,
        analytics = analyticsEngine,
        security = securityEngine,
        advancement = advancementEngine,
        trade = tradeEngine,
        governance = governanceEngine,
        demographics = demographicsEngine,
        advanceDate = ::advanceDate,
        applyPopulationChange = ::applyPopulationChange,
    )

    private val savePrefs = application.getSharedPreferences(
        AnalyticsSaveViewModel.PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    
    private fun applyActionWithFeedback(successMsg: String, failMsg: String, block: (GameState) -> GameState) {
        var success = false
        _state.update { old ->
            val new = block(old)
            if (new !== old) success = true
            new
        }
        if (success) {
            Toast.makeText(getApplication(), successMsg, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), failMsg, Toast.LENGTH_SHORT).show()
        }
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

                // Auto-save after every tick (async, coalesced).
                saveGameProgress()
                maybeTriggerEvent()
                if (_currentActiveEvent.value != null) {
                    pauseTimeAdvance()
                    // Persist the blocking event as well as the state changes that created it.
                    saveGameProgress()
                }
            }
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
        _state.update { current ->
            val cost = type.unitCost * amount
            if (current.vitals.budget < cost) return@update current
            val industry = current.militaryIndustry
            val updated = when (type) {
                MilitaryFacilityType.ARSENAL -> industry.copy(arsenals = industry.arsenals + amount)
                MilitaryFacilityType.AIRFIELD -> industry.copy(airfields = industry.airfields + amount)
                MilitaryFacilityType.SHIPYARD -> industry.copy(shipyards = industry.shipyards + amount)
            }
            current
                .copy(
                    vitals = current.vitals.copy(budget = current.vitals.budget - cost),
                    militaryIndustry = updated,
                )
                .pushNews(
                    "Construction: $amount ${type.displayName.lowercase()}(s) commissioned",
                    tag = "MILITARY",
                )
        }
    }

    fun availableLoan(): Long = LoanEngine.availableLoan(_state.value)

    fun canTakeLoan(): Boolean = LoanEngine.canTakeLoan(_state.value)

    fun takeLoan(amount: Long) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Loan secured.", "Loan unavailable (credit limit).") {
            LoanEngine.takeLoan(it, amount)
        }
    }

    fun repayLoan(amount: Long) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Repayment sent.", "Insufficient funds to repay.") {
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
        _state.update { governanceEngine.formAlliance(it, name, invitees) }
        Toast.makeText(getApplication(), "Alliance Formed.", Toast.LENGTH_SHORT).show()
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
        if (success) Toast.makeText(getApplication(), "Trade Deal Proposed.", Toast.LENGTH_SHORT).show()
        else Toast.makeText(getApplication(), "Trade Deal Failed (Check requirements).", Toast.LENGTH_SHORT).show()
    }

    fun cancelTradeDeal(dealId: String) {
        if (_currentActiveEvent.value != null) return
        var success = false
        _state.update { old ->
            val new = tradeEngine.cancelTradeDeal(old, dealId)
            if (new !== old) success = true
            new
        }
        if (success) Toast.makeText(getApplication(), "Trade Deal Cancelled.", Toast.LENGTH_SHORT).show()
    }

    fun setTariffRate(rate: Float) {
        if (_currentActiveEvent.value != null) return
        _state.update { tradeEngine.setTariffRate(it, rate) }
    }

    fun setGoodsExportQuota(quota: Float) {
        if (_currentActiveEvent.value != null) return
        _state.update { tradeEngine.setGoodsExportQuota(it, quota) }
    }

    fun buyFromMarket(commodity: TradeCommodity, amount: Long = TradeMarketViewModel.SPOT_BUNDLE_SIZE) {
        if (_currentActiveEvent.value != null) return
        _state.update { tradeEngine.buyFromMarket(it, commodity, amount) }
    }

    fun sellToMarket(commodity: TradeCommodity, amount: Long = TradeMarketViewModel.SPOT_BUNDLE_SIZE) {
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
        TradeMarketViewModel.stockOf(_state.value.production, commodity)

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
        _state.update { advancementEngine.allocateExtraResearchFunding(it) }
    }

    fun canStartResearch(techId: String): Boolean =
        AdvancementViewModel.canStartResearch(_state.value, techId)

    fun canAllocateExtraResearchFunding(): Boolean {
        val research = _state.value.research
        return research.activeTechId != null &&
            research.extraFundingTier < ResearchState.MAX_EXTRA_FUNDING_TIER &&
            _state.value.vitals.budget >= AdvancementViewModel.EXTRA_RESEARCH_FUNDING_COST
    }

    fun adjustMinistryFunding(ministry: SocietyMinistry, newFundingLevel: Float) {
        if (_currentActiveEvent.value != null) return
        _state.update { advancementEngine.adjustMinistryFunding(it, ministry, newFundingLevel) }
    }

    fun changeStateReligion(religion: com.presidentsimulator.game.data.StateReligion) {
        if (_currentActiveEvent.value != null) return
        _state.update { advancementEngine.changeStateReligion(it, religion) }
        Toast.makeText(getApplication(), "State Religion updated.", Toast.LENGTH_SHORT).show()
    }

    fun buildUniversity() {
        if (_currentActiveEvent.value != null) return
        _state.update { advancementEngine.buildUniversity(it) }
    }

    fun projectedSciencePerTick(): Long =
        advancementEngine.calculateScienceGenerated(_state.value)

    fun ministryForecast(ministry: SocietyMinistry): String =
        advancementEngine.forecastMinistryText(_state.value.society, ministry)

    fun canUnlockTechnology(techId: String): Boolean =
        AdvancementViewModel.canUnlock(_state.value, techId)

    // ── Espionage & internal security ────────────────────────────────────────

    fun deploySpy(targetCountryId: String, missionType: MissionType) {
        if (_currentActiveEvent.value != null) return
        _state.update { securityEngine.deploySpy(it, targetCountryId, missionType) }
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
        _state.update { securityEngine.recruitSpy(it) }
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
        EspionageSecurityViewModel.canDeploy(_state.value, targetCountryId, missionType)

    // ── Analytics & save/restore ──────────────────────────────────────────────

    fun currentGdp(): Long = analyticsEngine.calculateGDP(_state.value)

    fun exportGameStateToJson(): String =
        analyticsEngine.exportGameStateToJson(_state.value)

    fun importGameStateFromJson(jsonString: String): Boolean {
        return try {
            val restored = analyticsEngine.importGameStateFromJson(jsonString)
            _state.value = restored
            _currentActiveEvent.value = restored.crisis.pendingEventId
                ?.let { pendingId -> StoryArcEngine.nextEvent(restored) ?: EventRepository.byId(pendingId) }
            _missionResults.value = emptyList()
            pauseTimeAdvance()
            _showLaunchScreen.value = false
            _hasSave.value = true
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = analyticsEngine.exportGameStateToJson(stateSnapshot)
                savePrefs.edit()
                    .putString(AnalyticsSaveViewModel.KEY_AUTOMATED_SAVE, payload)
                    .putInt(AnalyticsSaveViewModel.KEY_LAST_PAYLOAD_BYTES, payload.length)
                    .apply()
                _saveLoadFeedback.value = analyticsEngine.feedbackForSave(payload)
                _hasSave.value = true
            } finally {
                saveInFlight.set(false)
            }
        }
    }

    fun loadLastAutomatedSave() {
        val payload = savePrefs.getString(AnalyticsSaveViewModel.KEY_AUTOMATED_SAVE, null)
        if (payload.isNullOrBlank()) {
            _saveLoadFeedback.value = analyticsEngine.feedbackForMissingSave()
            return
        }
        importGameStateFromJson(payload)
    }

    fun hasAutomatedSave(): Boolean =
        !savePrefs.getString(AnalyticsSaveViewModel.KEY_AUTOMATED_SAVE, null).isNullOrBlank()

    private val _saveSlots = MutableStateFlow<List<SaveSlotInfo>>(emptyList())
    val saveSlots: StateFlow<List<SaveSlotInfo>> = _saveSlots.asStateFlow()

    /** Refreshes slot metadata off the main thread; call before showing the launch screen. */
    fun refreshSaveSlots() {
        viewModelScope.launch(Dispatchers.IO) {
            _saveSlots.value = computeSaveSlots()
        }
    }

    private fun computeSaveSlots(): List<SaveSlotInfo> =
        (1..AnalyticsSaveViewModel.SLOT_COUNT).map { slot ->
            val payload = savePrefs.getString(AnalyticsSaveViewModel.slotKey(slot), null)
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
        if (slot !in 1..AnalyticsSaveViewModel.SLOT_COUNT) return
        val payload = analyticsEngine.exportGameStateToJson(_state.value)
        savePrefs.edit()
            .putString(AnalyticsSaveViewModel.slotKey(slot), payload)
            .putInt(AnalyticsSaveViewModel.KEY_LAST_PAYLOAD_BYTES, payload.length)
            .apply()
        _saveLoadFeedback.value = SaveLoadFeedback(
            message = "Saved to slot $slot (${AnalyticsSaveViewModel.formatBytes(payload.length)}).",
            payloadBytes = payload.length,
            success = true,
        )
        _hasSave.value = true
    }

    fun loadFromSlot(slot: Int) {
        if (slot !in 1..AnalyticsSaveViewModel.SLOT_COUNT) return
        val payload = savePrefs.getString(AnalyticsSaveViewModel.slotKey(slot), null)
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
                message = "Loaded slot $slot (${AnalyticsSaveViewModel.formatBytes(payload.length)}).",
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
        _state.update { OppositionEngine.negotiate(it) }
        Toast.makeText(getApplication(), "Negotiations held. Stability changed.", Toast.LENGTH_SHORT).show()
    }

    fun smearOpposition() {
        if (_currentActiveEvent.value != null) return
        _state.update { OppositionEngine.smear(it) }
        Toast.makeText(getApplication(), "Smear campaign initiated.", Toast.LENGTH_SHORT).show()
    }

    fun concedeToOpposition() {
        if (_currentActiveEvent.value != null) return
        _state.update { OppositionEngine.concedePlatform(it) }
        Toast.makeText(getApplication(), "Concessions made to opposition.", Toast.LENGTH_SHORT).show()
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

    fun startNewGame(countryId: String = "us", scenarioId: String = "standard", challengeId: String = "standard") {
        val seeded = ScenarioCatalog.apply(GameState.initial(countryId), scenarioId, challengeId = challengeId)
        _state.value = seeded
        _currentActiveEvent.value = null
        _missionResults.value = emptyList()
        pauseTimeAdvance()
        _showLaunchScreen.value = false
        saveGameProgress()
    }

    fun returnToLaunch() {
        pauseTimeAdvance()
        _currentActiveEvent.value = null
        _missionResults.value = emptyList()
        _hasSave.value = hasAutomatedSave()
        _showLaunchScreen.value = true
    }

    // ── Production & law actions ──────────────────────────────────────────────

    fun enactLaw(lawId: String) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Law Action Processed.", "Failed: Insufficient Capital/Budget.") { productionLawEngine.enactLaw(it, lawId) }
    }

    fun negotiatePendingLaw(lawId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { productionLawEngine.negotiatePendingLaw(it, lawId) }
    }

    fun repealLaw(lawId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { productionLawEngine.repealLaw(it, lawId) }
    }

    fun buildPowerPlant(amount: Int) {
        applyActionWithFeedback("Started construction.", "Insufficient budget to build Power Plants.") { productionLawEngine.buildPowerPlant(it, amount) }
    }

    fun buildMine(amount: Int) {
        applyActionWithFeedback("Started construction.", "Insufficient budget to build Mines.") { productionLawEngine.buildMine(it, amount) }
    }

    fun canEnactLaw(lawId: String): Boolean {
        val law = com.presidentsimulator.game.data.LawCatalog.byId(lawId) ?: return false
        return ProductionLawViewModel.canEnact(_state.value, law)
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
        Toast.makeText(getApplication(), "WAR DECLARED!", Toast.LENGTH_LONG).show()
    }

    fun claimWarSettlement() {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.claimWarSettlement(it) }
    }

    fun negotiateTreaty(targetCountryId: String, type: TreatyType) {
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Treaty Signed.", "Failed: Insufficient Capital/Relations.") { diplomacyEngine.negotiateTreaty(it, targetCountryId, type) }
        Toast.makeText(getApplication(), "Treaty Negotiated.", Toast.LENGTH_SHORT).show()
    }

    fun breakTreaty(targetCountryId: String, type: TreatyType) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.breakTreaty(it, targetCountryId, type) }
        Toast.makeText(getApplication(), "Treaty Broken!", Toast.LENGTH_SHORT).show()
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
        _state.update { productionLawEngine.cancelPendingLaw(it, lawId) }
    }

    fun rushPendingLaw(lawId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { productionLawEngine.rushPendingLaw(it, lawId) }
    }

    fun sendForeignAid(targetCountryId: String) {
        if (_currentActiveEvent.value != null) return
        var success = false
        _state.update { old ->
            val new = diplomacyEngine.sendForeignAid(old, targetCountryId)
            if (new !== old) success = true
            new
        }
        if (success) Toast.makeText(getApplication(), "Foreign Aid Sent.", Toast.LENGTH_SHORT).show()
        else Toast.makeText(getApplication(), "Action Failed (Insufficient Budget/Cooldown).", Toast.LENGTH_SHORT).show()
    }

    fun canSendForeignAid(targetCountryId: String): Boolean =
        DiplomacyViewModel.canSendForeignAid(_state.value, targetCountryId)

    fun conductStateVisit(targetCountryId: String) {
        if (_currentActiveEvent.value != null) return
        var success = false
        _state.update { old ->
            val new = diplomacyEngine.conductStateVisit(old, targetCountryId)
            if (new !== old) success = true
            new
        }
        if (success) Toast.makeText(getApplication(), "State Visit Completed.", Toast.LENGTH_SHORT).show()
        else Toast.makeText(getApplication(), "Action Failed (Insufficient Capital/Cooldown).", Toast.LENGTH_SHORT).show()
    }

    fun canConductStateVisit(targetCountryId: String): Boolean =
        DiplomacyViewModel.canConductStateVisit(_state.value, targetCountryId)

    fun runCampaignAction(action: CampaignAction) {
        if (_currentActiveEvent.value != null) return
        _state.update { demographicsEngine.runCampaignAction(it, action) }
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
        if (_currentActiveEvent.value != null) return
        applyActionWithFeedback("Recruitment started.", "Insufficient budget to recruit personnel.") { diplomacyEngine.recruitPersonnel(it, amount) }
    }

    fun upgradeMilitaryTraining() {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.upgradeMilitaryTraining(it) }
    }

    fun setFrontlineFocus(countryId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.setFrontlineFocus(it, countryId) }
    }

    fun purchaseTanks(amount: Int) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.purchaseTanks(it, amount) }
    }

    fun purchaseJets(amount: Int) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.purchaseJets(it, amount) }
    }

    fun purchaseShips(amount: Int) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.purchaseShips(it, amount) }
    }

    fun purchaseNukes(amount: Int) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.purchaseNukes(it, amount) }
    }

    fun purchaseMilitaryHardware(hardware: MilitaryHardware, amount: Int) {
        applyActionWithFeedback("Order placed.", "Failed: Insufficient budget or weapons embargo active.") { diplomacyEngine.purchaseHardware(it, amount, hardware) }
    }

    fun launchOffensive() {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.launchOffensive(it) }
    }

    fun holdDefensiveLine() {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.holdDefensiveLine(it) }
    }

    fun canDeclareWar(targetCountryId: String): Boolean =
        DiplomacyViewModel.canDeclareWar(_state.value, targetCountryId)

    fun canAffordTreaty(type: TreatyType): Boolean =
        DiplomacyViewModel.treatyAffordable(_state.value, type)

    fun armisticeCost(): Long = DiplomacyViewModel.armisticeCost(_state.value)

    fun setTimeSpeedMode(mode: TimeSpeedMode) {
        if (_state.value.gameOver.isGameOver && mode != TimeSpeedMode.PAUSED) return
        _timeSpeedMode.value = mode
        syncTimeTickJob()
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

    private fun pauseTimeAdvance() {
        _timeSpeedMode.value = TimeSpeedMode.PAUSED
        autoTickJob?.cancel()
        autoTickJob = null
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
        _state.update { current ->
            val cost = type.unitCost * amount
            if (current.vitals.budget < cost) return@update current

            when (type) {
                InfrastructureType.POWER_PLANT ->
                    return@update productionLawEngine.buildPowerPlant(current, amount)
                InfrastructureType.MINE ->
                    return@update productionLawEngine.buildMine(current, amount)
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

            withSectorXp.copy(
                vitals = withSectorXp.vitals.copy(
                    budget = withSectorXp.vitals.budget - cost,
                    approval = (withSectorXp.vitals.approval + approvalBump).coerceIn(0f, 100f),
                ),
                economy = updatedEconomy,
            )
        }
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

    private fun advanceDate(month: Int, year: Int): Pair<Int, Int> =
        if (month >= 12) 1 to (year + 1) else (month + 1) to year

    private fun applyPopulationChange(state: GameState): Long {
        val housingCapacity = state.economy.housing * 1_500_000L
        val population = state.vitals.population
        val approval = state.vitals.approval

        val housingFactor = when {
            population < housingCapacity * 0.85 -> 1.002
            population > housingCapacity -> 0.997
            else -> 1.0005
        }
        val approvalFactor = when {
            approval >= 60f -> 1.001
            approval <= 30f -> 0.998
            else -> 1.0
        }

        return (population * housingFactor * approvalFactor)
            .toLong()
            .coerceAtLeast(1_000_000L)
    }

    override fun onCleared() {
        pauseTimeAdvance()
        super.onCleared()
    }

    companion object {
        const val MIN_TAX_RATE = 0.00f
        const val MAX_TAX_RATE = 0.50f
        const val EVENT_CHANCE_PER_TICK = 0.02f

        /**
         * Months without random events after resolving one.
         * The state model's own default is 24; this constant is deliberately smaller
         * so a 2%/tick trigger rate yields a visible but not relentless event cadence.
         */
        const val EVENT_COOLDOWN_MONTHS = 12
    }
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
