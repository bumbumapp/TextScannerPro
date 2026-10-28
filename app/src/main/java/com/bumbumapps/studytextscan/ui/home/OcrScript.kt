package com.bumbumapps.studytextscan.ui.home

/** Writing systems of the bundled ML Kit recognizers, and how to pick one result per scan. */
enum class OcrScript(private val inScript: (Char) -> Boolean) {
    LATIN({ false }),
    DEVANAGARI({ it in '\u0900'..'\u097F' }),
    JAPANESE({ it in '\u3040'..'\u30FF' || it in '\u4E00'..'\u9FFF' }),
    KOREAN({ it in '\uAC00'..'\uD7AF' || it in '\u1100'..'\u11FF' }),
    CHINESE({ it in '\u4E00'..'\u9FFF' });

    fun count(text: String) = text.count(inScript)

    companion object {
        /**
         * The script-specific model whose own characters it found most wins; with none (plain Latin text)
         * the Latin model wins. Japanese beats Chinese when kana are present.
         */
        fun pickBest(results: List<Pair<OcrScript, String>>): OcrScript? {
            if (results.isEmpty()) return null
            val hasKana = results.any { (_, text) -> text.any { it in '\u3040'..'\u30FF' } }
            val scored = results.filter { it.first != LATIN }
                .map { (script, text) -> script to script.count(text) }
                .filter { (script, n) -> n > 0 && !(script == CHINESE && hasKana) }
            return scored.maxByOrNull { it.second }?.first
                ?: results.firstOrNull { it.first == LATIN }?.first
                ?: results.first().first
        }
    }
}
