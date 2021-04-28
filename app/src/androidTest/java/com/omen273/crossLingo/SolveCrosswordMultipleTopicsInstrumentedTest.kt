package com.omen273.crossLingo

import org.junit.Test

class SolveCrosswordMultipleTopicsInstrumentedTest: SolveCrossword() {

    @Test(timeout = Constants.TIMEOUT)
    fun solveCrosswordInstrumentedTest() {
        crossword = generateCrossword(chooseTopics = listOf(0,2,4))
        loadFirstCrossword("multiple1")
        solve()
    }
}