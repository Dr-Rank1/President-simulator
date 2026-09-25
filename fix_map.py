import re
with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "r") as f:
    content = f.read()

content = content.replace("InteractiveWorldMap(\n    state: GameState,", "InteractiveWorldMap(\n    modifier: Modifier = Modifier,")
content = content.replace("androidx.compose.foundation.Canvas(Modifier.fillMaxSize())", "androidx.compose.foundation.Canvas(modifier.fillMaxSize())")

with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "w") as f:
    f.write(content)
