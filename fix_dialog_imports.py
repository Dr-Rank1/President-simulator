with open("app/src/main/java/com/presidentsimulator/game/ui/components/GameTutorialDialog.kt", "r") as f:
    content = f.read()

if "import androidx.compose.foundation.border" not in content:
    content = content.replace("import androidx.compose.foundation.background", "import androidx.compose.foundation.background\nimport androidx.compose.foundation.border")

if "import com.presidentsimulator.game.ui.theme.NssBorder" not in content:
    content = content.replace("import com.presidentsimulator.game.ui.theme.NssBackground", "import com.presidentsimulator.game.ui.theme.NssBackground\nimport com.presidentsimulator.game.ui.theme.NssBorder")

with open("app/src/main/java/com/presidentsimulator/game/ui/components/GameTutorialDialog.kt", "w") as f:
    f.write(content)
