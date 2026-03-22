package com.bodyquest.app.util

object XpCalculator {
    fun xpForNextLevel(currentLevel: Int): Int = currentLevel * 100

    fun calculateNewLevel(currentLevel: Int, currentXp: Int, xpGained: Int): Pair<Int, Int> {
        var level = currentLevel
        var xp = currentXp + xpGained
        var needed = xpForNextLevel(level)

        while (xp >= needed) {
            xp -= needed
            level++
            needed = xpForNextLevel(level)
        }

        return Pair(level, xp)
    }
}
