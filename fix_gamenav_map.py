import re

with open("app/src/main/java/com/presidentsimulator/game/ui/navigation/GameNavigation.kt", "r") as f:
    content = f.read()

# Replace Row { NavHost } with Row { Box { InteractiveWorldMap; NavHost } }
content = content.replace(
    'Row(modifier = Modifier.weight(1f).fillMaxSize()) {',
    'Row(modifier = Modifier.weight(1f).fillMaxSize()) {\n            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).fillMaxHeight()) {\n                com.presidentsimulator.game.ui.components.InteractiveWorldMap(state = state, modifier = Modifier.fillMaxSize())'
)

# And then fix the end of NavHost:
content = content.replace(
    '            }\n            MinistryBottomNav(',
    '            }\n            }\n            MinistryBottomNav('
)

with open("app/src/main/java/com/presidentsimulator/game/ui/navigation/GameNavigation.kt", "w") as f:
    f.write(content)

