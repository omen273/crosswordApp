package com.omen273.crossLingo

import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.core.AnyOf
import org.junit.Test

class FreeClueInstrumentedTest : TestBaseClass() {

    private fun menuClick(name: Int, id: Int) {
        Espresso.openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
        Espresso.onView(ViewMatchers.isRoot())
            .perform(waitForView(AnyOf.anyOf(ViewMatchers.withText(name), ViewMatchers.withId(id))))
        Espresso.onView(AnyOf.anyOf(ViewMatchers.withText(name), ViewMatchers.withId(id)))
            .perform(ViewActions.click())
    }

    @Test
    fun freeClueInstrumentedTest() {
        val crossword = generateCrossword()!!
        getContext().openFileOutput(
            crossword.title + GameActivity.CLUE_COUNT_NAME + GameActivity.STATE_SUFFIX,
            AppCompatActivity.MODE_PRIVATE
        ).use {
            Utils.writeInt(it, 3, GameActivity.CLUE_COUNT_NAME)
        }
        loadFirstCrossword()
        val waitingTime = 15300L
        for (i in 0..1) {
            val start = System.currentTimeMillis()
            waitForCondition("", { System.currentTimeMillis() - start > waitingTime },
                waitingTime + 1)
            menuClick(R.string.solve_square_free, R.id.menu_solve_cell)
        }
        val start = System.currentTimeMillis()
        waitForCondition("", { System.currentTimeMillis() - start > waitingTime },
            waitingTime + 1)
        menuClick(R.string.solve_word_free, R.id.menu_solve_word)
        val start1 = System.currentTimeMillis()
        waitForCondition("", { System.currentTimeMillis() - start1 > waitingTime },
            waitingTime + 1)
        menuClick(R.string.solve_square, R.id.menu_solve_cell)
        pressBack()
        waitForView(ViewMatchers.withId(R.id.tableLayout))
        waitForCondition("star checking", { 49 == readStarsFromConfig() })
    }
}
