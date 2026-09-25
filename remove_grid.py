import re

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/MainDashboardScreen.kt", "r") as f:
    content = f.read()

# find the call to BottomActionGrid and remove it.
# It looks like:
#        BottomActionGrid(
#            onNavigate = onNavigate,
#            modifier = Modifier.padding(top = 8.dp)
#        )
# But we don't know exact.
content = re.sub(r'BottomActionGrid\([^)]*\)', '', content)

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/MainDashboardScreen.kt", "w") as f:
    f.write(content)
