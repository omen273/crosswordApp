package com.example.crosswordToLearn

import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.crosswordToLearn.R.id
import junit.framework.TestCase.assertEquals
import org.akop.ararat.core.Crossword
import org.hamcrest.core.AnyOf.anyOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SolveCrosswordWithTipsInstrumentedTest {

    @get:Rule
    var activityTestRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    private fun menuClick(name: Int, id: Int, price: Int): Int {
        var stars = readConfig()
        openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
        onView(isRoot()).perform(waitForView(anyOf(withText(name), withId(id))))
        onView(anyOf(withText(name), withId(id))).perform(click())
        stars -= price
        pressBack()
        pressBack()
        loadFirstCrossword()
        assertEquals(stars, readConfig())
        return stars
    }

    @Test
    fun solveCrossword() {
        val crossword = generateCrossword()
        loadFirstCrossword()
        val visited =
            Array(crossword.height) { Array(crossword.width) { false } }
        for (word in crossword.wordsAcross) {
            for ((i, cell) in word.cells.withIndex()) {
                testCell(word, cell.chars)
                visited[word.startRow][word.startColumn + i] = true
            }
        }
        val downWordSolved = 0
        val vertical =
            mutableListOf<Pair<Crossword.Cell, Crossword.Word>>()
        for ((i, word) in crossword.wordsDown.withIndex()) {
            if (i != downWordSolved) {
                for ((j, cell) in word.cells.withIndex()) {
                    if (!visited[word.startRow + j][word.startColumn]) vertical.add(
                        Pair(
                            cell,
                            word
                        )
                    )
                }
            }
        }
        menuClick(R.string.solve_word, id.menu_solve_word, GameActivity.WORD_OPEN_PRICE)
        val middle = vertical.size / 2
        val verticalBefore = vertical.subList(0, vertical.size / 2)
        for ((cell, word) in verticalBefore) testCell(word, cell.chars)
        menuClick(R.string.solve_square, id.menu_solve_cell, GameActivity.LETTER_OPEN_PRICE)
        val stars = menuClick(R.string.solve_square, id.menu_solve_cell, GameActivity.LETTER_OPEN_PRICE)
        val verticalAfter = vertical.subList(middle + 2, vertical.size)
        for ((cell, word) in verticalAfter) testCell(word, cell.chars)
        onView(withText(R.string.youve_solved_the_puzzle)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.youve_solved_the_puzzle)).inRoot(isDialog())
        onView(withText(R.string.another_crossword)).inRoot(isDialog()).perform(click())
        assertEquals(stars + GameActivity.BONUS_ON_SOLVE, readConfig())
    }
}
