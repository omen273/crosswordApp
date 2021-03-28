package com.example.crosswordToLearn

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImpossibleToBuildInstrumentedTest : ChoseTopicsToastTest() {

    @Test
    fun impossibleToBuildInstrumentedTest() {
        choseTopicsImpl(
            "impossibleToBuild.json",
            getContext().getString(R.string.choose_other_words)
        )
    }
}