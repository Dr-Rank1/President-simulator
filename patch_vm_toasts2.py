file_path = "app/src/main/java/com/presidentsimulator/game/viewmodel/GameViewModel.kt"

with open(file_path, "r") as f:
    content = f.read()

replacements = {
    "_state.update { OppositionEngine.concedePlatform(it) }": "_state.update { OppositionEngine.concedePlatform(it) }\n        Toast.makeText(getApplication(), \"Concessions made to opposition.\", Toast.LENGTH_SHORT).show()",
    "_state.update { diplomacyEngine.declareWar(it, targetCountryId, warGoal) }": "_state.update { diplomacyEngine.declareWar(it, targetCountryId, warGoal) }\n        Toast.makeText(getApplication(), \"WAR DECLARED!\", Toast.LENGTH_LONG).show()",
    "_state.update { diplomacyEngine.negotiateTreaty(it, targetCountryId, type) }": "_state.update { diplomacyEngine.negotiateTreaty(it, targetCountryId, type) }\n        Toast.makeText(getApplication(), \"Treaty Negotiated.\", Toast.LENGTH_SHORT).show()",
    "_state.update { diplomacyEngine.breakTreaty(it, targetCountryId, type) }": "_state.update { diplomacyEngine.breakTreaty(it, targetCountryId, type) }\n        Toast.makeText(getApplication(), \"Treaty Broken!\", Toast.LENGTH_SHORT).show()",
    "_state.update { productionLawEngine.setIdeology(it, ideology) }": "_state.update { productionLawEngine.setIdeology(it, ideology) }\n        Toast.makeText(getApplication(), \"State Ideology updated.\", Toast.LENGTH_SHORT).show()",
    "_state.update { governanceEngine.formAlliance(it, name, invitees) }": "_state.update { governanceEngine.formAlliance(it, name, invitees) }\n        Toast.makeText(getApplication(), \"Alliance Formed.\", Toast.LENGTH_SHORT).show()"
}

for old, new in replacements.items():
    if old in content:
        content = content.replace(old, new)
        print(f"Replaced {old.strip()[:30]}")
    else:
        print(f"NOT FOUND: {old.strip()[:30]}")

with open(file_path, "w") as f:
    f.write(content)
