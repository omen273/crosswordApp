package com.omen273.crossLingo

import org.junit.Test

class SolveCrosswordMultipleTopicsInstrumentedTest: SolveCrossword() {

    @Test
    fun solveCrosswordInstrumentedTest() {
        crossword = generateCrossword(chooseTopics = listOf(0,2,4))!!
        loadFirstCrossword("multiple " + "(C1) " + '1')
        solve()
    }
}