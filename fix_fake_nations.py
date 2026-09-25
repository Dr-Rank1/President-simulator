import re

with open("app/src/main/java/com/presidentsimulator/game/data/PlayableNationCatalog.kt", "r") as f:
    content = f.read()

# Replace the NATIONS block
start = content.find("    val NATIONS = listOf(")
end = content.find("\n    )\n\n    private fun fromRealProfile") + 7
if start != -1:
    content = content[:start] + "    val NATIONS = emptyList<NationDefinition>()\n" + content[end:]

with open("app/src/main/java/com/presidentsimulator/game/data/PlayableNationCatalog.kt", "w") as f:
    f.write(content)
