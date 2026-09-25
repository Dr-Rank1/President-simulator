import re

filepath = "app/src/main/java/com/presidentsimulator/game/ui/theme/Dimens.kt"
with open(filepath, 'r') as f:
    content = f.read()

def repl_dp(match):
    val = int(match.group(1))
    if val <= 2: return f"{val}.dp"
    new_val = max(1, int(val * 0.75))
    return f"{new_val}.dp"

content = re.sub(r'(\d+)\.dp', repl_dp, content)

# Also shrink theme
filepath_theme = "app/src/main/java/com/presidentsimulator/game/ui/theme/Theme.kt"
with open(filepath_theme, 'r') as f:
    content_theme = f.read()

def repl_sp(match):
    val = int(match.group(1))
    if val <= 8: return f"{val}.sp"
    new_val = max(8, int(val * 0.75))
    return f"{new_val}.sp"

content_theme = re.sub(r'(\d+)\.sp', repl_sp, content_theme)
with open(filepath_theme, 'w') as f:
    f.write(content_theme)

with open(filepath, 'w') as f:
    f.write(content)
