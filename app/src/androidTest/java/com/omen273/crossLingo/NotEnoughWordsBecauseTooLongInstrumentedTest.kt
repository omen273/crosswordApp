package com.omen273.crossLingo

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotEnoughWordsBecauseTooLongInstrumentedTest : BadTopic() {

    @Test
    fun notEnoughWordsBecauseTooLongInstrumentedTest() {
        check(
                "tooLongWordsData.json"
        )
    }
}