package com.omen273.crossLingo

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SolveCrosswordInstrumentedTest : SolveCrossword() {

    @Test
    fun solveCrosswordInstrumentedTest() {
        crossword = generateCrossword(false)
        loadFirstCrossword()
        solve()
    }
}