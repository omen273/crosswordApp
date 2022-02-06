package com.omen273.crossLingo

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.*
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import org.akop.ararat.core.Crossword
import org.akop.ararat.core.buildCrossword
import org.akop.ararat.io.UClickJsonFormatter
import org.hamcrest.*
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runners.model.Statement
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.TimeoutException

fun nthChildOf(parentMatcher: Matcher<View?>, childPosition: Int): Matcher<View?> {
    return object : TypeSafeMatcher<View?>() {
        override fun matchesSafely(view: View?): Boolean {
            if (view?.parent !is ViewGroup) {
                if (view != null) {
                    return parentMatcher.matches(view.parent)
                }
                return false
            }
            val group = view.parent as ViewGroup
            return parentMatcher.matches(view.parent) &&
                    group.getChildAt(childPosition) == view
        }

        override fun describeTo(description: Description?) {
            description?.appendText("with $childPosition child view of type parentMatcher")
        }
    }
}

fun hasNChildren(matcher: Matcher<View?>, childrenNumber: Int): Matcher<View?> {
    return object : TypeSafeMatcher<View?>() {
        override fun matchesSafely(view: View?): Boolean {
            if (view !is ViewGroup) {
                return false
            }
            return matcher.matches(view) &&
                    view.childCount == childrenNumber
        }

        override fun describeTo(description: Description?) {
            description?.appendText("The view doesn't have $childrenNumber children")
        }
    }
}

fun getItemFromCrosswordList(row: Int, column: Int, name: String? = null): Matcher<View?> {
    onView(isRoot()).perform(waitForView(withId(R.id.tableLayout)))
    //WORKAROUND: wait some time to load all items in the view
    //TODO wait for certain item loading
    val start = System.currentTimeMillis()
    waitForCondition("", { System.currentTimeMillis() - start > 300 })
    if (name != null) {
        onView(
            nthChildOf(
                nthChildOf(nthChildOf(withId(R.id.tableLayout), row), column),
                1
            )
        ).perform(waitForView(withText(name)))
    }
    return nthChildOf(nthChildOf(nthChildOf(withId(R.id.tableLayout), row), column), 0)
}

enum class FinishTypeGenerateCrossword { PRESS_HOME, PRESS_BACK, NOTHING }

fun generateCrossword(
    finish: FinishTypeGenerateCrossword = FinishTypeGenerateCrossword.PRESS_BACK,
    chooseTopics: List<Int> = listOf(0)
): Crossword? {
    chooseGenerateCrossword()
    onView(isRoot()).perform(waitForView(withId(R.id.topicList)))
    for (i in chooseTopics) {
        onView(nthChildOf(withId(R.id.topicList), i)).perform(ViewActions.click())
    }
    onView(isRoot()).perform(waitForView(withId(R.id.ok_play)))
    onView(withId(R.id.ok_play)).perform(ViewActions.click())
    onView(isRoot()).perform(waitForView(withId(R.id.crossword)))
    when (finish) {
        FinishTypeGenerateCrossword.PRESS_BACK -> Espresso.pressBack()
        FinishTypeGenerateCrossword.PRESS_HOME ->
            onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(
                ViewActions.click()
            )
        FinishTypeGenerateCrossword.NOTHING -> return null
    }
    return getLastCrossword()
}

fun chooseFirstTopic() {
    onView(isRoot()).perform(waitForView(withId(R.id.topicList)))
    onView(nthChildOf(withId(R.id.topicList), 0)).perform(ViewActions.click())
}

fun getLastCrossword(): Crossword {
    val imagesPath = File(
        getContext().getExternalFilesDir(null),
        MainActivity.IMAGE_DIRECTORY
    )
    val last: File = imagesPath.listFiles()?.maxByOrNull { it.lastModified() }
        ?: throw RuntimeException("Screen shoot doesn't exist")
    val crosswordName =
        last.name.removeSuffix(MainActivity.IMAGE_FORMAT) + GameActivity.DATA_SUFFIX
    return getContext().openFileInput(crosswordName).use {
        buildCrossword { UClickJsonFormatter().read(this, it) }
    }
}

fun getContext(): Context = InstrumentationRegistry.getInstrumentation().targetContext

fun getTestContext(): Context = InstrumentationRegistry.getInstrumentation().context

fun readConfig(): Int =
    GameActivity.readStarNumberFromConfig(getContext().filesDir, getContext().resources)

fun chooseGenerateCrossword() {
    onView(getItemFromCrosswordList(0, 0)).perform(ViewActions.click())
}

fun loadFirstCrossword(name: String? = null) {
    onView(isRoot()).perform(waitForView(withId(R.id.tableLayout)))
    onView(getItemFromCrosswordList(1, 0, name)).perform(ViewActions.click())
    onView(isRoot()).perform(waitForView(withId(R.id.crossword)))
}

fun waitForCondition(reason: String, condition: Callable<Boolean>, timeout: Long = 10000) {
    val end = System.currentTimeMillis() + timeout

    try {
        while (!condition.call()) {
            if (System.currentTimeMillis() > end) {
                throw AssertionError(reason)
            }

            Thread.sleep(16)
        }
    } catch (e: Exception) {
        throw e
    }
}

fun waitForView(
    viewMatcher: Matcher<View>, timeout: Long = 10000,
    waitForDisplayed: Boolean = true
): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return Matchers.any(View::class.java)
        }

        override fun getDescription(): String {
            val matcherDescription = StringDescription()
            viewMatcher.describeTo(matcherDescription)
            return "wait for a specific view <$matcherDescription> to be ${
                if (waitForDisplayed)
                    "displayed" else "not displayed during $timeout millis."
            }"
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()
            val startTime = System.currentTimeMillis()
            val endTime = startTime + timeout
            val visibleMatcher = isDisplayed()

            do {
                val viewVisible = TreeIterables.breadthFirstViewTraversal(view)
                    .any { viewMatcher.matches(it) && visibleMatcher.matches(it) }

                if (viewVisible == waitForDisplayed) return
                uiController.loopMainThreadForAtLeast(50)
            } while (System.currentTimeMillis() < endTime)

            // Timeout happens.
            throw PerformException.Builder()
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(view))
                .withCause(TimeoutException())
                .build()
        }
    }
}

fun testCell(word: Crossword.Word, ch: String) {
    val hint = getContext().getString(R.string.tip, word.number, word.hint, word.citation)
    onView(isRoot()).perform(waitForView(withText(hint)))
    onView(withId(R.id.hint)).check(ViewAssertions.matches(withText(hint)))
    onView(withId(R.id.crossword)).perform(
        ViewActions.typeTextIntoFocusedView(ch)
    )
}

class ToastMatcher :
    TypeSafeMatcher<Root>() {

    override fun describeTo(description: Description) {
        description.appendText("is toast")
    }

    override fun matchesSafely(root: Root): Boolean {
        val type = root.windowLayoutParams.get().type
        @Suppress("DEPRECATION")
        if (type == WindowManager.LayoutParams.TYPE_TOAST ||
            type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        ) {
            val windowToken = root.decorView.windowToken
            val appToken = root.decorView.applicationWindowToken
            if (windowToken === appToken) return true
        }
        return false
    }

    companion object {
        fun onToast(text: String): ViewInteraction =
            onView(withText(text)).inRoot(isToast())!!

        fun onToast(textId: Int): ViewInteraction =
            onView(withText(textId)).inRoot(isToast())!!

        fun isToast(): Matcher<Root> {
            return ToastMatcher()
        }
    }
}

class RetryTestRule(val retryCount: Int = 3) : TestRule {

    override fun apply(base: Statement?, description: org.junit.runner.Description?): Statement {
        return statement(base, description)
    }

    private fun statement(base: Statement?, description: org.junit.runner.Description?): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                var caughtThrowable: Throwable? = null

                // implement retry logic here
                for (i in 0 until retryCount) {
                    try {
                        base?.evaluate()
                        return
                    } catch (t: Throwable) {
                        caughtThrowable = t
                        if (description != null) {
                            Log.e("ERROR", description.displayName + ": run " + (i + 1) + " failed")
                        }
                    }
                }

                if (description != null) {
                    Log.e(
                        "ERROR",
                        description.displayName + ": giving up after " + retryCount + " failures"
                    )
                }
                throw caughtThrowable!!
            }
        }
    }
}

open class BadTopic : TestBaseClass() {
    private lateinit var scenario: ActivityScenario<ChooseTopicsActivity>

    fun check(fileName: String) {
        Intent(
            ApplicationProvider.getApplicationContext(),
            ChooseTopicsActivity::class.java
        ).apply {
            val transformer = DataTransformer(
                getTestContext().resources.assets.open(fileName)
                    .use {
                        WordsReader().read(it,
                            fun(level: String) {
                                Utils.validateLevel(getContext().resources, level)
                            })
                    })
            val data = transformer.dataByLevelsByTopics
            val topics = transformer.sortedTopicsByLevel
            putExtra(MainActivity.CROSSWORD_DATA_NAME_VARIABLE, data)
            putExtra(MainActivity.CROSSWORD_TOPICS_NAME_VARIABLE, topics)
        }.also { scenario = ActivityScenario.launch(it) }
        val topicList = withId(R.id.topicList)
        onView(isRoot()).perform(waitForView(topicList))
        onView(topicList).check(ViewAssertions.matches(hasNChildren(topicList, 1)))
    }
}

fun setLevelImpl(level: String = "advanced(C1)") {
    var configLevel: String? = "test"
    configLevel =
        ChooseTopicsActivity.readLevelFromConfig(getContext().filesDir, getContext().resources)
    waitForCondition("", { configLevel != "test" }, 300)
    if (configLevel == null) {
        onView(withText(level))
            .inRoot(RootMatchers.isDialog())
            .perform(ViewActions.click())
    }
}

open class TestBaseClass {

    @get:Rule
    var activityTestRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    val retryTestRule = RetryTestRule()

    @Before
    fun setLevel() {
        setLevelImpl()
    }
}

open class SolveCrossword : TestBaseClass() {

    protected lateinit var crossword: Crossword

    fun solve(printAllLetters: Boolean = false, actionChecking: Boolean = false) {
        onView(isRoot()).perform(waitForView(withId(R.id.crossword)))
        val visited =
            Array(crossword.height) { Array(crossword.width) { false } }
        for (word in crossword.wordsAcross) {
            for ((j, cell) in word.cells.withIndex()) {
                testCell(word, cell.chars)
                if (!printAllLetters)
                    visited[word.startRow][word.startColumn + j] = true
            }
        }
        for (word in crossword.wordsDown) {
            for ((j, cell) in word.cells.withIndex()) {
                if (!visited[word.startRow + j][word.startColumn]) {
                    testCell(word, cell.chars)
                }
            }
        }
        //it doesn't work sometimes for R.string.youve_solved_the_puzzle
        //printAllLetter mode
        if (actionChecking)
            onView(withText(R.string.remove)).inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click())
        else
            onView(withText(R.string.youve_solved_the_puzzle))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(isDisplayed()))
    }
}

abstract class BadCrosswordDataTest : TestBaseClass() {

    abstract fun spoil()
    lateinit var crossword: Crossword

    fun test() {
        crossword = generateCrossword()!!
        val start = System.currentTimeMillis()
        waitForCondition("", { System.currentTimeMillis() - start > 300 })
        spoil()
        val start1 = System.currentTimeMillis()
        waitForCondition("", { System.currentTimeMillis() - start1 > 300 })
        onView(isRoot()).perform(waitForView(withId(R.id.tableLayout)))
        onView(getItemFromCrosswordList(1, 0)).perform(ViewActions.click())
        ToastMatcher.onToast(R.string.damaged_data).check(
            ViewAssertions.matches(isDisplayed())
        )
    }
}
