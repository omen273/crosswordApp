package com.example.crosswordToLearn

import android.content.Context
import android.content.Intent
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
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import org.akop.ararat.core.Crossword
import org.akop.ararat.core.buildCrossword
import org.akop.ararat.io.UClickJsonFormatter
import org.hamcrest.*
import org.junit.Rule
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

fun getItemFromCrosswordList(row: Int, column: Int): Matcher<View?> {
    onView(isRoot()).perform(waitForView(withId(R.id.tableLayout)))
    //WORKAROUND: wait some time to load all items in the view
    //TODO wait for certain item loading
    val start = System.currentTimeMillis()
    waitForCondition("", { System.currentTimeMillis() - start > 300 })
    return nthChildOf(nthChildOf(nthChildOf(withId(R.id.tableLayout), row), column), 0)
}

fun generateCrossword(): Crossword {
    chooseGenerateCrossword()
    chooseFirstTopic()
    onView(isRoot()).perform(waitForView(withId(R.id.ok_play)))
    onView(withId(R.id.ok_play)).perform(ViewActions.click())
    Espresso.pressBack()
    Espresso.pressBack()
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
    var last: File? = imagesPath.listFiles()?.iterator()?.next()
    for (file in imagesPath.listFiles()) if (last != null) {
        if (last.lastModified() < file.lastModified()) last = file
    }
    val crosswordName =
        last?.name?.removeSuffix(MainActivity.IMAGE_FORMAT) + GameActivity.DATA_SUFFIX

    return getContext().openFileInput(crosswordName).use {
        buildCrossword { UClickJsonFormatter().read(this, it) }
    }
}

fun isKeyboardShown(): Boolean {
    val inputMethodManager =
        InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
    return inputMethodManager.isAcceptingText
}

fun getContext(): Context = InstrumentationRegistry.getInstrumentation().targetContext

fun getTestContext(): Context = InstrumentationRegistry.getInstrumentation().context

fun readConfig(): Int = GameActivity.readConfig(getContext().filesDir, getContext().resources)

fun chooseGenerateCrossword() {
    onView(getItemFromCrosswordList(0, 0)).perform(ViewActions.click())
}


fun loadFirstCrossword() {
    onView(isRoot()).perform(waitForView(withId(R.id.tableLayout)))
    onView(getItemFromCrosswordList(0, 1)).perform(ViewActions.click())
    onView(isRoot()).perform(waitForView(withId(R.id.crossword)))
    waitForCondition("", { isKeyboardShown() })
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
    val hint = getContext().getString(
        if (word.direction == Crossword.Word.DIR_ACROSS) R.string.across
        else R.string.down, word.number, word.hint
    )
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

open class ChoseTopicsToastTest {

    @get:Rule
    var activityTestRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    private lateinit var scenario: ActivityScenario<ChooseTopicsActivity>

    fun choseTopicsImpl(fileName: String, message: String) {
        Intent(
            ApplicationProvider.getApplicationContext(),
            ChooseTopicsActivity::class.java
        ).apply {
            val data =
                getTestContext().resources.assets.open(fileName)
                    .use { WordsReader().read(it) }
            putExtra(MainActivity.CROSSWORD_DATA_NAME_VARIABLE, data)
            putExtra(MainActivity.CROSSWORD_IMAGE_SIZE_VARIABLE, 0)
        }.also { scenario = ActivityScenario.launch(it) }
        chooseFirstTopic()
        onView(isRoot()).perform(waitForView(withId(R.id.ok_play)))
        onView(withId(R.id.ok_play)).perform(ViewActions.click())
        ToastMatcher.onToast(message).check(
            ViewAssertions.matches(isDisplayed())
        )
    }
}

open class SolveCrossword {

    @get:Rule
    var activityTestRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    protected lateinit var crossword: Crossword

    fun solve() {
        onView(isRoot()).perform(waitForView(withId(R.id.crossword)))
        val visited =
            Array(crossword.height) { Array(crossword.width) { false } }
        for (word in crossword.wordsAcross) {
            for ((j, cell) in word.cells.withIndex()) {
                testCell(word, cell.chars)
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
        onView(withText(R.string.youve_solved_the_puzzle))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(isDisplayed()))
    }
}
