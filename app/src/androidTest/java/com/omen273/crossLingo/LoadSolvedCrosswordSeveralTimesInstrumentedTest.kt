package com.omen273.crossLingo

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import org.junit.Test

class LoadSolvedCrosswordSeveralTimesInstrumentedTest: SolveCrossword() {

    @Test(timeout = Constants.TIMEOUT)
    fun solveCrosswordInstrumentedTest() {
        crossword = generateCrossword()
        loadFirstCrossword()
        solve()
        Espresso.onView(ViewMatchers.withText(R.string.another_crossword))
            .inRoot(RootMatchers.isDialog())
            .perform(ViewActions.click())
        for(i in 0 .. 2) {
            Espresso.onView(ViewMatchers.isRoot())
                .perform(waitForView(ViewMatchers.withId(R.id.tableLayout)))
            Espresso.onView(getItemFromCrosswordList(0, 1)).perform(ViewActions.click())
            Espresso.onView(ViewMatchers.withText(R.string.another_crossword))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click())
        }
    }
}