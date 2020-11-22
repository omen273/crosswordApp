package com.example.crosswordToLearn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.CheckBox
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_choose_topics.*

class ChooseTopicsActivity : AppCompatActivity() {
    private val crosswordLanguage = "EN"
    private val clueLanguage = "RU"
    private val clueType = ClueType.WORD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_topics)
        val topicNames = getTopics()
        topics.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val wordsInList = getChosenTopics()
                topicList.removeAllViews()
                for (topic in wordsInList.sorted()) {
                    with(CheckBox(this@ChooseTopicsActivity)) {
                        text = topic
                        toggle()
                        topicList.addView(this)
                    }
                }
                for (topic in topicNames) {
                    if (!wordsInList.contains(topic) && topic.startsWith(p0.toString())) {
                        with(CheckBox(this@ChooseTopicsActivity)) {
                            text = topic
                            topicList.addView(this)
                        }
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        ok_play.setOnClickListener {
            with(Intent(this, GameActivity::class.java))
            {
                val chosenTopics = getChosenTopics()
                when {
                    chosenTopics.isNotEmpty() -> {
                        val chosenWords = getWords(chosenTopics)
                        when {
                            chosenWords.size >= CROSSWORD_SIZE -> {
                                putExtra(WORDS_VARIABLE, chosenWords)
                                putExtra(
                                    MainActivity.CROSSWORD_NAME_VARIABLE,
                                    if (chosenTopics.size == 1) getChosenTopics().iterator()
                                        .next() else NAME_FOR_CROSSWORD_WITH_MULTIPLE_TOPICS
                                )
                                putExtra(MainActivity.CROSSWORD_IS_GENERATED_VARIABLE, true)
                                putExtra(
                                    MainActivity.CROSSWORD_IMAGE_SIZE_VARIABLE,
                                    extras?.getInt(MainActivity.CROSSWORD_IMAGE_SIZE_VARIABLE)
                                )
                                topicList.removeAllViews()
                                startActivityForResult(this, MainActivity.ACTIVITY_GAME)
                            }
                            else -> {
                                Toast.makeText(
                                    this@ChooseTopicsActivity, getString(
                                        R.string.not_enough_words,
                                        CROSSWORD_SIZE
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    else -> {
                        Toast.makeText(
                            this@ChooseTopicsActivity, R.string.choose_at_least_one_topic,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MainActivity.ACTIVITY_GAME) {
            when (resultCode) {
                MainActivity.ACTIVITY_GAME_OK -> {
                    setResult(MainActivity.ACTIVITY_GAME_OK)
                    finish()
                }
                MainActivity.ACTIVITY_GAME_FAIL -> {
                    Toast.makeText(
                        this@ChooseTopicsActivity, R.string.choose_other_words, Toast.LENGTH_SHORT
                    ).show()
                }
                MainActivity.ACTIVITY_GAME_REMOVE -> {
                    setResult(MainActivity.ACTIVITY_GAME_REMOVE)
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (topicList.childCount == 0) {
            //TODO checking of data changing
            for (topic in getTopics()) {
                with(CheckBox(this@ChooseTopicsActivity)) {
                    text = topic
                    topicList.addView(this)
                }
            }
        }
    }

    @Suppress("unused")
    enum class ClueType { WORD, QUESTION }

    private fun getWords(topics: HashSet<String>): HashMap<String, String> {
        val data = intent.extras?.get("data") as ArrayList<*>
        val words = hashMapOf<String, String>()
        for (wordItem in data) {
            val item = wordItem as ArrayList<*>
            val wordItemTr = item.find {
                val itemTr = it as LanguageItem
                itemTr.language == crosswordLanguage
            } as LanguageItem?
            val clueItemTr = item.find {
                val itemTr = it as LanguageItem
                itemTr.language == clueLanguage &&
                    (clueType == ClueType.WORD || itemTr.questions.isNotEmpty())
            } as LanguageItem?
            if (wordItemTr != null && clueItemTr != null &&
                wordItemTr.topics.find { it1 -> topics.find { it == it1 } != null } != null &&
                wordItemTr.word.all { it.isLetter() } && wordItemTr.word.length <= MAX_SIDE &&
                wordItemTr.word.length > 1
            ) {
                words[wordItemTr.word] = if(clueType == ClueType.WORD) clueItemTr.word
                else clueItemTr.questions.random()
            }
        }
        return words
    }

    private fun getTopics(): ArrayList<String> {
        val data = intent.extras?.get("data") as ArrayList<*>
        val topics = hashSetOf<String>()
        for (wordItem in data) {
            val item = wordItem as ArrayList<*>
            val wordItemTr = item.find {
                val itemTr = it as LanguageItem
                itemTr.language == crosswordLanguage
            } as LanguageItem?
            if (wordItemTr != null && item.find {
                    val itemTr = it as LanguageItem
                    itemTr.language == clueLanguage &&
                        (clueType == ClueType.WORD || itemTr.questions.isNotEmpty())
                } != null) {
                topics.addAll(wordItemTr.topics)
            }
        }
        return ArrayList(topics).apply { sort() }
    }

    internal fun getChosenTopics(): HashSet<String> {
        val topics: HashSet<String> = hashSetOf()
        for (i in 0 until topicList.childCount) {
            with(topicList.getChildAt(i) as CheckBox) {
                if (isChecked) topics.add(text.toString())
            }
        }
        return topics
    }

    companion object {
        const val CROSSWORD_SIZE: Int = 14
        const val MAX_SIDE: Int = 15
        const val WORDS_VARIABLE: String = "words"
        private const val NAME_FOR_CROSSWORD_WITH_MULTIPLE_TOPICS = "multiple"
    }
}
