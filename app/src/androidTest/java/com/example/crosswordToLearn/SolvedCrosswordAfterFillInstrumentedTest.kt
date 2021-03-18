package com.example.crosswordToLearn

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SolvedCrosswordAfterFillInstrumentedTest : SolveCrossword() {

    @Rule
    @JvmField
    var timeout: Timeout = Timeout.millis(30000)

    @Test
    fun solvedCrosswordAfterFillInstrumentedTest() {
        crossword = generateCrossword()
        loadFirstCrossword()
        val length = crossword.wordsDown.fold(0, { acc, word -> acc + word.cells.size }) +
            crossword.wordsAcross.fold(0, { acc, word -> acc + word.cells.size })
        for (i in 0 until length) onView(withId(R.id.crossword)).perform(
            ViewActions.typeTextIntoFocusedView(",")
        )
        solve()
    }
}