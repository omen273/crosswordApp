package com.omen273.crossLingo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
@SdkSuppress(maxSdkVersion = 29)
class BadCrosswordStateInstrumentedTest : BadCrosswordDataTest() {

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