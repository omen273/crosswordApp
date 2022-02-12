package com.omen273.crossLingo

import kotlin.collections.ArrayList
import kotlin.collections.HashMap

//works only for EN words and RU tips now and clueType equals word
typealias wordsWithTips = HashMap<String, String>
typealias wordsWithTipsByTopic = HashMap<String, wordsWithTips>

class DataTransformer(data: ArrayList<ArrayList<LanguageItem>>) {

    var dataByLevelsByTopics = hashMapOf<String, wordsWithTipsByTopic>()
        private set

    var sortedTopicsByLevel = hashMapOf<String, ArrayList<String>>()
        private set

    private val crosswordLanguage = "EN"
    private val clueLanguage = "RU"

    init {
        for (wordItem in data) {
            val wordItemTr = findWordItem(wordItem)
            val clueItemTr = findClueItem(wordItem)
            if (wordItemTr != null && clueItemTr != null) {
                if (wordItemTr.word.all { it.isLetter() } &&
                    wordItemTr.word.length <= ChooseTopicsActivity.MAX_SIDE &&
                    wordItemTr.word.length > 1) {
                    if (dataByLevelsByTopics[wordItemTr.level] == null) {
                        dataByLevelsByTopics[wordItemTr.level] = hashMapOf()
                    }
                    for (topic in wordItemTr.topics) {
                        if (dataByLevelsByTopics[wordItemTr.level]!![topic] == null) {
                            dataByLevelsByTopics[wordItemTr.level]!![topic] = hashMapOf()
                        }
                        dataByLevelsByTopics[wordItemTr.level]!![topic]!![wordItemTr.word] =
                            clueItemTr.word
                    }
                }
            }
        }

        for (level in dataByLevelsByTopics) {
            dataByLevelsByTopics[level.key] = dataByLevelsByTopics[level.key]!!
                .filterValues { it.size >= ChooseTopicsActivity.CROSSWORD_SIZE }
                    as wordsWithTipsByTopic
        }

        for (level in dataByLevelsByTopics) {
            sortedTopicsByLevel[level.key] =
                ArrayList(dataByLevelsByTopics[level.key]!!.keys.sorted())
        }
    }

    private fun findClueItem(item: ArrayList<LanguageItem>) = item.find {
        it.language == clueLanguage
    }

    private fun findWordItem(item: ArrayList<LanguageItem>) = item.find {
        it.language == crosswordLanguage
    }
}
