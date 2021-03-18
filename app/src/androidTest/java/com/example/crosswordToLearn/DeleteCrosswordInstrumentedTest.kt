package com.example.crosswordToLearn

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)

//This test generates a first crossword. It should be at first position for crosswords, according
// to a game logic. After it, it saves solutions for it. Then it generates a second crossword.
// It should be first instead the first one. The test deletes the second crossword. And solve
// crossword at first position. In case of absence of deletion solution with a high probability
// doesn't fit crossword.
class DeleteCrosswordInstrumentedTest : SolveCrossword() {

    @Rule
    @JvmField
    var timeout: Timeout = Timeout.millis(30000)

    @Test
    fun deleteCrosswordInstrumentedTest() {
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