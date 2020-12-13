package com.example.crosswordToLearn

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotEnoughWordsBecauseTooLongInstrumentedTest : ChoseTopicsToastTest() {

    @Test
    fun choseTopics() {
        choseTopicsImpl(
                "tooLongWordsData.json", getContext().getString(
                R.string.not_enough_words,
                ChooseTopicsActivity.CROSSWORD_SIZE
        )
        )
    }
}