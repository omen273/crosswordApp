package com.example.crosswordToLearn

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotEnoughWordsBecauseTooShortInstrumentedTests : ChoseTopicsToastTest() {

    @Test
    fun choseTopics() {
        choseTopicsImpl(
                "tooShortWordsData.json", getContext().getString(
                R.string.not_enough_words,
                ChooseTopicsActivity.CROSSWORD_SIZE
        )
        )
    }
}