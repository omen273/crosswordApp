package com.example.crosswordToLearn

import android.os.Environment
import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class DeleteCrosswordAfterSolveInstrumentedTest : SolveCrossword(){

    @Before
    fun addBadData() {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val path = File(getContext().getExternalFilesDir(null), MainActivity.IMAGE_DIRECTORY)
            if (!path.exists()) path.mkdir()
            val path2 = File(getContext().getExternalFilesDir(null), "TEST")
            val res = path2.mkdir()
            Log.i("WRITE", "Result of writing star: ${res}")
            
            File(path, "star.png").apply {
                FileOutputStream(this).use {
                    getTestContext().resources.assets.open("star.png")
                }
            }
        }
    }

    @Test
    fun deleteCrosswordAfterSolveInstrumentedTest() {
        val crossword1 = generateCrossword()
        crossword = generateCrossword()
        loadFirstCrossword()
        solve()
        Espresso.onView(ViewMatchers.withText(R.string.remove))
            .inRoot(RootMatchers.isDialog())
            .perform(ViewActions.click())
        crossword = crossword1
        loadFirstCrossword()
        solve()
    }
}