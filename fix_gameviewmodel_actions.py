import re

path = "app/src/main/java/com/presidentsimulator/game/viewmodel/GameViewModel.kt"
with open(path, "r") as f:
    content = f.read()

helper = """
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
"""

if "fun applyActionWithFeedback" not in content:
    content = content.replace("init {", helper + "\n    init {")

# ProposeResolution
content = re.sub(
    r'_state\.update\s*\{\s*governanceEngine\.proposeResolution\((it|old),\s*(.*?)\)\s*\}',
    r'applyActionWithFeedback("Resolution Proposed.", "Failed: Insufficient Capital or Cooldown.") { governanceEngine.proposeResolution(it, \2) }',
    content
)

# enactLaw
content = re.sub(
    r'_state\.update\s*\{\s*productionLawEngine\.enactLaw\((it|old),\s*(.*?)\)\s*\}',
    r'applyActionWithFeedback("Law Action Processed.", "Failed: Insufficient Capital/Budget.") { productionLawEngine.enactLaw(it, \2) }',
    content
)

# setIdeology
content = re.sub(
    r'_state\.update\s*\{\s*productionLawEngine\.setIdeology\((it|old),\s*(.*?)\)\s*\}(\s*Toast\.makeText[^\n]+)?',
    r'applyActionWithFeedback("Ideology Shifted.", "Failed: Insufficient Budget ($3B required).") { productionLawEngine.setIdeology(it, \2) }',
    content
)

# buildInfrastructure
content = re.sub(
    r'_state\.update\s*\{\s*productionLawEngine\.buildInfrastructure\((it|old),\s*(.*?)\)\s*\}',
    r'applyActionWithFeedback("Infrastructure construction started.", "Failed: Insufficient Budget.") { productionLawEngine.buildInfrastructure(it, \2) }',
    content
)

# formAlliance
content = re.sub(
    r'_state\.update\s*\{\s*diplomacyEngine\.formAlliance\((it|old),\s*(.*?)\)\s*\}',
    r'applyActionWithFeedback("Alliance Formed.", "Failed: Insufficient Capital or Invalid Target.") { diplomacyEngine.formAlliance(it, \2) }',
    content
)

# negotiateTreaty
content = re.sub(
    r'_state\.update\s*\{\s*diplomacyEngine\.negotiateTreaty\((it|old),\s*(.*?)\)\s*\}',
    r'applyActionWithFeedback("Treaty Signed.", "Failed: Insufficient Capital/Relations.") { diplomacyEngine.negotiateTreaty(it, \2) }',
    content
)

# declareWar
content = re.sub(
    r'_state\.update\s*\{\s*diplomacyEngine\.declareWar\((it|old),\s*(.*?)\)\s*\}',
    r'applyActionWithFeedback("War Declared!", "Failed: Invalid Target or Missing Requirements.") { diplomacyEngine.declareWar(it, \2) }',
    content
)

with open(path, "w") as f:
    f.write(content)
print("Actions wrapped.")
