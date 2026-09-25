with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "r") as f:
    content = f.read()

import re
# We'll replace the InteractiveWorldMap function with a highly optimized one.
# First find the boundaries of the function.
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
    val paths = androidx.compose.runtime.remember {
        val shapes = listOf(
            listOf(.08f to .22f, .13f to .13f, .22f to .16f, .28f to .25f, .25f to .37f, .19f to .42f, .14f to .34f, .09f to .34f),
            listOf(.28f to .44f, .35f to .47f, .38f to .59f, .35f to .75f, .31f to .86f, .28f to .71f, .26f to .56f),
            listOf(.45f to .25f, .49f to .20f, .55f to .23f, .58f to .31f, .54f to .37f, .49f to .35f),
            listOf(.48f to .39f, .56f to .37f, .60f to .49f, .57f to .68f, .52f to .79f, .47f to .63f, .45f to .49f),
            listOf(.59f to .20f, .70f to .14f, .83f to .19f, .93f to .29f, .88f to .43f, .78f to .45f, .70f to .38f, .62f to .39f),
            listOf(.78f to .62f, .85f to .59f, .91f to .66f, .88f to .75f, .81f to .73f),
        )
        shapes.map { points ->
            androidx.compose.ui.graphics.Path().apply {
                // The points are relative, we need to scale them in onDraw.
                // Wait, Path is scaleable if we just store the points and create the path in remember based on 1x1, then scale the canvas!
                moveTo(points.first().first, points.first().second)
                points.drop(1).forEach { lineTo(it.first, it.second) }
                close()
            }
        }
    }
    
    Box(modifier.clickable { onOpenDiplomacy() }) {
        androidx.compose.foundation.Canvas(modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            for (i in 1..8) {
                val x = w * i / 8f
                drawLine(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f), androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, h), 1.dp.toPx())
            }
            for (i in 1..12) {
                val y = h * i / 12f
                drawLine(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(w, y), 1.dp.toPx())
            }
            
            androidx.compose.ui.graphics.drawscope.withTransform({
                scale(w, h, androidx.compose.ui.geometry.Offset.Zero)
            }) {
                paths.forEachIndexed { index, path ->
                    val color = if (index % 2 == 0) com.presidentsimulator.game.ui.theme.NssPrimary.copy(alpha = 0.3f) else com.presidentsimulator.game.ui.theme.NssPrimary.copy(alpha = 0.2f)
                    drawPath(path, color)
                    // The stroke needs inverse scaling if we scale the canvas, so just draw stroke unscaled.
                }
            }
            
            // Draw stroke unscaled
            paths.forEachIndexed { index, path ->
                val scaledPath = androidx.compose.ui.graphics.Path().apply {
                    addPath(path)
                    transform(androidx.compose.ui.graphics.Matrix().apply { scale(w, h, 1f) })
                }
                drawPath(scaledPath, com.presidentsimulator.game.ui.theme.NssPrimary.copy(alpha = 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
            }
            
            listOf(.20f to .27f, .33f to .58f, .52f to .29f, .53f to .51f, .76f to .29f, .85f to .67f).forEachIndexed { index, point ->
                drawCircle(if (index == 3) com.presidentsimulator.game.ui.theme.NssAccent else com.presidentsimulator.game.ui.theme.NssSky, 3.dp.toPx(), androidx.compose.ui.geometry.Offset(point.first * w, point.second * h))
            }
        }
    }
}
"""

content = content[:start] + new_map + content[end+2:]

with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "w") as f:
    f.write(content)
