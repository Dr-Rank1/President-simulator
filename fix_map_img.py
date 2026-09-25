with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "r") as f:
    content = f.read()

import re
start = content.find("@Composable\nfun InteractiveWorldMap(")
end = content.find("}\n", start)
while content[end:end+2] != "}\n":
    end = content.find("}\n", end + 1)
end = content.find("}\n", end + 1)
end = content.find("}\n", end + 1)

new_map = """@Composable
fun InteractiveWorldMap(
    modifier: Modifier = Modifier,
    onOpenDiplomacy: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF031626)) // Ocean color matching dark theme
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

content = content[:start] + new_map + content[end+2:]

with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "w") as f:
    f.write(content)
