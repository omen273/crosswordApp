package com.omen273.crossLingo

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import org.junit.Test

class CheckStarCountAfterRemovingInstrumentedTest: SolveCrossword() {

    @Test
    fun solveCrosswordInstrumentedTest() {
        val START_STAR_NAMBER = 50
        waitForCondition("Stars number checking", {START_STAR_NAMBER  == readConfig()})
        crossword = generateCrossword()
        loadFirstCrossword()
        solve()
        Espresso.onView(ViewMatchers.withText(R.string.remove))
            .inRoot(RootMatchers.isDialog())
            .perform(ViewActions.click())
        val number = START_STAR_NAMBER + GameActivity.BONUS_ON_SOLVE
        waitForCondition("Stars number checking", {number == readConfig()})
    }
}