import os
import re

# 1. PlayableNationCatalog.kt
catalog_path = "app/src/main/java/com/presidentsimulator/game/data/PlayableNationCatalog.kt"
with open(catalog_path, "r") as f:
    catalog_content = f.read()

# Replace all() function
catalog_content = catalog_content.replace(
    "fun all(): List<NationDefinition> = NATIONS + RealCountryCatalog.all.map(::fromRealProfile)",
    "fun all(): List<NationDefinition> = RealCountryCatalog.all.map(::fromRealProfile)"
)

# Replace initialState function to use RealCountryCatalog.all.first()
catalog_content = catalog_content.replace(
    "byId(countryId)?.toInitialGameState() ?: NATIONS.first().toInitialGameState()",
    "byId(countryId)?.toInitialGameState() ?: all().first().toInitialGameState()"
)

# Optional: just leave the NATIONS list and explicit relations as dead code so we don't accidentally break braces.
# But it's safer to just let it be. Wait, wait, no, leaving dead code is fine, but it might be nice to just delete `private val NATIONS` and `explicit` map.

with open(catalog_path, "w") as f:
    f.write(catalog_content)


# 2. GameViewModel.kt
vm_path = "app/src/main/java/com/presidentsimulator/game/viewmodel/GameViewModel.kt"
with open(vm_path, "r") as f:
    vm = f.read()
vm = vm.replace('fun startNewGame(countryId: String = "veltra"', 'fun startNewGame(countryId: String = "us"')
with open(vm_path, "w") as f:
    f.write(vm)

# 3. Models.kt
models_path = "app/src/main/java/com/presidentsimulator/game/data/Models.kt"
with open(models_path, "r") as f:
    models = f.read()
models = models.replace('fun initial(countryId: String = "veltra")', 'fun initial(countryId: String = "us")')
with open(models_path, "w") as f:
    f.write(models)

# 4. GovernanceModels.kt
gov_path = "app/src/main/java/com/presidentsimulator/game/data/GovernanceModels.kt"
with open(gov_path, "r") as f:
    gov = f.read()
gov = gov.replace('val id: String = "veltra"', 'val id: String = "us"')
with open(gov_path, "w") as f:
    f.write(gov)
