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
            .perform(waitForView(ViewMatchers.withId(R.id.move_cursor_to_filled_cell)))
        if(!SettActivity.readPrintToFilledCellsFromConfig(getContext().filesDir, getContext().resources))
            Espresso.onView(ViewMatchers.withId(R.id.move_cursor_to_filled_cell))
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