import re
import os

screens_dir = "app/src/main/java/com/presidentsimulator/game/ui/screens/"
components_dir = "app/src/main/java/com/presidentsimulator/game/ui/components/"

def shrink_file(filepath):
    if not os.path.exists(filepath): return
    with open(filepath, 'r') as f:
        content = f.read()
    
    # Simple regex to find .sp and .dp and reduce them by 20-30%
    def repl_sp(match):
        val = int(match.group(1))
        # Reduce by roughly 25%
        new_val = max(8, int(val * 0.75))
        return f"{new_val}.sp"
        
    def repl_dp(match):
        val = int(match.group(1))
        # Keep 1.dp as 1.dp
        if val <= 1: return f"{val}.dp"
        new_val = max(1, int(val * 0.75))
        return f"{new_val}.dp"
        
    # We'll only replace integer dp and sp.
    # Exclude files where we don't want to shrink (e.g. MainDashboard, BottomNav)
    if "GlobalHud" in filepath or "MainDashboard" in filepath or "BottomNav" in filepath or "CountrySelect" in filepath:
        return
        
    content = re.sub(r'(\d+)\.sp', repl_sp, content)
    content = re.sub(r'(\d+)\.dp', repl_dp, content)
    
    with open(filepath, 'w') as f:
        f.write(content)

for root, _, files in os.walk(screens_dir):
    for file in files:
        if file.endswith(".kt"):
            shrink_file(os.path.join(root, file))
            
for root, _, files in os.walk(components_dir):
    for file in files:
        if file.endswith(".kt"):
            shrink_file(os.path.join(root, file))

print("Shrunk all sizes by 25%")
