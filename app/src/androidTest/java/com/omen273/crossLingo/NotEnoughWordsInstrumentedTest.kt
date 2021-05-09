package com.omen273.crossLingo

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotEnoughWordsInstrumentedTest : BadTopic() {

    @Test
    fun notEnoughWordsInstrumentedTest() {
        check(
            "notEnoughWords.json"
        )
    }
}