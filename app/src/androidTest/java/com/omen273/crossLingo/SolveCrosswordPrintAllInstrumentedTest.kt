package com.omen273.crossLingo

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import org.junit.Before
import org.junit.Test

class SolveCrosswordPrintAllInstrumentedTest: SolveCrossword() {

    @Before
    fun setPrintAll() {
        Espresso.onView(ViewMatchers.withContentDescription(R.string.settings))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.isRoot())
            .perform(waitForView(ViewMatchers.withId(R.id.move_selection_to_solved_squares)))
        if(!SettActivity.readMoveSelectionToSolvedSquares(getContext().filesDir, getContext().resources))
            Espresso.onView(ViewMatchers.withId(R.id.move_selection_to_solved_squares))
                .perform(ViewActions.click())
        val start = System.currentTimeMillis()
        waitForCondition("", { System.currentTimeMillis() - start > 300 })
        Espresso.onView(ViewMatchers.withContentDescription(R.string.abc_action_bar_up_description))
            .perform(ViewActions.click())
    }

    @Test
    fun solveCrosswordInstrumentedTest() {
        crossword = generateCrossword(false)
        loadFirstCrossword()
        solve(printAllLetters = true, actionChecking = true)
    }
}