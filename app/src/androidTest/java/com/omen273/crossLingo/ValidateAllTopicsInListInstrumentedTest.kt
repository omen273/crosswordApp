package com.omen273.crossLingo

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class ValidateAllTopicsInListInstrumentedTest {

    @get:Rule
    var activityTestRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    private var level = getContext().resources.getStringArray(R.array.levels).iterator()

    @Before
    fun setLevel() {
        if(!level.hasNext()) throw Exception("Empty levels")
        setLevelImpl(level.next())
    }

    @Test
    fun validate() {
        while(level.hasNext()){
            var topicPosition = 0
            chooseGenerateCrossword()
            var end = false
            while (!end) {
                val attemptNumber = 10
                for (k in 0 until attemptNumber) {
                    try {
                        waitForView(withId(R.id.topicList), 100)
                        Espresso.onView(nthChildOf(withId(R.id.topicList), topicPosition))
                            .perform(ViewActions.scrollTo())
                        .perform(ViewActions.click())}
                    catch(e:Exception) {
                        end = true
                        Espresso.pressBack()
                        break
                    }
                    Espresso.onView(ViewMatchers.isRoot()).perform(waitForView(withId(R.id.ok_play)))
                    Espresso.onView(withId(R.id.ok_play)).perform(ViewActions.click())
                    waitForView(withId(R.id.crossword))
                    waitForView(ViewMatchers.withContentDescription(R.string.abc_action_bar_up_description))
                    Espresso.onView(ViewMatchers.withContentDescription(R.string.abc_action_bar_up_description))
                        .perform(ViewActions.click())
                    chooseGenerateCrossword()
                }
                ++topicPosition
            }
            waitForView(ViewMatchers.withContentDescription(R.string.settings))
            Espresso.onView(ViewMatchers.withContentDescription(R.string.settings))
                .perform(ViewActions.click())
            waitForView(withId(R.id.level_list))
            Espresso.onView(withId(R.id.level_list)).perform(ViewActions.click())
            Espresso.onView(withText(level.next())).perform(ViewActions.click())
            waitForView(ViewMatchers.withContentDescription(R.string.abc_action_bar_up_description))
            Espresso.onView(ViewMatchers.withContentDescription(R.string.abc_action_bar_up_description))
                .perform(ViewActions.click())
        }
    }
}
