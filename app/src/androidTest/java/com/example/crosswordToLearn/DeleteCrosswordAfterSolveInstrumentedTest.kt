package com.example.crosswordToLearn

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeleteCrosswordAfterSolveInstrumentedTest : SolveCrossword(){

    @Test
    fun solveCrossword() {
        val crossword1 = generateCrossword()
        crossword = generateCrossword()
        loadFirstCrossword()
        solve()
        Espresso.onView(ViewMatchers.withText(R.string.remove))
            .inRoot(RootMatchers.isDialog())
            .perform(ViewActions.click())
        crossword = crossword1
        loadFirstCrossword()
        solve()
    }
}