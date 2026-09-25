with open("app/src/main/java/com/presidentsimulator/game/ui/navigation/GameNavigation.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.compose.foundation.layout.Box(", "Box(")
content = content.replace("InteractiveWorldMap(state = state, modifier", "InteractiveWorldMap(modifier")

with open("app/src/main/java/com/presidentsimulator/game/ui/navigation/GameNavigation.kt", "w") as f:
    f.write(content)
