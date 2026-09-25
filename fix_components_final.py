with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "r") as f:
    content = f.read()

start = content.find("@Composable\nfun InteractiveWorldMap(")
# cut everything from start to the end
content = content[:start]

new_map = """@Composable
fun InteractiveWorldMap(
    modifier: Modifier = Modifier,
    onOpenDiplomacy: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF031626))
            .clickable { onOpenDiplomacy() }
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.presidentsimulator.game.R.drawable.world_map),
            contentDescription = "World Map",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.35f,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(com.presidentsimulator.game.ui.theme.NssPrimary)
        )
    }
}
"""

content += new_map

with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/CountrySelectScreen.kt", "r") as f:
    cselect = f.read()

if "import androidx.compose.ui.text.style.TextAlign" not in cselect:
    cselect = cselect.replace("import androidx.compose.ui.text.font.FontWeight", "import androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.style.TextAlign")

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/CountrySelectScreen.kt", "w") as f:
    f.write(cselect)
