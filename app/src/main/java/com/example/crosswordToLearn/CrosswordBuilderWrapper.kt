package com.example.crosswordToLearn

class WordParams(var x: Int, var y: Int, var word: String, var isHorizontal: Boolean)

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
