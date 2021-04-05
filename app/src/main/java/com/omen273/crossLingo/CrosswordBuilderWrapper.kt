package com.omen273.crossLingo

class WordParams{
    var x: Int = 0
    var y: Int = 0
    var word: String = ""
    var isHorizontal: Boolean = false
}

class CrosswordParams {
    var width: Int = 0
    var height: Int = 0
    var words: ArrayList<WordParams> = arrayListOf()
    fun addWord(params: WordParams): Boolean = words.add(params)
}

class CrosswordBuilderWrapper {

    external fun getCrossword(
        words: ArrayList<String>,
        wordCount: Int,
        maxSideSize: Int,
        maxTime: Int
    ): CrosswordParams

    companion object {
        init {
            System.loadLibrary("crosswordBuilder")
        }
    }
}
