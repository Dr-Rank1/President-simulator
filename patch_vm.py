import re

with open("app/src/main/java/com/presidentsimulator/game/viewmodel/GameViewModel.kt", "r") as f:
    content = f.read()

# Fix advanceTimeTick to run in a background coroutine
old_advance = """    fun advanceTimeTick() {
        if (_currentActiveEvent.value != null) return
        if (_state.value.gameOver.isGameOver) return
        if (_turnSummary.value != null) return
        if (_missionResults.value.isNotEmpty()) return
        if (_warOutcome.value != null) return
        if (_state.value.agenda.needsBriefing) return
        if (_state.value.demographics.election.hasPendingNight) return

        val before = _state.value
        val beforeMissions = before.espionage.activeMissions.associateBy { it.id }
        val beforeUnlocked = before.research.unlockedTechIds.toSet()
        val beforeActiveLaws = before.legal.activeLawIds.toSet()
        val beforePending = before.legal.pendingLaws.map { it.lawId }.toSet()

        _state.update { current ->
            if (current.gameOver.isGameOver) return@update current
            monthlyPipeline.advance(current)
        }

        val after = _state.value"""

new_advance = """    fun advanceTimeTick() {
        if (_currentActiveEvent.value != null) return
        if (_state.value.gameOver.isGameOver) return
        if (_turnSummary.value != null) return
        if (_missionResults.value.isNotEmpty()) return
        if (_warOutcome.value != null) return
        if (_state.value.agenda.needsBriefing) return
        if (_state.value.demographics.election.hasPendingNight) return

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val before = _state.value
            val beforeMissions = before.espionage.activeMissions.associateBy { it.id }
            val beforeUnlocked = before.research.unlockedTechIds.toSet()
            val beforeActiveLaws = before.legal.activeLawIds.toSet()
            val beforePending = before.legal.pendingLaws.map { it.lawId }.toSet()

            val advancedState = monthlyPipeline.advance(before)
            _state.value = advancedState

            val after = advancedState"""

content = content.replace(old_advance, new_advance)

# Need to close the coroutine at the end of advanceTimeTick
# Find the end of advanceTimeTick
# Wait, let's just find the last line of advanceTimeTick which is:
#         _state.update { it.copy(agenda = it.agenda.copy(needsBriefing = newBriefing != null), currentBriefing = newBriefing) }
#     }

old_end = """        val newBriefing = if (isFirstMonth) {
            BriefingType.FIRST_DAY
        } else if (after.month == 1) {
            BriefingType.YEAR_START
        } else null
        _state.update { it.copy(agenda = it.agenda.copy(needsBriefing = newBriefing != null), currentBriefing = newBriefing) }
    }"""

new_end = """        val newBriefing = if (isFirstMonth) {
            BriefingType.FIRST_DAY
        } else if (after.month == 1) {
            BriefingType.YEAR_START
        } else null
        _state.value = _state.value.copy(agenda = _state.value.agenda.copy(needsBriefing = newBriefing != null), currentBriefing = newBriefing)
        } // end launch
    }"""

content = content.replace(old_end, new_end)

with open("app/src/main/java/com/presidentsimulator/game/viewmodel/GameViewModel.kt", "w") as f:
    f.write(content)
