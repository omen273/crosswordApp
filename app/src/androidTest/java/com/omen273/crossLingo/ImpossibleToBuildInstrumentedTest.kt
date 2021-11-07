package com.omen273.crossLingo

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(maxSdkVersion = 29)
class ImpossibleToBuildInstrumentedTest : TestBaseClass() {
    private lateinit var scenario: ActivityScenario<ChooseTopicsActivity>

    @Test
    fun impossibleToBuildInstrumentedTest() {
        Intent(
                ApplicationProvider.getApplicationContext(),
                ChooseTopicsActivity::class.java
        ).apply {
            val transformer = DataTransformer(
                getTestContext().resources.assets.open("impossibleToBuild.json")
                    .use { WordsReader().read(it,
                        fun(level: String){Utils.
                        validateLevel(getContext().resources, level)}) })
            val data = transformer.dataByLevelsByTopics
            val topics = transformer.sortedTopicsByLevel
            putExtra(MainActivity.CROSSWORD_DATA_NAME_VARIABLE, data)
            putExtra(MainActivity.CROSSWORD_TOPICS_NAME_VARIABLE, topics)
        }.also { scenario = ActivityScenario.launch(it) }
        chooseFirstTopic()
        Espresso.onView(ViewMatchers.isRoot()).perform(waitForView(ViewMatchers.withId(R.id.ok_play)))
        Espresso.onView(ViewMatchers.withId(R.id.ok_play)).perform(ViewActions.click())
        ToastMatcher.onToast(getContext().getString(R.string.choose_other_words)).check(
                ViewAssertions.matches(ViewMatchers.isDisplayed())
        )
    }
}