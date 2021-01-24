package com.example.crosswordToLearn

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SolveCrosswordInstrumentedTest : SolveCrossword() {

    @Test
    fun solveCrosswordInstrumentedTest() {
        crossword = generateCrossword()
        loadFirstCrossword()
        solve()
    }
}