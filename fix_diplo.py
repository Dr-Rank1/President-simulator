with open("app/src/main/java/com/presidentsimulator/game/ui/screens/DiplomacyScreen.kt", "r") as f:
    content = f.read()

# Add imports
content = content.replace("import androidx.compose.ui.Alignment\n",
"""import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
""")

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

old_rel = """                "RELATIONS" -> {
                    RelationsLegend()
                    state.diplomacy.rivals.chunked(layout.gridColumns).forEachIndexed { rowIndex, row ->"""

new_rel = """                "RELATIONS" -> {
                    item { RelationsLegend() }
                    itemsIndexed(state.diplomacy.rivals.chunked(layout.gridColumns)) { rowIndex, row ->"""
content = content.replace(old_rel, new_rel)

old_vis = """                    AnimatedVisibility(
                        visible = selectedRival != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {"""

new_vis = """                    item {
                        AnimatedVisibility(
                            visible = selectedRival != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut(),
                        ) {"""
content = content.replace(old_vis, new_vis)

old_vis_end = """                        }
                    }
                }

                "TREATIES" -> {"""

new_vis_end = """                        }
                        }
                    }
                }

                "TREATIES" -> {"""
content = content.replace(old_vis_end, new_vis_end)

old_treat = """                "TREATIES" -> {
                    state.diplomacy.rivals.filter { it.hasTradeTreaty || it.hasNonAggressionPact }.forEach { rival ->"""

new_treat = """                "TREATIES" -> {
                    items(state.diplomacy.rivals.filter { it.hasTradeTreaty || it.hasNonAggressionPact }) { rival ->"""
content = content.replace(old_treat, new_treat)

old_neg = """                "NEGOTIATIONS" -> {
                    state.diplomacy.rivals.forEach { rival ->"""

new_neg = """                "NEGOTIATIONS" -> {
                    items(state.diplomacy.rivals) { rival ->"""
content = content.replace(old_neg, new_neg)

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/DiplomacyScreen.kt", "w") as f:
    f.write(content)
