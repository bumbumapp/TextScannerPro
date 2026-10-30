package com.bumbumapps.studytextscan.ui.home

import com.bumbumapps.studytextscan.R
import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {
    @Test
    fun greetingFollowsTimeOfDay() {
        assertEquals(R.string.greet_evening, greetingRes(4))
        assertEquals(R.string.greet_morning, greetingRes(5))
        assertEquals(R.string.greet_morning, greetingRes(11))
        assertEquals(R.string.greet_afternoon, greetingRes(12))
        assertEquals(R.string.greet_afternoon, greetingRes(17))
        assertEquals(R.string.greet_evening, greetingRes(18))
    }
}
