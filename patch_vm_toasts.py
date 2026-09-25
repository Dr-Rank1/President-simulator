import re

file_path = "app/src/main/java/com/presidentsimulator/game/viewmodel/GameViewModel.kt"

with open(file_path, "r") as f:
    content = f.read()

# Make sure Toast is imported
if "import android.widget.Toast" not in content:
    content = content.replace("import android.content.Context", "import android.content.Context\nimport android.widget.Toast")

def add_toast(method_signature, toast_message):
    global content
    
    # We find the method signature, and insert the toast just inside the method body.
    # The methods are like:
    # fun negotiateWithOpposition() {
    #     if (_currentActiveEvent.value != null) return
    
    # or
    # fun proposeTradeDeal(...) {
    
    # Let's just find the exact body.
    
    # First, let's locate the method block start.
    match = re.search(re.escape(method_signature) + r'\s*\{(.*?)\}', content, re.DOTALL)
    if not match:
        print(f"Could not find {method_signature}")
        return
        
    block = match.group(1)
    # We want to add the toast right before closing or after the update.
    # Actually, let's just replace `_state.update { ... }` with `_state.update { ... }\nToast.makeText(getApplication(), "...", Toast.LENGTH_SHORT).show()`
    
    # Since regex can be tricky with nested {}, let's just do a string replacement on a known line inside the method.
    pass

# A simpler way:
# We just replace `_state.update { OppositionEngine.negotiate(it) }` with:
# `_state.update { OppositionEngine.negotiate(it) }; Toast.makeText(getApplication(), "Negotiations held.", Toast.LENGTH_SHORT).show()`

replacements = {
    "_state.update { OppositionEngine.negotiate(it) }": "_state.update { OppositionEngine.negotiate(it) }\n        Toast.makeText(getApplication(), \"Negotiations held. Stability changed.\", Toast.LENGTH_SHORT).show()",
    "_state.update { OppositionEngine.smear(it) }": "_state.update { OppositionEngine.smear(it) }\n        Toast.makeText(getApplication(), \"Smear campaign initiated.\", Toast.LENGTH_SHORT).show()",
    "_state.update { OppositionEngine.concede(it) }": "_state.update { OppositionEngine.concede(it) }\n        Toast.makeText(getApplication(), \"Concessions made to opposition.\", Toast.LENGTH_SHORT).show()",
    "_state.update { it.copy(society = it.society.copy(stateReligion = religion)) }": "_state.update { it.copy(society = it.society.copy(stateReligion = religion)) }\n        Toast.makeText(getApplication(), \"State Religion updated.\", Toast.LENGTH_SHORT).show()",
    "_state.update { it.copy(society = it.society.copy(ideology = ideology)) }": "_state.update { it.copy(society = it.society.copy(ideology = ideology)) }\n        Toast.makeText(getApplication(), \"State Ideology updated.\", Toast.LENGTH_SHORT).show()",
    "tradeEngine.proposeTradeDeal(it, partnerCountryId, commodity, amount, type)": "tradeEngine.proposeTradeDeal(it, partnerCountryId, commodity, amount, type).also { Toast.makeText(getApplication(), \"Trade Deal Proposed\", Toast.LENGTH_SHORT).show() }",
    "tradeEngine.cancelTradeDeal(it, dealId)": "tradeEngine.cancelTradeDeal(it, dealId).also { Toast.makeText(getApplication(), \"Trade Deal Cancelled\", Toast.LENGTH_SHORT).show() }",
    "diplomacyEngine.breakTreaty(it, partnerCountryId, type)": "diplomacyEngine.breakTreaty(it, partnerCountryId, type).also { Toast.makeText(getApplication(), \"Treaty Broken!\", Toast.LENGTH_SHORT).show() }",
    "diplomacyEngine.negotiateTreaty(it, partnerCountryId, type)": "diplomacyEngine.negotiateTreaty(it, partnerCountryId, type).also { Toast.makeText(getApplication(), \"Treaty Negotiated.\", Toast.LENGTH_SHORT).show() }",
    "diplomacyEngine.sendForeignAid(it, targetCountryId)": "diplomacyEngine.sendForeignAid(it, targetCountryId).also { Toast.makeText(getApplication(), \"Foreign Aid Sent.\", Toast.LENGTH_SHORT).show() }",
    "diplomacyEngine.conductStateVisit(it, targetCountryId)": "diplomacyEngine.conductStateVisit(it, targetCountryId).also { Toast.makeText(getApplication(), \"State Visit Completed.\", Toast.LENGTH_SHORT).show() }",
    "diplomacyEngine.declareWar(it, targetCountryId, goal)": "diplomacyEngine.declareWar(it, targetCountryId, goal).also { Toast.makeText(getApplication(), \"WAR DECLARED!\", Toast.LENGTH_LONG).show() }",
    "diplomacyEngine.formAlliance(it, allianceName, memberIds)": "diplomacyEngine.formAlliance(it, allianceName, memberIds).also { Toast.makeText(getApplication(), \"Alliance Formed.\", Toast.LENGTH_SHORT).show() }"
}

for old, new in replacements.items():
    if old in content:
        content = content.replace(old, new)
        print(f"Replaced {old.strip()[:30]}")
    else:
        print(f"NOT FOUND: {old.strip()[:30]}")

with open(file_path, "w") as f:
    f.write(content)

