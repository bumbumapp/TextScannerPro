package com.bumbumapps.studytextscan.ui.home

import com.bumbumapps.studytextscan.service.textFilter.TextFilterService
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ScanTextFromImageUseCase(
    private val filterService: TextFilterService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    // Latin first; each script-specific model also reads Latin, so their results must not be merged.
    private val recognizers = listOf(
        OcrScript.LATIN to TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS),
        OcrScript.DEVANAGARI to TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build()),
        OcrScript.JAPANESE to TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build()),
        OcrScript.KOREAN to TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build()),
        OcrScript.CHINESE to TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build()),
    )

    suspend operator fun invoke(image: InputImage) = withContext(dispatcher) {
        val results = recognizers.mapNotNull { (script, recognizer) ->
            val result = recognizer.process(image).await()
            if (result.text.isBlank()) null else Triple(script, result.text, result)
        }
        val best = OcrScript.pickBest(results.map { it.first to it.second })
        val chosen = results.firstOrNull { it.first == best }
        val chips = mutableSetOf<Pair<String, String>>()
        chosen?.third?.textBlocks?.forEach { block ->
            block.lines.forEach { line ->
                line.elements.forEach { element ->
                    chips.addAll(filterService.filterTextForEmails(element.text))
                    chips.addAll(filterService.filterTextForPhoneNumbers(element.text))
                    chips.addAll(filterService.filterTextForLinks(element.text))
                }
            }
        }
        (chosen?.second ?: "") to chips.toList()
    }
}
