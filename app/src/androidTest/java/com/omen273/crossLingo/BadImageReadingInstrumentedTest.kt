package com.omen273.crossLingo

import android.content.Intent
import android.os.Environment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class BadImageReadingInstrumentedTest {

    @Rule
    @JvmField
    val retryTestRule = RetryTestRule()

    @Before
    fun addBadData() {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val name = "bad"
            val path = File(getContext().getExternalFilesDir(null), MainActivity.IMAGE_DIRECTORY)
            if (!path.exists()) path.mkdir()
            File(path, "${name}${MainActivity.IMAGE_FORMAT}").apply {
                FileOutputStream(this).use {
                    getTestContext().resources.assets.open("impossibleToBuild.json")
                }
            }
            File(getContext().filesDir, "$name${GameActivity.DATA_SUFFIX}").apply {
                FileOutputStream(this).use {
                    getTestContext().resources.assets.open("tooLongWordsData.json")
                }
            }
            File(getContext().filesDir, "$name${GameActivity.STATE_SUFFIX}").apply {
                FileOutputStream(this).use {
                    getTestContext().resources.assets.open("tooLongWordsData.json")
                }
            }
        }
        else{
            throw Exception("Media is not mounted")
        }
    }

    private lateinit var scenario: ActivityScenario<ChooseTopicsActivity>

    @Test
    fun badImageReadingInstrumentedTest(){
        Intent(
                ApplicationProvider.getApplicationContext(),
                MainActivity::class.java
        ).also { scenario = ActivityScenario.launch(it) }
        val start = System.currentTimeMillis()
        waitForCondition("", { System.currentTimeMillis() - start > 300 })
        setLevelImpl()
        ToastMatcher.onToast(R.string.damaged_data).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )
    }
}