import re

path = "app/src/main/java/com/presidentsimulator/game/ui/screens/DiplomacyScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Replace the wrong syntax:
wrong = """androidx.compose.foundation.clickable(onClick = { selectedRivalId = null }) {
                                Text("CLOSE", color = NssAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }"""
right = """Text("CLOSE", color = NssAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { selectedRivalId = null })"""

content = content.replace(wrong, right)

# Also fix the import 'com.presidentsimulator.game.ui.theme.NssAccent'
content = content.replace("package com.presidentsimulator.game.ui.screens\ncom.presidentsimulator.game.ui.theme.NssAccent", "package com.presidentsimulator.game.ui.screens\nimport com.presidentsimulator.game.ui.theme.NssAccent")

with open(path, "w") as f:
    f.write(content)
