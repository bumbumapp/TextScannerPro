package com.bumbumapps.studytextscan.ui.detailscan

import com.bumbumapps.studytextscan.Constant.VERTICAL
import org.junit.Assert.assertEquals
import org.junit.Test

class PickContentTest {
    @Test
    fun verticalModeUsesVerticalEditor() {
        assertEquals("v", pickContent(VERTICAL, horizontal = "h", vertical = "v"))
    }

    @Test
    fun otherModesUseHorizontalEditor() {
        assertEquals("h", pickContent("HORIZONTAL", horizontal = "h", vertical = "v"))
        assertEquals("h", pickContent(null, horizontal = "h", vertical = "v"))
    }
}
