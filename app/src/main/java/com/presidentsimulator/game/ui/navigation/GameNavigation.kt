package com.presidentsimulator.game.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.presidentsimulator.game.audio.GameAudioBridge
import com.presidentsimulator.game.audio.GameAudioCrisisEffect
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playClick
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.GovernanceUNScreen
import com.presidentsimulator.game.ui.components.ElectionNightDialog
import com.presidentsimulator.game.ui.components.EventCrisisDialog
import com.presidentsimulator.game.ui.components.GameTutorialDialog
import com.presidentsimulator.game.ui.components.GlobalHud
import com.presidentsimulator.game.ui.components.MinistryBottomNav
import com.presidentsimulator.game.ui.components.MissionResultDialog
import com.presidentsimulator.game.ui.components.MorningBriefingDialog
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.TurnSummaryDialog
import com.presidentsimulator.game.ui.components.WarOutcomeDialog
import com.presidentsimulator.game.ui.components.collectAlertCount
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.screens.AnalyticsScreen
import com.presidentsimulator.game.ui.screens.ApprovalDemographicsScreen
import com.presidentsimulator.game.ui.screens.CabinetScreen
import com.presidentsimulator.game.ui.screens.DiplomacyScreen
import com.presidentsimulator.game.ui.screens.EconomyScreen
import com.presidentsimulator.game.ui.screens.CountrySelectScreen
import com.presidentsimulator.game.ui.screens.LaunchScreen
import com.presidentsimulator.game.ui.screens.LawsScreen
import com.presidentsimulator.game.ui.screens.MainDashboardScreen
import com.presidentsimulator.game.ui.screens.MilitaryScreen
import com.presidentsimulator.game.ui.screens.ScienceScreen
import com.presidentsimulator.game.ui.screens.SecurityScreen
import com.presidentsimulator.game.ui.screens.SettingsAudioScreen
import com.presidentsimulator.game.viewmodel.GameViewModel
import kotlin.math.roundToInt

@Composable
fun GameNavigation(
    viewModel: GameViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val state by viewModel.state.collectAsState()
    val timeSpeedMode by viewModel.timeSpeedMode.collectAsState()
    val activeEvent by viewModel.currentActiveEvent.collectAsState()
    val turnSummary by viewModel.turnSummary.collectAsState()
    val missionResults by viewModel.missionResults.collectAsState()
    val warOutcome by viewModel.warOutcome.collectAsState()
    val showLaunch by viewModel.showLaunchScreen.collectAsState()
    val hasSave by viewModel.hasSave.collectAsState()
    val gameOver = state.gameOver.isGameOver
    val isVictory = state.gameOver.isVictory

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }
    var showCountrySelect by remember { mutableStateOf(false) }
    var showTutorial by rememberSaveable { mutableStateOf(false) }
    var tutorialPage by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(showLaunch) {
        if (showLaunch) showCountrySelect = false
    }

    BackHandler(enabled = showLaunch && showCountrySelect) {
        audio.playClick()
        showCountrySelect = false
    }

    val navigate: (GameDestination) -> Unit = { destination ->
        audio.playClick()
        navController.navigate(destination.route) {
            launchSingleTop = true
            popUpTo(GameDestination.Dashboard.route) { saveState = true }
            restoreState = true
        }
    }

    if (showLaunch) {
        if (showCountrySelect) {
            CountrySelectScreen(
                nations = viewModel.playableNations(),
                onBack = {
                    audio.playClick()
                    showCountrySelect = false
                },
                onSelectCountry = { countryId, scenarioId, challengeId ->
                    audio.playClick()
                    viewModel.startNewGame(countryId, scenarioId, challengeId)
                    showCountrySelect = false
                    tutorialPage = 0
                    showTutorial = true
                },
            )
        } else {
            LaunchScreen(
                hasSave = hasSave,
                onContinueGame = {
                    audio.playClick()
                    viewModel.continueGame()
                },
                onNewGame = {
                    audio.playClick()
                    showCountrySelect = true
                },
                slots = viewModel.listSaveSlots(),
                onLoadSlot = { slot ->
                    audio.playClick()
                    viewModel.loadFromSlot(slot)
                },
            )
        }
        return
    }

    GameAudioBridge(state = state)
    GameAudioCrisisEffect(hasActiveEvent = activeEvent != null)

    // Overlay priority: crisis > election night > war end > missions > turn summary > morning briefing > campaign end
    activeEvent?.let { event ->
        EventCrisisDialog(
            event = event,
            onChoiceSelected = { choice ->
                audio.playClick()
                viewModel.resolveEvent(choice)
            },
        )
    }

    val electionNight = state.demographics.election.pendingNight
    if (activeEvent == null && electionNight != null) {
        ElectionNightDialog(
            result = electionNight,
            onConfirm = {
                audio.playClick()
                viewModel.confirmElectionNight()
            },
        )
    }

    if (activeEvent == null && electionNight == null) {
        warOutcome?.let { outcome ->
            WarOutcomeDialog(
                outcome = outcome,
                onDismiss = {
                    audio.playClick()
                    viewModel.clearWarOutcome()
                },
            )
        }
    }

    val pendingMission = missionResults.firstOrNull()
    BackHandler(
        enabled = !showLaunch && !gameOver && currentRoute != null && currentRoute != GameDestination.Dashboard.route &&
            activeEvent == null && electionNight == null && warOutcome == null && pendingMission == null &&
            turnSummary == null && !state.agenda.needsBriefing,
    ) {
        audio.playClick()
        navController.popBackStack(GameDestination.Dashboard.route, inclusive = false)
    }
    if (activeEvent == null && electionNight == null && warOutcome == null && pendingMission != null) {
        MissionResultDialog(
            mission = pendingMission,
            state = state,
            onDismiss = {
                audio.playClick()
                viewModel.dismissMissionResult()
            },
        )
    }

    if (activeEvent == null && electionNight == null && warOutcome == null && pendingMission == null) {
        turnSummary?.let { summary ->
            TurnSummaryDialog(
                summary = summary,
                onDismiss = {
                    audio.playClick()
                    viewModel.clearTurnSummary()
                },
            )
        }
    }

    if (
        !showTutorial &&
        activeEvent == null &&
        electionNight == null &&
        warOutcome == null &&
        pendingMission == null &&
        turnSummary == null &&
        state.agenda.needsBriefing
    ) {
        MorningBriefingDialog(
            agenda = state.agenda,
            year = state.year,
            month = state.month,
            outlook = state.diplomacy.activeWar?.let { war ->
                val rivalName = state.diplomacy.rivalById(war.targetCountryId)?.name ?: "your rival"
                "The war with $rivalName remains the immediate priority. Review the front and decide whether to press or negotiate."
            } ?: when {
                state.netIncome < 0L -> "The treasury is losing about ${kotlin.math.abs(state.netIncome) / 1_000_000_000L}B each month. Consider a fiscal adjustment before reserves tighten."
                state.nextElectionYear > 0 && state.nextElectionYear - state.year <= 1 -> "The next election is approaching. Check the latest cohort polling and identify a group you need to win back."
                else -> "The government enters ${state.dateLabel} with ${state.vitals.approval.toInt()}% approval. Choose one priority and follow its effects through the next turn."
            },
            storyline = state.storyArc.activeArcId?.let { "${state.storyArc.lastStoryNote} · chapter ${state.storyArc.chapter} of 3" },
            onDismiss = {
                audio.playClick()
                viewModel.acknowledgeBriefing()
            },
            onJumpToAction = { item ->
                audio.playClick()
                viewModel.actOnAgendaItem(item)
                GameDestination.fromRoute(item.targetRoute)?.let(navigate)
            },
        )
    }

    if (showTutorial) {
        GameTutorialDialog(
            page = tutorialPage,
            onNext = {
                audio.playClick()
                if (tutorialPage >= 3) viewModel.dismissTutorial() else tutorialPage += 1
            },
            onBack = {
                audio.playClick()
                tutorialPage = (tutorialPage - 1).coerceAtLeast(0)
            },
            onSkip = {
                audio.playClick()
                viewModel.dismissTutorial()
            },
        )
    }

    if (gameOver) {
        CampaignEndDialog(
            isVictory = isVictory,
            reason = state.gameOver.reason,
            campaign = state,
            onLoadSave = { viewModel.loadLastAutomatedSave() },
            onReturnToLaunch = { viewModel.returnToLaunch() },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NssBackground),
    ) {
        GlobalHud(
            state = state,
            timeSpeedMode = timeSpeedMode,
            timeSpeedEnabled = !gameOver,
            alertCount = collectAlertCount(state),
            onTimeSpeedModeSelected = { mode ->
                audio.playClick()
                viewModel.setTimeSpeedMode(mode)
            },
        )

        Row(modifier = Modifier.weight(1f).fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                com.presidentsimulator.game.ui.components.InteractiveWorldMap(modifier = Modifier.fillMaxSize())
            NavHost(
                navController = navController,
                startDestination = GameDestination.Dashboard.route,
                modifier = Modifier.fillMaxSize(),
            ) {
            composable(GameDestination.Dashboard.route) {
                MainDashboardScreen(
                    state = state,
                    onNavigate = navigate,
                    onSpinHeadline = { viewModel.spinPressHeadline(it) },
                    onSuppressHeadline = { viewModel.suppressPressHeadline(it) },
                    onDisasterResponse = { viewModel.allocateDisasterResponse(it) },
                    onMakeCommitment = { viewModel.makeMandateCommitment(it) },
                )
            }
            composable(GameDestination.Economy.route) {
                EconomyScreen(state = state, viewModel = viewModel)
            }
            composable(GameDestination.Military.route) {
                MilitaryScreen(state = state, viewModel = viewModel)
            }
            composable(GameDestination.Diplomacy.route) {
                DiplomacyScreen(state = state, viewModel = viewModel)
            }
            composable(GameDestination.SecretService.route) {
                SecurityScreen(state = state, viewModel = viewModel)
            }
            composable(GameDestination.Science.route) {
                ScienceScreen(viewModel = viewModel)
            }
            composable(GameDestination.LawsSociety.route) {
                LawsScreen(viewModel = viewModel)
            }
            composable(GameDestination.Governance.route) {
                GovernanceUNScreen(state = state, viewModel = viewModel)
            }
            composable(GameDestination.AudioSettings.route) {
                SettingsAudioScreen(viewModel = viewModel)
            }
            composable(GameDestination.Analytics.route) {
                AnalyticsScreen(state = state, viewModel = viewModel)
            }
            composable(GameDestination.Demographics.route) {
                ApprovalDemographicsScreen(state = state, viewModel = viewModel)
            }
            composable(GameDestination.Cabinet.route) {
                CabinetScreen(state = state, viewModel = viewModel)
            }
            }
            }
            MinistryBottomNav(
                state = state,
                currentRoute = currentRoute,
                onNavigate = navigate,
                sideRail = true,
                modifier = Modifier.width(80.dp).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun CampaignEndDialog(
    isVictory: Boolean,
    reason: String,
    campaign: GameState,
    onLoadSave: () -> Unit,
    onReturnToLaunch: () -> Unit,
) {
    val accent = if (isVictory) NssEmerald else NssRed
    val headline = if (isVictory) "VICTORY" else "GAME OVER"
    val title = if (isVictory) "Mandate Secured" else "Regime Collapsed"

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .clip(NssCardShape)
                .background(NssBackground)
                .padding(20.dp),
        ) {
            Text(headline, fontSize = 10.sp, fontWeight = FontWeight.Black, color = accent, letterSpacing = 3.sp)
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = NssForeground,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Text(reason, fontSize = 13.sp, color = NssMutedForeground, lineHeight = 18.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            NssPanel(modifier = Modifier.fillMaxWidth()) {
                val scores = campaign.legacy.scores
                val finalScore = (scores.overall * campaign.scenario.scoreMultiplier).roundToInt()
                Text("NATIONAL LEADERSHIP LEGACY · ${scores.grade.uppercase()}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = accent, letterSpacing = 1.5.sp)
                Text("Campaign score  $finalScore", fontSize = 20.sp, fontWeight = FontWeight.Black, color = NssForeground, modifier = Modifier.padding(top = 4.dp))
                Text("${scores.overall} base × ${campaign.scenario.scoreMultiplier} challenge modifier", fontSize = 10.sp, color = NssMutedForeground)
                listOf(
                    "Prosperity" to scores.prosperity,
                    "Security" to scores.security,
                    "Diplomacy" to scores.diplomacy,
                    "Society" to scores.society,
                    "Mandate" to scores.mandate,
                ).forEach { (pillar, value) ->
                    Text("$pillar  ·  $value", fontSize = 11.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 3.dp))
                }
                val honors = campaignHonors(campaign, isVictory)
                if (honors.isNotEmpty()) {
                    Text("HONORS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = NssAccent, letterSpacing = 1.sp, modifier = Modifier.padding(top = 8.dp))
                    honors.forEach { honor -> Text("✦  $honor", fontSize = 11.sp, color = NssForeground, modifier = Modifier.padding(top = 3.dp)) }
                }
                if (campaign.mandate.lastReview.isNotEmpty()) {
                    Text("TERM PROMISE REVIEW", fontSize = 9.sp, fontWeight = FontWeight.Black, color = NssAccent, letterSpacing = 1.sp, modifier = Modifier.padding(top = 8.dp))
                    campaign.mandate.lastReview.forEach { result ->
                        Text("${if (result.fulfilled) "✓" else "×"} ${result.goal.title} · ${result.review}", fontSize = 10.sp, color = if (result.fulfilled) NssEmerald else NssMutedForeground, modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }
            Text(
                text = "Load Last Save",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(NssCardShape)
                    .background(NssPrimary)
                    .clickable(onClick = onLoadSave)
                    .padding(vertical = 12.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Return to Title",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(NssCardShape)
                    .background(NssAccent)
                    .clickable(onClick = onReturnToLaunch)
                    .padding(vertical = 12.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun campaignHonors(state: GameState, victory: Boolean): List<String> = buildList {
    if (state.legacy.electionsWon >= 2) add("Long Mandate · won ${state.legacy.electionsWon} elections")
    if (state.legacy.warsWon >= 1 && state.legacy.warsLost == 0) add("Unbeaten Commander · no wars lost")
    if (state.legacy.disastersHandled >= 3) add("Steady Hand · contained ${state.legacy.disastersHandled} disasters")
    if (state.legacy.lawsEnacted >= 5) add("Reformer · enacted ${state.legacy.lawsEnacted} laws")
    if (state.legacy.peakApproval >= 80f) add("People's Mandate · reached ${state.legacy.peakApproval.toInt()}% approval")
    if (state.storyArc.completedArcIds.isNotEmpty()) add("Crisis Storyteller · closed ${state.storyArc.completedArcIds.size} political story arc(s)")
    if (victory && state.scenario.challengeId != "standard") add("Challenge cleared · ${state.scenario.challengeId.replace('_', ' ')}")
    if (victory && state.legacy.scores.overall >= 80) add("Historic Leader · legacy score above 80")
}
