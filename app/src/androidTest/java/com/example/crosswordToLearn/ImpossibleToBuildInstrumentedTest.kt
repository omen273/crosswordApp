package com.example.crosswordToLearn

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImpossibleToBuildInstrumentedTest : ChoseTopicsToastTest() {

    @Rule
    @JvmField
    var timeout: Timeout = Timeout.millis(30000)

    @Test
    fun impossibleToBuildInstrumentedTest() {
        choseTopicsImpl(
            "impossibleToBuild.json",
            getContext().getString(R.string.choose_other_words)
        )
    }
}