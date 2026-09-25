import re

path = "app/src/main/java/com/presidentsimulator/game/ui/screens/DiplomacyScreen.kt"
with open(path, "r") as f:
    content = f.read()

imports = [
    "import androidx.compose.ui.window.Dialog",
    "import androidx.compose.foundation.clickable",
    "import androidx.compose.foundation.layout.height",
    "import androidx.compose.ui.unit.sp",
    "import androidx.compose.ui.text.font.FontWeight",
    "import androidx.compose.ui.graphics.Color",
    "import androidx.compose.foundation.shape.RoundedCornerShape",
    "import androidx.compose.ui.draw.clip",
    "import androidx.compose.foundation.background",
    "import androidx.compose.foundation.layout.padding",
    "import androidx.compose.foundation.layout.Row",
    "import androidx.compose.foundation.layout.Column",
    "import androidx.compose.foundation.layout.Spacer",
    "import androidx.compose.material3.Text",
    "com.presidentsimulator.game.ui.theme.NssAccent"
]

for imp in imports:
    if imp not in content:
        content = content.replace("package com.presidentsimulator.game.ui.screens", f"package com.presidentsimulator.game.ui.screens\n{imp}")

with open(path, "w") as f:
    f.write(content)
print("Imports fixed")
