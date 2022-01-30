package com.omen273.crossLingo

import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
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
        var starNumber = 50;
        waitForCondition("star checking", { starNumber == readStarsFromConfig() })
        val crossword = generateCrossword()!!
        getContext().openFileOutput(
            crossword.title + GameActivity.CLUE_COUNT_NAME + GameActivity.STATE_SUFFIX,
            AppCompatActivity.MODE_PRIVATE
        ).use {
            Utils.writeInt(it, 9, GameActivity.CLUE_COUNT_NAME)
        }
        loadFirstCrossword()
        val waitingTime = 15300L
        for (i in 0..1) {
            val start = System.currentTimeMillis()
            waitForCondition("", { System.currentTimeMillis() - start > waitingTime },
                waitingTime + 1)
            menuClick(R.string.solve_square_free, R.id.menu_solve_cell)
        }
        // 1st word
        val start = System.currentTimeMillis()
        waitForCondition("", { System.currentTimeMillis() - start > waitingTime },
            waitingTime + 1)
        menuClick(R.string.solve_word_free, R.id.menu_solve_word)

        // 2nd & 3rd words
        for (i in 0..1) {
            val start = System.currentTimeMillis()
            waitForCondition("", { System.currentTimeMillis() - start > waitingTime },
                waitingTime + 1)
            menuClick(R.string.solve_word, R.id.menu_solve_word)
            starNumber -= GameActivity.WORD_OPEN_PRICE
        }

        for (i in 0..1) {
            val start = System.currentTimeMillis()
            waitForCondition("", { System.currentTimeMillis() - start > waitingTime },
                waitingTime + 1)
            menuClick(R.string.solve_square_free, R.id.menu_solve_cell)
        }

        // 4th word
        menuClick(R.string.solve_word, R.id.menu_solve_word)
        starNumber -= GameActivity.WORD_OPEN_PRICE

        val start3 = System.currentTimeMillis()
        waitForCondition("", { System.currentTimeMillis() - start3 > waitingTime },
            waitingTime + 1)
        menuClick(R.string.solve_square, R.id.menu_solve_cell)
        pressBack()
        waitForView(ViewMatchers.withId(R.id.tableLayout))
        starNumber -= GameActivity.LETTER_OPEN_PRICE
        waitForCondition("star checking", { starNumber == readStarsFromConfig() })

        loadFirstCrossword()

        // 5th word
        val start4 = System.currentTimeMillis()
        waitForCondition("", { System.currentTimeMillis() - start4 > waitingTime },
            waitingTime + 1)
        menuClick(R.string.solve_word_free, R.id.menu_solve_word)

        // 8 more words to solve all except one word
        for (i in 0..7)
        {
            menuClick(R.string.solve_word, R.id.menu_solve_word)
            starNumber -= GameActivity.WORD_OPEN_PRICE
        }

        val start5 = System.currentTimeMillis()
        waitForCondition("", { System.currentTimeMillis() - start5 > waitingTime },
            waitingTime + 1)
        menuClick(R.string.solve_word, R.id.menu_solve_word)

        Espresso.onView(ViewMatchers.withText(R.string.youve_solved_the_puzzle))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText(R.string.youve_solved_the_puzzle))
            .inRoot(RootMatchers.isDialog())
        Espresso.onView(ViewMatchers.withText(R.string.another_crossword))
            .inRoot(RootMatchers.isDialog()).perform(ViewActions.click())

        starNumber -= GameActivity.WORD_OPEN_PRICE
        starNumber += GameActivity.BONUS_ON_SOLVE
        waitForCondition("star checking", { starNumber == readStarsFromConfig() })
    }
}
