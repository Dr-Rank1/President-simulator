file_path = "app/src/main/java/com/presidentsimulator/game/viewmodel/GameViewModel.kt"

with open(file_path, "r") as f:
    content = f.read()

replacements = {
    "_state.update { advancementEngine.changeStateReligion(it, religion) }": "_state.update { advancementEngine.changeStateReligion(it, religion) }\n        Toast.makeText(getApplication(), \"State Religion updated.\", Toast.LENGTH_SHORT).show()"
}

for old, new in replacements.items():
    if old in content:
        content = content.replace(old, new)
        print(f"Replaced {old.strip()[:30]}")
    else:
        print(f"NOT FOUND: {old.strip()[:30]}")

with open(file_path, "w") as f:
    f.write(content)
