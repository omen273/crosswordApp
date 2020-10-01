package com.example.crosswordToLearn

//TODO add backspase after solved cell and solved word
import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.akop.ararat.core.Crossword
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

//@RunWith(AndroidJUnit4::class)
class SolveCrosswordWithBackspacesInstrumentedTest {

    @get:Rule
    var activityTestRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    fun printBackspace() {
        onView(withId(R.id.crossword)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_8))
        onView(withId(R.id.crossword)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
    }

    //@Test
    fun solveCrossword() {
        val crossword = generateCrossword()
        loadFirstCrossword()
        val removedCells = mutableListOf<Crossword.Cell>()
        val visited =
            Array(crossword.height) { Array(crossword.width) { false } }
        for (i in 0 until crossword.wordsAcross.size - 1) {
            val word = crossword.wordsAcross[i]
            for ((j, cell) in word.cells.withIndex()) {
                testCell(word, cell.chars)
                visited[word.startRow][word.startColumn + j] = true
            }
        }
        val lastWord = crossword.wordsAcross[crossword.wordsAcross.size - 1]
        testCell(lastWord, lastWord.cells[0].chars)
        printBackspace()
        /*printBackspace()
        for ((i, cell) in lastWord.cells.withIndex()) {
            Utils.testCell(lastWord, cell.chars)
            visited[lastWord.startRow][lastWord.startColumn + i] = true
        }
        for (word in crossword.wordsDown) {
            for ((i, cell) in word.cells.withIndex()) {
                if (!visited[word.startRow + i][word.startColumn]) Utils.testCell(word, cell.chars)
            }
        }*/
        onView(withText(R.string.youve_solved_the_puzzle)).inRoot(isDialog())
            .check(matches(isDisplayed()))
    }
}