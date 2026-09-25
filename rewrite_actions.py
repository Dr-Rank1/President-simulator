import re

path = "app/src/main/java/com/presidentsimulator/game/viewmodel/GameViewModel.kt"
with open(path, "r") as f:
    content = f.read()

def replace_action(content, func_name, engine_call, success_msg):
    # Regex to find: fun funcName(args) { ... _state.update { engineCall(it, args).also { ... } } }
    # This is tricky because args vary. Let's just do it manually for the known ones.
    pass

# ProposeTradeDeal
old_trade = """        _state.update {
            tradeEngine.proposeTradeDeal(it, partnerCountryId, commodity, amount, type).also { Toast.makeText(getApplication(), "Trade Deal Proposed", Toast.LENGTH_SHORT).show() }
        }"""
new_trade = """        var success = false
        _state.update { old -> 
            val new = tradeEngine.proposeTradeDeal(old, partnerCountryId, commodity, amount, type)
            if (new !== old) success = true
            new
        }
        if (success) Toast.makeText(getApplication(), "Trade Deal Proposed.", Toast.LENGTH_SHORT).show()
        else Toast.makeText(getApplication(), "Trade Deal Failed (Check requirements).", Toast.LENGTH_SHORT).show()"""
content = content.replace(old_trade, new_trade)

# CancelTradeDeal
old_cancel = """_state.update { tradeEngine.cancelTradeDeal(it, dealId).also { Toast.makeText(getApplication(), "Trade Deal Cancelled", Toast.LENGTH_SHORT).show() } }"""
new_cancel = """var success = false
        _state.update { old ->
            val new = tradeEngine.cancelTradeDeal(old, dealId)
            if (new !== old) success = true
            new
        }
        if (success) Toast.makeText(getApplication(), "Trade Deal Cancelled.", Toast.LENGTH_SHORT).show()"""
content = content.replace(old_cancel, new_cancel)

# sendForeignAid
old_aid = """_state.update { diplomacyEngine.sendForeignAid(it, targetCountryId).also { Toast.makeText(getApplication(), "Foreign Aid Sent.", Toast.LENGTH_SHORT).show() } }"""
new_aid = """var success = false
        _state.update { old ->
            val new = diplomacyEngine.sendForeignAid(old, targetCountryId)
            if (new !== old) success = true
            new
        }
        if (success) Toast.makeText(getApplication(), "Foreign Aid Sent.", Toast.LENGTH_SHORT).show()
        else Toast.makeText(getApplication(), "Action Failed (Insufficient Budget/Cooldown).", Toast.LENGTH_SHORT).show()"""
content = content.replace(old_aid, new_aid)

# conductStateVisit
old_visit = """_state.update { diplomacyEngine.conductStateVisit(it, targetCountryId).also { Toast.makeText(getApplication(), "State Visit Completed.", Toast.LENGTH_SHORT).show() } }"""
new_visit = """var success = false
        _state.update { old ->
            val new = diplomacyEngine.conductStateVisit(old, targetCountryId)
            if (new !== old) success = true
            new
        }
        if (success) Toast.makeText(getApplication(), "State Visit Completed.", Toast.LENGTH_SHORT).show()
        else Toast.makeText(getApplication(), "Action Failed (Insufficient Capital/Cooldown).", Toast.LENGTH_SHORT).show()"""
content = content.replace(old_visit, new_visit)

with open(path, "w") as f:
    f.write(content)
