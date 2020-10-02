package com.example.crosswordToLearn

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeleteCrosswordInstrumentedTest : SolveCrossword() {

    @Test
    fun solveCrossword() {
        crossword = generateCrossword()
        generateCrossword()
        Espresso.onView(ViewMatchers.isRoot())
            .perform(waitForView(ViewMatchers.withId(R.id.tableLayout)))
        Espresso.onView(getItemFromCrosswordList(0, 1)).perform(ViewActions.longClick())
        Espresso.onView(ViewMatchers.withText(R.string.yes))
            .inRoot(RootMatchers.isDialog())
            .perform(ViewActions.click())
        loadFirstCrossword()
        solve()
    }

}