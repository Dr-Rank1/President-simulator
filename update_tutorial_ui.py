import re

path = "app/src/main/java/com/presidentsimulator/game/ui/components/GameTutorialDialog.kt"
with open(path, "r") as f:
    content = f.read()

# I want to add some animation and a nicer shadow.
# I'll just change the tutorial background from Color(0xF5050A0F) to something a bit more professional, maybe adding a glow effect.
# Or just use NssCardShape with a thicker border.
