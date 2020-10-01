package com.example.crosswordToLearn

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.Test


class SolvedCrosswordAfterFillInstrumentedTest : SolveCrossword() {

    @Test
    fun solveCrossword() {
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