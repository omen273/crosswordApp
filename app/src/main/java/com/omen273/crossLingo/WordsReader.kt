package com.omen273.crossLingo

import android.os.Parcelable
import android.util.JsonReader
import kotlinx.android.parcel.Parcelize
import java.io.InputStream
import java.nio.charset.Charset

@Parcelize
data class LanguageItem(
    var language: String, var word: String,
    var topics: ArrayList<String>, var questions: ArrayList<String>,
    var level:String
) : Parcelable

class WordsReader {
    fun read(inputStream: InputStream, levelValidator : (level: String) -> Unit): ArrayList<ArrayList<LanguageItem>> =
        with(
            JsonReader(
                inputStream.bufferedReader
                    (Charset.forName(MainActivity.DEFAULT_ENCODING))
            )
        ) {
            val words = arrayListOf<ArrayList<LanguageItem>>()
            beginArray()
            while (hasNext()) {
                beginArray()
                val wordItem = arrayListOf<LanguageItem>()
                while (hasNext()) {
                    beginObject()
                    val languageItem = LanguageItem(
                        "", "",
                        arrayListOf(), arrayListOf(), ""
                    )
                    languageItem.language = nextName()
                    beginObject()
                    while (hasNext()) {
                        when (val tag = nextName()) {
                            "word" -> {
                                val word = nextString()
                                languageItem.word = word
                            }
                            "topics" -> {
                                beginArray()
                                while (hasNext()) {
                                    val topic = nextString()
                                    if (topic.isNotEmpty()) languageItem.topics.add(topic)
                                }
                                endArray()
                            }
                            "questions" -> {
                                beginArray()
                                while (hasNext()) {
                                    languageItem.questions.add(nextString())
                                }
                                endArray()
                            }
                            "level" -> {
                                val level = nextString()
                                levelValidator(level)
                                languageItem.level = level
                            }
                            else -> throw RuntimeException("The wrong json tag: $tag")
                        }
                    }
                    endObject()
                    endObject()
                    if (languageItem.topics.isNotEmpty() && languageItem.word.length > 1
                            && languageItem.word.length <= ChooseTopicsActivity.MAX_SIDE &&
                            languageItem.word.all { it.isLetter() }) {
                        wordItem.add(languageItem)
                    }
                }
                endArray()
                if (wordItem.isNotEmpty()) words.add(wordItem)
            }
            endArray()
            words
        }
}
