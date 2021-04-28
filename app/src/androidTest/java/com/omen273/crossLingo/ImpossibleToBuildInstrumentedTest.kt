package com.omen273.crossLingo

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImpossibleToBuildInstrumentedTest : ChoseTopicsToastTest() {

    @Test(timeout = Constants.TIMEOUT)
    fun impossibleToBuildInstrumentedTest() {
        choseTopicsImpl(
            "impossibleToBuild.json",
            getContext().getString(R.string.choose_other_words)
        )
    }
}