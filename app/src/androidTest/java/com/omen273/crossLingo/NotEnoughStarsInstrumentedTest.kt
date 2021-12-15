package com.omen273.crossLingo

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.core.AnyOf.anyOf
import org.junit.Test

class NotEnoughStarsInstrumentedTest: TestBaseClass()  {

    private fun useClue(name: Int, id: Int) {
        Espresso.openActionBarOverflowOrOptionsMenu(
                InstrumentationRegistry.getInstrumentation().targetContext
        )
        onView(isRoot()).perform(waitForView(anyOf(withText(name), withId(id))))
        onView(anyOf(withText(name), withId(id))).perform(click())
    }

    @Test
    fun run() {
        generateCrossword(finish = FinishTypeGenerateCrossword.NOTHING)
        for(i in 0..12) useClue(R.string.solve_word, R.id.menu_solve_word)
        Espresso.pressBack()
        generateCrossword(finish = FinishTypeGenerateCrossword.NOTHING)
        for(i in 0..3) useClue(R.string.solve_word, R.id.menu_solve_word)
        onView(withText(getContext().getString(R.string.not_enough_stars_word, "2")))
                        .inRoot(RootMatchers.isDialog())
                        .check(ViewAssertions.matches(isDisplayed()))
        onView(withText(R.string.okButton)).inRoot(RootMatchers.isDialog())
            .perform(click())
        for(i in 0..2) useClue(R.string.solve_square, R.id.menu_solve_cell)
        onView(withText(getContext().getString(R.string.not_enough_stars_square, "0")))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(isDisplayed()))
    }
}
