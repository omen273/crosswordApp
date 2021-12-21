package com.omen273.crossLingo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class BadCrosswordDataInstrumentedTest : BadCrosswordDataTest() {

    override fun spoil() {
        File(getContext().filesDir, "${crossword.title}${GameActivity.DATA_SUFFIX}").apply {
            FileOutputStream(this).use {
                getTestContext().resources.assets.open("tooLongWordsData.json")
            }
        }
    }

    @Test
    fun badCrosswordDataInstrumentedTest() {
        test()
    }
}