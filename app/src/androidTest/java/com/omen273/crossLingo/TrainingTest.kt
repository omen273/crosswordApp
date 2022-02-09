package com.omen273.crossLingo

import android.view.View
import android.widget.TextView
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.omen273.crossLingo.R.id
import junit.framework.TestCase.assertEquals
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.AnyOf.anyOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrainingTest: TestBaseClass() {

    private fun loadTraining(
        chooseTopics: List<Int> = listOf(0)
    ): List<String> {
        val topics = mutableListOf<String>()
        onView(getItemFromCrosswordList(0, 1)).perform(ViewActions.click())
        onView(isRoot()).perform(waitForView(withId(R.id.topicList)))
        for (i in chooseTopics) {
            topics.add(nthChildOf(withId(R.id.topicList), i).toString())
            onView(nthChildOf(withId(R.id.topicList), i)).perform(ViewActions.click())
        }
        onView(isRoot()).perform(waitForView(withId(R.id.ok_play)))
        onView(withId(R.id.ok_play)).perform(ViewActions.click())
        onView(isRoot()).perform(waitForView(withId(R.id.crossword)))
        return topics
    }

    private fun menuClick(name: Int, id: Int) {
        openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
        onView(isRoot()).perform(waitForView(anyOf(withText(name), withId(id))))
        onView(anyOf(withText(name), withId(id))).perform(click())
    }

    private fun matchTopics(parentMatcher: Matcher<View?>, topics: List<String>): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun matchesSafely(view: View?): Boolean {
                val hint = view as TextView
                for (topic in topics)
                {
                    if (hint.text.endsWith(topic)) return true;
                }
                return false;
            }

            override fun describeTo(description: Description?) {
                description?.appendText("check $topics.toString() in hints")
            }
        }
    }

    @Test
    fun solveTrainingTest() {
        loadTraining(listOf(2,4))
        var stars = readConfig()
        for (i in 0..13) {
            menuClick(R.string.solve_square_free, id.menu_solve_cell)
            menuClick(R.string.solve_word_free, id.menu_solve_word)
        }

        onView(withText(R.string.train_the_same_topics)).inRoot(isDialog()).perform(click())

        for (i in 0..13) {
            onView(withId(R.id.hint)).perform(waitForView(matchTopics(withId(R.id.hint), listOf("[dress]", "[food]"))))
            menuClick(R.string.solve_word_free, id.menu_solve_word)
        }

        assertEquals(stars, readConfig())
    }
}
