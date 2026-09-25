import re

# Fix GameTutorialDialog.kt
path = "app/src/main/java/com/presidentsimulator/game/ui/components/GameTutorialDialog.kt"
with open(path, "r") as f:
    content = f.read()

content = content.replace("import com.presidentsimulator.game.ui.theme.NssCardShape", "import com.presidentsimulator.game.ui.theme.NssCardShape\nimport androidx.compose.ui.window.DialogProperties\nimport androidx.compose.ui.window.DialogWindowProvider")
with open(path, "w") as f:
    f.write(content)

# Fix SettingsAudioScreen.kt
path2 = "app/src/main/java/com/presidentsimulator/game/ui/screens/SettingsAudioScreen.kt"
with open(path2, "r") as f:
    content2 = f.read()

content2 = content2.replace("viewModel.triggerTutorial()", "viewModel?.triggerTutorial()")

with open(path2, "w") as f:
    f.write(content2)
