package com.example.crosswordToLearn

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SolveCrosswordAfterResetInstrumentedTest : SolveCrossword() {

    @Test
    fun solveCrossword() {
        crossword = generateCrossword()
        loadFirstCrossword()
        solve()
        Espresso.onView(ViewMatchers.withText(R.string.reset))
            .inRoot(RootMatchers.isDialog())
            .perform(ViewActions.click())
        Espresso.pressBack()
        Espresso.pressBack()
        loadFirstCrossword()
        solve()
    }
}