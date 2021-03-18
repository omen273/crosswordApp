package com.example.crosswordToLearn

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.crosswordToLearn.BadCrosswordDataTest
import com.example.crosswordToLearn.GameActivity
import com.example.crosswordToLearn.getContext
import com.example.crosswordToLearn.getTestContext
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class BadCrosswordStateInstrumentedTest : BadCrosswordDataTest() {

    @Rule
    @JvmField
    var timeout: Timeout = Timeout.millis(30000)

    override fun spoil() {
        File(getContext().filesDir, "${crossword.title}${GameActivity.STATE_SUFFIX}").apply {
            FileOutputStream(this).use {
                getTestContext().resources.assets.open("tooLongWordsData.json")
            }
        }
    }

    @Test
    fun badCrosswordStateInstrumentedTest() {
        test()
    }
}