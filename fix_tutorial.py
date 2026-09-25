import re

path = "app/src/main/java/com/presidentsimulator/game/ui/components/GameTutorialDialog.kt"
with open(path, "r") as f:
    content = f.read()

# Remove the Dialog wrapper and just use a Box
content = re.sub(r'Dialog\([\s\S]*?\) \{', r'Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp, end = 16.dp, start = 16.dp), contentAlignment = Alignment.BottomCenter) {', content)
# Wait, let's just rewrite the GameTutorialDialog function completely!
