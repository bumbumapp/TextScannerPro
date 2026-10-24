package com.bumbumapps.studytextscan

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Card #66: the minified release build crashed on launch because R8 full mode removed the generic
 * signature that ViewBindingKotlinModel reads via reflection. The keep rules must stay.
 */
class ProguardRulesTest {
    private val rules = File("proguard-rules.pro").readText()

    @Test
    fun keepsGenericSignaturesOfViewBindingModels() {
        assertTrue(rules.contains("-keepattributes Signature"))
        assertTrue(rules.contains("class * extends com.bumbumapps.studytextscan.epoxy.ViewBindingKotlinModel"))
        assertTrue(rules.contains("public static *** bind(android.view.View);"))
    }
}
