import re

path = "app/src/main/java/com/presidentsimulator/game/ui/components/GameTutorialDialog.kt"
with open(path, "r") as f:
    content = f.read()

content = content.replace("NssCardShape", "RoundedCornerShape(8.dp)")
content = content.replace("import com.presidentsimulator.game.ui.theme.RoundedCornerShape(8.dp)", "")

with open(path, "w") as f:
    f.write(content)
