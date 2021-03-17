package com.example.crosswordToLearn

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotEnoughWordsInstrumentedTest : ChoseTopicsToastTest() {

    @Test(timeout = 30000)
    fun notEnoughWordsInstrumentedTest() {
        choseTopicsImpl(
            "notEnoughWords.json", getContext().getString(
                R.string.not_enough_words,
                ChooseTopicsActivity.CROSSWORD_SIZE
            )
        )
    }
}