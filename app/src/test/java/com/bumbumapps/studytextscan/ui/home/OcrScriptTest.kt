package com.bumbumapps.studytextscan.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class OcrScriptTest {
    private val latin = "Photosynthesis is the process by which"

    @Test
    fun latinTextUsesOnlyTheLatinResult() {
        val results = OcrScript.values().map { it to latin }
        assertEquals(OcrScript.LATIN, OcrScript.pickBest(results))
    }

    @Test
    fun hindiTextUsesDevanagari() {
        val results = listOf(OcrScript.LATIN to "ab", OcrScript.DEVANAGARI to "नमस्ते दुनिया", OcrScript.CHINESE to "ab")
        assertEquals(OcrScript.DEVANAGARI, OcrScript.pickBest(results))
    }

    @Test
    fun japaneseWithKanaBeatsChinese() {
        val results = listOf(OcrScript.JAPANESE to "光合成とは植物", OcrScript.CHINESE to "光合成植物植物")
        assertEquals(OcrScript.JAPANESE, OcrScript.pickBest(results))
    }

    @Test
    fun koreanTextUsesKorean() {
        val results = listOf(OcrScript.LATIN to "x", OcrScript.KOREAN to "광합성은 식물이")
        assertEquals(OcrScript.KOREAN, OcrScript.pickBest(results))
    }

    @Test
    fun noResults() {
        assertEquals(null, OcrScript.pickBest(emptyList()))
    }
}
