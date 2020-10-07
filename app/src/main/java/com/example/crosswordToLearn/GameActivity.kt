package com.example.crosswordToLearn
// Copyright (c) Akop Karapetyan
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.toolbar.*
import org.akop.ararat.core.Crossword
import org.akop.ararat.core.CrosswordState
import org.akop.ararat.core.buildCrossword
import org.akop.ararat.core.buildWord
import org.akop.ararat.io.UClickJsonFormatter
import org.akop.ararat.view.CrosswordView
import java.io.*
import java.lang.RuntimeException
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

//TODO check saving in case of emergency switch off
//TODO cursor to second and so on letter if first has been solved add tests
//TODO fix cell solved after backspace, add tests
//TODO test for cross letter input
class GameActivity : AppCompatActivity(), CrosswordView.OnLongPressListener,
    CrosswordView.OnStateChangeListener, CrosswordView.OnSelectionChangeListener {

    internal lateinit var crosswordView: CrosswordView
    internal var name = ""
    internal var delete = false

    @ExperimentalUnsignedTypes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        crosswordView = findViewById(R.id.crossword)
        setSupportActionBar(game_toolbar)
        star_number.text = readConfig(filesDir, resources).toString()
        name = intent.getStringExtra(MainActivity.CROSSWORD_NAME_VARIABLE).toString()
        val isGenerated =
            intent.getBooleanExtra(MainActivity.CROSSWORD_IS_GENERATED_VARIABLE, false)
        val crossword =
            if (isGenerated) {
                @Suppress("UNCHECKED_CAST")
                val wordsMap =
                    intent.getSerializableExtra(ChooseTopicsActivity.WORDS_VARIABLE)
                        as HashMap<String, String>
                when (val crosswordParams = generateCrossword(wordsMap)) {
                    null -> {
                        setResult(MainActivity.ACTIVITY_GAME_FAIL)
                        finish()
                        return
                    }
                    else -> {
                        name = generateName(name)
                        val sharedPref =
                            getSharedPreferences("1", Context.MODE_PRIVATE) ?: return
                        with(sharedPref.edit()) {
                            putString(MainActivity.CROSSWORD_NAME_VARIABLE, name)
                            apply()
                        }

                        buildCrossword {
                            flags = 0
                            width = crosswordParams.width
                            height = crosswordParams.height
                            title = name
                            author = "author"
                            copyright = "copyright"
                            comment = "comment"
                            if (crosswordParams.words.size < 2) {
                                throw RuntimeException(
                                    "The crossword should consist " +
                                        "of more or equal to two words."
                                )
                            }
                            var prev: WordParams = crosswordParams.words[0]
                            var n = 1
                            for ((i, word) in
                            crosswordParams.words.sortedWith(compareBy({ it.y }, { it.x }))
                                .withIndex()) {
                                words += buildWord {
                                    direction =
                                        if (word.isHorizontal) Crossword.Word.DIR_ACROSS
                                        else Crossword.Word.DIR_DOWN
                                    hint = wordsMap[word.word.toLowerCase(Locale.ROOT)].toString()
                                    number =
                                        if (i != 0 && prev.x == word.x && prev.y == word.y) n - 1
                                        else n++
                                    startRow = word.y
                                    startColumn = word.x
                                    for (ch in word.word) addCell(ch.toString(), 0)
                                    prev = word
                                }
                            }
                        }
                    }
                }
            } else readCrossword()

        crosswordView.also { cv ->
            cv.crossword = crossword
            val fillName = name + STATE_SUFFIX
            if (!isGenerated) readState(fillName).also { st -> cv.restoreState(st) }
            cv.onLongPressListener = this
            cv.onStateChangeListener = this
            cv.onSelectionChangeListener = this
            cv.inputValidator = { ch -> !ch.first().isISOControl() }
            cv.undoMode = CrosswordView.UNDO_SMART
            cv.markerDisplayMode = CrosswordView.MARKER_CUSTOM or CrosswordView.MARKER_SOLVED
            onSelectionChanged(cv, cv.selectedWord, cv.selectedCell)
        }

        if (crosswordView.state?.isCompleted ?: return) showFinishGameDialog()
    }

    private fun readCrossword(): Crossword = openFileInput("$name${DATA_SUFFIX}").use {
        buildCrossword { UClickJsonFormatter().read(this, it) }
    }

    private fun readState(fillName: String): CrosswordState = openFileInput(fillName).use {
        it.bufferedReader(Charset.forName(MainActivity.DEFAULT_ENCODING)).use { br ->
            Gson().fromJson(br.readLine().toString(), CrosswordState::class.java)
        }
    }

    private fun generateCrossword(inp: HashMap<String, String>): CrosswordParams? {
        var res: CrosswordParams? = null
        val maxAttemptCount = 10
        var i = 0
        val maxTime = 100
        while (res == null && i < maxAttemptCount) {
            val stepAdding = 0.6 * maxTime * i
            res = CrosswordBuilderWrapper().getCrossword(
                ArrayList(inp.keys),
                ChooseTopicsActivity.CROSSWORD_SIZE, ChooseTopicsActivity.MAX_SIDE,
                maxTime + stepAdding.toInt()
            )
            ++i
        }
        return res
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val state = savedInstanceState.getParcelable("state") as CrosswordState?
        if (state != null) crosswordView.restoreState(state)
    }

    @ExperimentalUnsignedTypes
    private fun generateName(title: String): String {
        val last = fileList().filter { it.startsWith(title) }
            .filter { !it.endsWith(STATE_SUFFIX) }
            .maxWithOrNull(compareBy {
                it.subSequence(title.length, it.length - DATA_SUFFIX.length).toString().toUInt()
            })
        return if (last == null) title + "1"
        else {
            val number = last.subSequence(
                title.length,
                last.length - DATA_SUFFIX.length
            ).toString().toUInt() + 1u
            title + number.toString()
        }
    }

    @ExperimentalUnsignedTypes
    override fun onPause() {
        super.onPause()
        if (!delete) {
            writeCrossword()
            writeState()
            writeConfig()
            saveScreenshot()
        } else delete = false
    }

    private fun saveScreenshot() {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val path = File(getExternalFilesDir(null), MainActivity.IMAGE_DIRECTORY)
            if (!path.exists()) path.mkdir()
            File(path, "$name${MainActivity.IMAGE_FORMAT}").apply {
                FileOutputStream(this).use {
                    val targetSize =
                        intent.getIntExtra(
                            MainActivity.CROSSWORD_IMAGE_SIZE_VARIABLE,
                            200
                        )
                    val defaultQuality = 100
                    val ratio = if (crosswordView.puzzleBitmap != null)
                        targetSize * defaultQuality / maxOf(
                            (crosswordView.puzzleBitmap ?: return@use).width,
                            (crosswordView.puzzleBitmap ?: return@use).height
                        ) else defaultQuality
                    crosswordView.puzzleBitmap?.compress(Bitmap.CompressFormat.JPEG, ratio, it)
                }
            }
        }
    }

    private fun writeConfig() = openFileOutput(CONFIG_NAME, MODE_PRIVATE).use {
        ConfigWriter().write(it, star_number.text.toString().toInt())
    }

    private fun writeState() = openFileOutput(name + STATE_SUFFIX, MODE_PRIVATE).use {
        it.write(Gson().toJson(crosswordView.state).toString().toByteArray())
    }

    private fun writeCrossword() = openFileOutput("$name${DATA_SUFFIX}", MODE_PRIVATE).use {
        crosswordView.crossword?.let { it1 -> UClickJsonFormatter().write(it1, it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("state", crosswordView.state)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_game, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_solve_cell -> {
                if (crosswordView.isSelectedCellSolved() == false &&
                    star_number.text.toString().toInt() >= LETTER_OPEN_PRICE
                ) {
                    crosswordView.selectedWord?.let {
                        crosswordView.solveChar(
                            it,
                            crosswordView.selectedCell
                        )
                    }
                    star_number.text = (star_number.text.toString().toInt() -
                        LETTER_OPEN_PRICE).toString()
                    return true
                }
                return false
            }
            R.id.menu_solve_word -> {
                if (!crosswordView.isSelectedWordSolved() &&
                    star_number.text.toString().toInt() >= WORD_OPEN_PRICE
                ) {
                    crosswordView.selectedWord?.let { crosswordView.solveWord(it) }
                    star_number.text = (star_number.text.toString().toInt() -
                        WORD_OPEN_PRICE).toString()
                    return true
                }
                return false
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCellLongPressed(view: CrosswordView, word: Crossword.Word, cell: Int) {}

    override fun onCrosswordChanged(view: CrosswordView) {}

    class FinishGame(private val game: GameActivity) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog =
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setMessage(R.string.youve_solved_the_puzzle)
                    .setPositiveButton(R.string.reset) { _, _ -> game.crosswordView.reset() }
                    .setNegativeButton(R.string.another_crossword) { _, _ -> game.onBackPressed() }
                    .setNeutralButton(R.string.remove) { _, _ ->
                        game.delete = true
                        game.onBackPressed()
                    }
                    .create()
            } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun showFinishGameDialog() = FinishGame(this).apply {
        isCancelable = false
        show(supportFragmentManager, "FinishGame")
    }

    override fun onCrosswordSolved(view: CrosswordView) {
        star_number.text = (star_number.text.toString().toInt() + BONUS_ON_SOLVE).toString()
        showFinishGameDialog()
    }

    override fun onCrosswordUnsolved(view: CrosswordView) {}

    override fun onSelectionChanged(view: CrosswordView, word: Crossword.Word?, position: Int) {
        hint.text = when (word?.direction) {
            Crossword.Word.DIR_ACROSS -> getString(R.string.across, word.number, word.hint)
            Crossword.Word.DIR_DOWN -> getString(R.string.down, word.number, word.hint)
            else -> ""
        }
    }

    override fun onBackPressed() {
        setResult(if (delete) MainActivity.ACTIVITY_GAME_REMOVE else MainActivity.ACTIVITY_GAME_OK)
        super.onBackPressed()
    }

    companion object {
        const val LETTER_OPEN_PRICE: Int = 1
        const val WORD_OPEN_PRICE: Int = 3
        const val BONUS_ON_SOLVE: Int = 5
        const val CONFIG_NAME: String = "config.json"
        const val STATE_SUFFIX: String = "Fill.json"
        const val DATA_SUFFIX: String = ".json"

        fun readConfig(path: File, resources: Resources): Int = with(File(path, CONFIG_NAME)) {
            if (exists()) FileInputStream(this).use { ConfigReader().read(it) }
            else resources.openRawResource(R.raw.config).use { ConfigReader().read(it) }
        }
    }
}
