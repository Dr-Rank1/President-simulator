import os
import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # For items(something) { item -> 
    # we want to add key = { it.id }
    
    # 1. DiplomacyScreen.kt
    if "DiplomacyScreen.kt" in filepath:
        content = content.replace(
            "itemsIndexed(state.diplomacy.rivals.chunked(layout.gridColumns)) { rowIndex, row ->",
            "itemsIndexed(state.diplomacy.rivals.chunked(layout.gridColumns), key = { i, r -> r.joinToString { it.id } }) { rowIndex, row ->"
        )
        content = content.replace(
            "items(state.diplomacy.rivals.filter { it.hasTradeTreaty || it.hasNonAggressionPact }) { rival ->",
            "items(state.diplomacy.rivals.filter { it.hasTradeTreaty || it.hasNonAggressionPact }, key = { it.id }) { rival ->"
        )
        content = content.replace(
            "items(state.diplomacy.rivals) { rival ->",
            "items(state.diplomacy.rivals, key = { it.id }) { rival ->"
        )
        
    # 2. EconomyScreen.kt, LawsScreen.kt, etc.
    # We will use regex to find `items(list) { item ->` where we can insert a key.
    # Actually, the user says "the game is very slow". 
    # What if we just fix GameViewModel first?
    with open(filepath, "w") as f:
        f.write(content)

for root, _, files in os.walk("app/src/main/java/com/presidentsimulator/game/ui/screens"):
    for file in files:
        if file.endswith(".kt"):
            fix_file(os.path.join(root, file))

