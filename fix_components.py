with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "a") as f:
    f.write("""

fun formatCompactMoney(value: Long): String {
    if (value >= 1_000_000_000_000L) return String.format("$%.1fT", value / 1_000_000_000_000.0)
    if (value >= 1_000_000_000L) return String.format("$%.1fB", value / 1_000_000_000.0)
    if (value >= 1_000_000L) return String.format("$%.1fM", value / 1_000_000.0)
    if (value >= 1_000L) return String.format("$%.1fK", value / 1_000.0)
    return "$$value"
}

fun formatCompactMil(value: Long): String {
    if (value >= 1_000_000_000L) return String.format("%.1fB", value / 1_000_000_000.0)
    if (value >= 1_000_000L) return String.format("%.1fM", value / 1_000_000.0)
    if (value >= 1_000L) return String.format("%.1fK", value / 1_000.0)
    return value.toString()
}
""")
