with open("app/build.gradle.kts", "r") as f:
    content = f.read()

# Enable strong skipping mode explicitly
if "composeCompiler" not in content:
    content += """
composeCompiler {
    enableStrongSkippingMode = true
}
"""
    with open("app/build.gradle.kts", "w") as f:
        f.write(content)
