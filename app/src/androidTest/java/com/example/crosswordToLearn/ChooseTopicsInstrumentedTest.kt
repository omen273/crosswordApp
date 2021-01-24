package com.example.crosswordToLearn


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.FlakyTest


import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChooseTopicsInstrumentedTest {

    @get:Rule
    var activityTestRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun chooseTopicsInstrumentedTest() {
        chooseGenerateCrossword()
        onView(isRoot()).perform(waitForView(withId(R.id.ok_play)))
        onView(withId(R.id.ok_play)).perform(click())
        onView(withText(R.string.choose_at_least_one_topic)).inRoot(ToastMatcher.isToast()).check(
            matches(isDisplayed())
        )
    }
}
