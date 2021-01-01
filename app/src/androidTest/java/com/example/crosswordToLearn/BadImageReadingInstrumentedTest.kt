package com.example.crosswordToLearn

import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class BadImageReadingInstrumentedTest : SolveCrossword() {

    @Test
    fun test(){
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val name = "bad"
            val path = File(getContext().getExternalFilesDir(null), MainActivity.IMAGE_DIRECTORY)
            if (!path.exists()) path.mkdir()
            File(path, "$name.${MainActivity.IMAGE_FORMAT}").apply {
                FileOutputStream(this).use {
                    getTestContext().resources.assets.open("tooLongWordsData.json")
                }
            }
            File(getContext().filesDir, "$name.${GameActivity.DATA_SUFFIX}").apply {
                FileOutputStream(this).use {
                    getTestContext().resources.assets.open("tooLongWordsData.json")
                }
            }
            File(getContext().filesDir, "$name.${GameActivity.STATE_SUFFIX}").apply {
                FileOutputStream(this).use {
                    getTestContext().resources.assets.open("tooLongWordsData.json")
                }
            }
        }
        crossword = generateCrossword()
        loadFirstCrossword()
        solve()
        crossword = generateCrossword()
        loadFirstCrossword()
        solve()
    }
}