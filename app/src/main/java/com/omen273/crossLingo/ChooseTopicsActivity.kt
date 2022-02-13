package com.omen273.crossLingo

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.CheckBox
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_choose_topics.*
import kotlinx.android.synthetic.main.toolbar_activity_choose_topic.*
import kotlinx.android.synthetic.main.toolbar_sett.*
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class ChooseTopicsActivity : AppCompatActivity() {

    private var isTraining = false

    private fun getTopics(): ArrayList<String>? {
        val data = intent.extras?.get(MainActivity.CROSSWORD_TOPICS_NAME_VARIABLE) as HashMap<*, *>
        val level = readLevelFromConfig(filesDir, resources) ?: return arrayListOf()
        return data[level] as ArrayList<String>?
    }

    private fun getWords(topics: HashSet<String>) : HashMap<String, Pair<String, String>>{
        val data = intent.extras?.get(MainActivity.CROSSWORD_DATA_NAME_VARIABLE) as HashMap<*, *>
        val res =  hashMapOf<String, Pair<String, String>>()
        val level = readLevelFromConfig(filesDir, resources)
        val dataLevel = data[level] as HashMap<*, *>?
        for(topic in topics) {
            dataLevel?.get(topic)?.let { res.putAll((it as HashMap<String, String>).
            map{Pair(it.key, Pair(it.value, topic))}) }
        }
        return res
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_topics)
        setSupportActionBar(choose_topics_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState != null) {
            isTraining = savedInstanceState.getBoolean("isTraining")
        }
        choose_topics_toolbar.setNavigationOnClickListener{
            onBackPressed()
        }
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
                if(topicNames != null) {
                    for (topic in topicNames) {
                        if (!wordsInList.contains(topic) && topic.startsWith(p0.toString())) {
                            with(CheckBox(this@ChooseTopicsActivity)) {
                                text = topic
                                topicList.addView(this)
                            }
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
                                    (if (chosenTopics.size == 1) getChosenTopics().iterator()
                                        .next() else NAME_FOR_CROSSWORD_WITH_MULTIPLE_TOPICS) + " ("
                                            + (readLevelFromConfig(filesDir, resources)
                                        ?.substringAfter('(')
                                        ?: return@setOnClickListener) + ' '
                                )
                                putExtra(MainActivity.CROSSWORD_IS_GENERATED_VARIABLE, true)
                                val training = intent.getBooleanExtra(MainActivity.TRAINING_NAME_VARIABLE, false)
                                if(training || isTraining) {
                                    putExtra(MainActivity.TRAINING_NAME_VARIABLE, true)
                                    putExtra(TOPICS_VARIABLE, chosenTopics)
                                    isTraining = true
                                }
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
                MainActivity.ACTIVITY_GAME_BAD_DATA -> {
                    setResult(MainActivity.ACTIVITY_GAME_BAD_DATA)
                    finish()
                }
                MainActivity.ACTIVITY_GAME_TRAINING -> {
                    setResult(MainActivity.ACTIVITY_GAME_TRAINING)
                    finish()
                }
                MainActivity.ACTIVITY_GAME_OTHER_TOPICS -> {
                    isTraining = true
                }
                MainActivity.ACTIVITY_GAME_THE_SAME_TOPICS -> {
                    isTraining = true
                    val sharedPref =
                        getSharedPreferences("1", Context.MODE_PRIVATE) ?: return
                    val previousTopics = sharedPref.getStringSet(TOPICS_VARIABLE, setOf())
                    if(previousTopics != null) {
                        val hashSet = previousTopics.toHashSet()
                        val chosenWords = getWords(hashSet)
                        with(Intent(this, GameActivity::class.java)) {
                            putExtra(WORDS_VARIABLE, chosenWords)
                            putExtra(TOPICS_VARIABLE, hashSet)
                            putExtra(MainActivity.CROSSWORD_IS_GENERATED_VARIABLE, true)
                            putExtra(MainActivity.TRAINING_NAME_VARIABLE, true)
                            startActivityForResult(this, MainActivity.ACTIVITY_GAME)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (topicList.childCount == 0) {
            val topics = getTopics()
            if(topics != null) {
                for (topic in topics) {
                    with(CheckBox(this@ChooseTopicsActivity)) {
                        text = topic
                        topicList.addView(this)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isTraining", isTraining)
    }

    @Suppress("unused")
    enum class ClueType { WORD, QUESTION }

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
        const val LEVEL_NAME: String = "level.json"
        const val TOPICS_VARIABLE: String = "topics"
        private const val NAME_FOR_CROSSWORD_WITH_MULTIPLE_TOPICS = "multiple"

        fun readLevelFromConfig(path: File, resources: Resources): String? = with(File(path, LEVEL_NAME)) {
            val validate = fun(level: String){Utils.validateLevel(resources, level)}
            if (exists()) FileInputStream(this).use { ConfigReader().readLevel(it, validate) }
            else resources.openRawResource(R.raw.level).use { ConfigReader().readLevel(it, validate)
            }
        }
    }
}
