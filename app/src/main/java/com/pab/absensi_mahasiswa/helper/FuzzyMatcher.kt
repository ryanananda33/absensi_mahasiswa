package com.pab.absensi_mahasiswa.helper

import kotlin.math.max

object FuzzyMatcher {
    // Algoritma Levenshtein Distance untuk menghitung skor kemiripan (0.0 - 1.0)
    fun getSimilarityScore(s1: String, s2: String): Double {
        val longer = if (s1.length > s2.length) s1.lowercase() else s2.lowercase()
        val shorter = if (s1.length > s2.length) s2.lowercase() else s1.lowercase()

        if (longer.isEmpty()) return 1.0
        
        val distance = editDistance(longer, shorter)
        return (longer.length - distance).toDouble() / longer.length.toDouble()
    }

    private fun editDistance(s1: String, s2: String): Int {
        val costs = IntArray(s2.length + 1)
        for (i in 0..s1.length) {
            var lastValue = i
            for (j in 0..s2.length) {
                if (i == 0) {
                    costs[j] = j
                } else if (j > 0) {
                    var newValue = costs[j - 1]
                    if (s1[i - 1] != s2[j - 1]) {
                        newValue = max(max(newValue, lastValue), costs[j]) + 1
                    }
                    costs[j - 1] = lastValue
                    lastValue = newValue
                }
            }
            if (i > 0) costs[s2.length] = lastValue
        }
        return costs[s2.length]
    }
}
