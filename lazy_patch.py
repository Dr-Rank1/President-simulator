import re

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/DiplomacyScreen.kt", "r") as f:
    content = f.read()

# Add imports
content = content.replace("import androidx.compose.ui.Alignment\n",
"""import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
""")

# Replace Column
old_col = """        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nssMinistryScrollPadding()
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {"""

new_col = """        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nssMinistryScrollPadding()
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {"""
content = content.replace(old_col, new_col)

# Replace Relations
old_rel = """                "RELATIONS" -> {
                    RelationsLegend()
                    state.diplomacy.rivals.chunked(layout.gridColumns).forEachIndexed { rowIndex, row ->"""

new_rel = """                "RELATIONS" -> {
                    item { RelationsLegend() }
                    itemsIndexed(state.diplomacy.rivals.chunked(layout.gridColumns)) { rowIndex, row ->"""
content = content.replace(old_rel, new_rel)

old_vis = """                    AnimatedVisibility(
                        visible = selectedRival != null,"""

new_vis = """                    item {
                        AnimatedVisibility(
                            visible = selectedRival != null,"""
content = content.replace(old_vis, new_vis)

old_vis_end = """                        }
                    }
                }"""

new_vis_end = """                        }
                        }
                    }
                }"""
content = content.replace(old_vis_end, new_vis_end)

# Replace Treaties
old_treaty = """                "TREATIES" -> {
                    if (treatyRivals.isEmpty()) {
                        Text("No active treaties. Negotiate with rivals on the Relations tab.", color = NssMutedForeground)
                    } else {
                        treatyRivals.forEach { rival ->"""

new_treaty = """                "TREATIES" -> {
                    if (treatyRivals.isEmpty()) {
                        item { Text("No active treaties. Negotiate with rivals on the Relations tab.", color = NssMutedForeground) }
                    } else {
                        items(treatyRivals) { rival ->"""
content = content.replace(old_treaty, new_treaty)

# Replace Negotiations
old_neg = """                "NEGOTIATIONS" -> {
                    val deals = state.economy.tradeDeals
                    if (deals.isEmpty()) {
                        Text("No active trade negotiations.", color = NssMutedForeground)
                    } else {
                        deals.forEach { deal ->"""
new_neg = """                "NEGOTIATIONS" -> {
                    val deals = state.economy.tradeDeals
                    if (deals.isEmpty()) {
                        item { Text("No active trade negotiations.", color = NssMutedForeground) }
                    } else {
                        items(deals) { deal ->"""
content = content.replace(old_neg, new_neg)

# Wait, check if tradeDeals is in diplomacy or economy
if "state.diplomacy.tradeDeals" in content:
    content = content.replace("state.diplomacy.tradeDeals", "state.economy.tradeDeals")

# Replace Negotiations (if it was diplomacy)
old_neg2 = """                "NEGOTIATIONS" -> {
                    val deals = state.economy.tradeDeals
                    if (deals.isEmpty()) {
                        Text("No active trade negotiations.", color = NssMutedForeground)
                    } else {
                        deals.forEach { deal ->"""
new_neg2 = """                "NEGOTIATIONS" -> {
                    val deals = state.economy.tradeDeals
                    if (deals.isEmpty()) {
                        item { Text("No active trade negotiations.", color = NssMutedForeground) }
                    } else {
                        items(deals) { deal ->"""
content = content.replace(old_neg2, new_neg2)

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/DiplomacyScreen.kt", "w") as f:
    f.write(content)

print("Patch applied.")
