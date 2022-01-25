package com.omen273.crossLingo
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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.toolbar_game.*
import org.akop.ararat.core.Crossword
import org.akop.ararat.core.CrosswordState
import org.akop.ararat.core.buildCrossword
import org.akop.ararat.core.buildWord
import org.akop.ararat.io.UClickJsonFormatter
import org.akop.ararat.view.CrosswordView
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import android.widget.Toast

import android.content.Intent
import android.graphics.Typeface
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.EditText


class GameActivity : AppCompatActivity(), CrosswordView.OnLongPressListener,
    CrosswordView.OnStateChangeListener, CrosswordView.OnSelectionChangeListener,
    CrosswordView.OnPrintLetterListener {

    private lateinit var crosswordView: CrosswordView
    internal var name = ""
    private var delete = false
    private var onBackPressedCallBefore = false

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        crosswordView = findViewById(R.id.crossword)
        setSupportActionBar(game_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        game_toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        val training = intent.getBooleanExtra(MainActivity.TRAINING_NAME_VARIABLE, false)
        if (!training) {
            star_number.text = readStarNumberFromConfig(filesDir, resources).toString()
        }
        else {
            starImage.visibility = View.INVISIBLE
        }
        val isGenerated =
            intent.getBooleanExtra(MainActivity.CROSSWORD_IS_GENERATED_VARIABLE, false)
        val crossword = when (val restoredCrossword =
            savedInstanceState?.getParcelable("crossword") as Crossword?) {
            null -> {
                name = intent.getStringExtra(MainActivity.CROSSWORD_NAME_VARIABLE).toString()
                if (isGenerated) {
                    @Suppress("UNCHECKED_CAST")
                    val wordsMap =
                        intent.getSerializableExtra(ChooseTopicsActivity.WORDS_VARIABLE)
                                as HashMap<String, Pair<String, String>>
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
                                        val item = wordsMap[word.word.lowercase(Locale.ROOT)]
                                        hint = item?.first
                                        //it is a topic in reality
                                        citation = item?.second
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
            }
            else -> {
                if (delete) onBackPressed()
                name = savedInstanceState?.getCharSequence("name") as String
                delete = savedInstanceState.getBoolean("delete")
                activateOnMoveCursorToSolvedCellsMode =
                    savedInstanceState.getBoolean("activateOnMoveCursorToSolvedCellsMode")
                hits = savedInstanceState.getInt("hits")
                restoredCrossword
            }
        }

        crosswordView.also { cv ->
            val tv = TypedValue()
            var actionBarHeight = 0
            if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight =
                    TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            }
            cv.toolbarHeight = actionBarHeight
            cv.hintView = hint
            cv.keyboard = keyboard_ga
            cv.crossword = crossword
            val fillName = name + STATE_SUFFIX
            cv.moveSelectionToSolvedSquares =
                SettActivity.readMoveSelectionToSolvedSquares(filesDir, resources)
            activateOnMoveCursorToSolvedCellsMode = cv.moveSelectionToSolvedSquares
            val state = savedInstanceState?.getParcelable("state")
                    as CrosswordState?
            if (state != null)
                crosswordView.restoreState(state)
            else {
                if (!isGenerated) try {
                    readState(fillName).also { st -> cv.restoreState(st) }
                } catch (e: Exception) {
                    Log.e("ERROR", "The bad crossword state")
                    setResult(MainActivity.ACTIVITY_GAME_BAD_DATA)
                    finish()
                }
            }
            cv.onLongPressListener = this
            cv.onStateChangeListener = this
            cv.onSelectionChangeListener = this
            cv.onPrintLetterListener = this
            cv.inputValidator = { ch -> !ch.first().isISOControl() }
            cv.undoMode = CrosswordView.UNDO_SMART
            cv.markerDisplayMode = CrosswordView.MARKER_CUSTOM or CrosswordView.MARKER_SOLVED
            onSelectionChanged(cv, cv.selectedWord, cv.selectedCell)
        }
        if (crosswordView.state?.isCompleted ?: return) showFinishGameDialog(true)
        keyboard_ga.inputConnection = crosswordView.onCreateInputConnection(EditorInfo())
    }

    private fun readCrossword(): Crossword = openFileInput("$name${DATA_SUFFIX}").use {
        buildCrossword {
            try {
                UClickJsonFormatter().read(this, it)
            } catch (e: Exception) {
                Log.e("ERROR", "The bad crossword data")
                setResult(MainActivity.ACTIVITY_GAME_BAD_DATA)
                finish()
            }
        }
    }

    private fun readState(fillName: String): CrosswordState = openFileInput(fillName).use {
        it.bufferedReader(Charset.forName(MainActivity.DEFAULT_ENCODING)).use { br ->
            Gson().fromJson(br.readLine().toString(), CrosswordState::class.java)
        }
    }

    private fun generateCrossword(inp: HashMap<String, Pair<String, String>>): CrosswordParams? {
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

    private fun saveData() {
        val training = intent.getBooleanExtra(MainActivity.TRAINING_NAME_VARIABLE, false)
        if (!delete && !training) {
            //check that a crossword has been drawn otherwise doesn't save it
            // not thrown an exception because it is a normal situation in case of
            //two another crossword usages. Please see https://github.com/omen273/crosswordApp/issues/138
            //for details
            if (crosswordView.puzzleBitmap != null) {
                writeCrossword()
                writeState()
                saveScreenshot()
            }
        }
    }

    override fun onPause() {
        writeConfig()
        if (!onBackPressedCallBefore) saveData()
        else onBackPressedCallBefore = false
        super.onPause()
    }

    private fun saveScreenshot() {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val path = File(getExternalFilesDir(null), MainActivity.IMAGE_DIRECTORY)
            if (!path.exists()) path.mkdir()
            File(path, "$name${MainActivity.IMAGE_FORMAT}").apply {
                FileOutputStream(this).use {
                    if (crosswordView.puzzleBitmap == null) return
                    val bitmap = crosswordView.puzzleBitmap!!
                    val maxDimension = maxOf(bitmap.width, bitmap.height)
                    val dstBmp =
                        Bitmap.createBitmap(maxDimension, maxDimension, Bitmap.Config.ARGB_8888)

                    val canvas = Canvas(dstBmp)
                    canvas.drawColor(Color.BLACK)
                    canvas.drawBitmap(
                        bitmap,
                        (maxDimension - bitmap.width).toFloat() / 2,
                        (maxDimension - bitmap.height).toFloat() / 2,
                        null
                    )
                    var size = MainActivity.computeImageSize(resources)
                    val minSize = 500
                    size = minOf(size, minSize)
                    dstBmp?.scale(size, size, false)
                        ?.compress(Bitmap.CompressFormat.JPEG, 40, it)
                }
            }
        } else {
            throw Exception("Media is not mounted")
        }
    }

    private fun writeConfig() {
        val starNumber = star_number.text.toString()
        if (starNumber.toIntOrNull() != null) {
            openFileOutput(CONFIG_NAME, MODE_PRIVATE).use {
                ConfigWriter().write(it, starNumber.toInt())
            }
        }
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
        if (crosswordView.crossword != null)
            outState.putParcelable("crossword", crosswordView.crossword)
        outState.putCharSequence("name", name)
        outState.putBoolean("delete", delete)
        outState.putBoolean(
            "activateOnMoveCursorToSolvedCellsMode",
            activateOnMoveCursorToSolvedCellsMode
        )
        outState.putInt("hits", hits)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_game, menu)
        if(intent.getBooleanExtra(MainActivity.TRAINING_NAME_VARIABLE, false)) {
            menu.findItem(R.id.menu_solve_cell).title = getString(R.string.solve_square_free)
            menu.findItem(R.id.menu_solve_word).title = getString(R.string.solve_word_free)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val training = intent.getBooleanExtra(MainActivity.TRAINING_NAME_VARIABLE, false)
        when (item.itemId) {
            R.id.menu_solve_cell -> {
                if (crosswordView.isSelectedCellSolved() == false
                ) {
                    if (training || star_number.text.toString().toInt() >= LETTER_OPEN_PRICE) {
                        crosswordView.selectedWord?.let {
                            crosswordView.solveChar(
                                it,
                                crosswordView.selectedCell
                            )
                        }
                        if (!training)
                            star_number.text = (star_number.text.toString().toInt() -
                                    LETTER_OPEN_PRICE).toString()
                        return true
                    } else {
                        val dialog = AlertDialog.Builder(this).setMessage(
                            getString(
                                R.string.not_enough_stars_square,
                                star_number.text.toString()
                            )
                        )
                            .setNeutralButton(R.string.okButton) { _, _ -> }.create()
                        dialog.show()
                    }
                }
                return false
            }
            R.id.menu_solve_word -> {
                if (!crosswordView.isSelectedWordSolved()
                ) {
                    if (training || star_number.text.toString().toInt() >= WORD_OPEN_PRICE) {
                        crosswordView.selectedWord?.let { crosswordView.solveWord(it) }
                        if (!training)
                            star_number.text = (star_number.text.toString().toInt() -
                                    WORD_OPEN_PRICE).toString()
                        return true
                    } else {
                        val dialog = AlertDialog.Builder(this).setMessage(
                            getString(
                                R.string.not_enough_stars_word,
                                star_number.text.toString()
                            )
                        )
                            .setNeutralButton(R.string.okButton) { _, _ -> }.create()
                        dialog.show()
                    }
                }
                return false
            }
            R.id.menu_report_error -> {
                val types = resources.getStringArray(R.array.error_reasons)
                val b = AlertDialog.Builder(this)
                    .setCustomTitle(createTitle(R.string.error_reasons_string))
                    .setItems(types) { dialog, selectedItem ->
                        dialog.dismiss()
                        val message = EditText(this)
                        val b1 =
                            AlertDialog.Builder(this)
                                .setCustomTitle(createTitle(R.string.error_message))
                                .setView(message)
                                .setPositiveButton(R.string.okButton) { _, _ ->
                                    crosswordView.selectedWord?.let {
                                        sendMail(
                                            it,
                                            types[selectedItem], message.text.toString()
                                        )
                                    }
                                }.create()
                        b1.show()
                    }.create()
                b.show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendMail(word: Crossword.Word, item: String, message: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "message/rfc822"
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf(email_for_errors))
        i.putExtra(
            Intent.EXTRA_SUBJECT, "level: ${
                ChooseTopicsActivity.readLevelFromConfig(filesDir, resources)
            }, hint: ${word.hint}, " +
                    "topic: ${word.citation}, reason: $item"
        )
        i.putExtra(Intent.EXTRA_TEXT, message)
        try {
            startActivity(Intent.createChooser(i, "Send mail..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "There are no email clients installed. " +
                        "Please, write an e-mail with a problem's description " +
                        "to $email_for_errors by yourself",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createTitle(stringID: Int) = with(TextView(this)) {
        text = getString(stringID)
        textSize = 18f
        setPadding(20, 10, 20, 10)
        setTypeface(null, Typeface.BOLD)
        setTextColor(Color.BLACK)
        this
    }

    override fun onCellLongPressed(view: CrosswordView, word: Crossword.Word, cell: Int) {
    }

    override fun onCrosswordChanged(view: CrosswordView) {}

    private fun showFinishGameDialog(shownAgain: Boolean = false) {
        val dialog = AlertDialog.Builder(this)
        if (!shownAgain) {
            if (!intent.getBooleanExtra(MainActivity.TRAINING_NAME_VARIABLE, false)) {
                dialog.setMessage(R.string.youve_solved_the_puzzle)
                    .setNegativeButton(R.string.another_crossword) { _, _ -> onBackPressed() }
                .setPositiveButton(R.string.reset) { _, _ -> crosswordView.reset() }
                .setNeutralButton(R.string.remove) { _, _ ->
                    delete = true
                    onBackPressed()
                }
            }
            else {
                dialog.setNeutralButton(R.string.train_the_same_topic) { _, _ ->
                    val topics =
                        intent.getSerializableExtra(ChooseTopicsActivity.TOPICS_VARIABLE)
                                as HashSet<String>
                    val sharedPref =
                        getSharedPreferences("1", Context.MODE_PRIVATE) ?: return@setNeutralButton
                    with(sharedPref.edit()) {
                        putStringSet(ChooseTopicsActivity.TOPICS_VARIABLE, topics)
                        apply()
                    }
                    setResult(MainActivity.ACTIVITY_GAME_THE_SAME_TOPICS)
                    onBackPressed()
                }
                .setPositiveButton(R.string.train_other_topics) { _, _  ->
                    setResult(MainActivity.ACTIVITY_GAME_OTHER_TOPICS)
                    onBackPressed()
                }.setNegativeButton(R.string.another_crossword) {  _, _  ->
                        setResult(MainActivity.ACTIVITY_GAME_TRAINING)
                        onBackPressed()
                    }

            }
        }

        val builder = dialog.create()
        builder.setCancelable(false)
        builder.show()
        builder.window?.setGravity(Gravity.BOTTOM)
    }

    private fun requestReview() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                manager.launchReviewFlow(this, reviewInfo).addOnCompleteListener { _ ->
                    showFinishGameDialog()
                }
            } else {
                showFinishGameDialog()
            }
        }
    }

    private fun readSolvedCrosswordNumberToFile(path: File) =
        try {
            FileInputStream(path).use { ConfigReader().solvedCrosswordNumber(it) }
        } catch (e: Exception) {
            Log.e("ERROR", "The bad solved crossword number file")
            path.delete()
            0
        }

    override fun onCrosswordSolved(view: CrosswordView) {
        if(!intent.getBooleanExtra(MainActivity.TRAINING_NAME_VARIABLE, false)) {
            star_number.text = (star_number.text.toString().toInt() + BONUS_ON_SOLVE).toString()
        }
        val path = File(filesDir, "solved_crossword_number.json")
        if (path.exists()) {
            val number = readSolvedCrosswordNumberToFile(path)
            if (number == 3 || number % 100 == 0) requestReview()
            else showFinishGameDialog()
            FileOutputStream(path).use { ConfigWriter().writeSolvedCrosswordNumber(it, number + 1) }
        } else {
            FileOutputStream(path).use { ConfigWriter().writeSolvedCrosswordNumber(it, 1) }
            showFinishGameDialog()
        }

    }

    override fun onCrosswordUnsolved(view: CrosswordView) {}

    private var hits = 0
    private var activateOnMoveCursorToSolvedCellsMode = false

    @kotlin.ExperimentalStdlibApi
    private fun detectMoveCursorToSolvedCellsMode(
        selection: CrosswordView.Selectable?,
        puzzleCells: Array<Array<CrosswordView.Cell?>>,
        ch: Char,
        position: Int
    ) {
        val startRow = selection?.word?.startRow
        val startColumn = selection?.word?.startColumn
        val row = selection?.row
        val column = selection?.column
        var solved = 0
        if (startRow != null && startColumn != null && row != null && column != null) {
            when (selection.word.direction) {
                Crossword.Word.DIR_ACROSS -> {
                    for (i in column - 1 downTo startColumn)
                        if (puzzleCells[row][i]?.isFlagSet(CrosswordView.Cell.FLAG_SOLVED) == true)
                            ++solved
                }
                Crossword.Word.DIR_DOWN -> {
                    for (i in row - 1 downTo startRow)
                        if (puzzleCells[i][column]?.isFlagSet(CrosswordView.Cell.FLAG_SOLVED) == true)
                            ++solved
                }
            }
        }

        if (selection != null) {
            if (solved > 0 && ch.uppercaseChar() ==
                selection.word.cells[position - solved].chars[0] &&
                ch.uppercaseChar() != selection.word.cells[position].chars[0]
            )
                ++hits
        }

        val MAX_HITS_NUMBER = 3
        if (hits == MAX_HITS_NUMBER) {
            val builder =
                AlertDialog.Builder(this).setMessage(R.string.change_print_mode_to_move_to_solved)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        SettActivity.writePrintToFilledCellsToConfig(this, true)
                        crosswordView.moveSelectionToSolvedSquares = true
                    }
                    .setNegativeButton(R.string.no) { _, _ ->
                    }.create()
            builder.setCancelable(false)
            builder.show()
            activateOnMoveCursorToSolvedCellsMode = true
        }
    }

    override fun onSelectionChanged(view: CrosswordView, word: Crossword.Word?, position: Int) {
        hint.text = when (word) {
            null -> ""
            else -> getString(R.string.tip, word.number, word.hint, word.citation)
        }
    }

    @ExperimentalStdlibApi
    override fun onPrintLetter(
        selection: CrosswordView.Selectable?,
        puzzleCells: Array<Array<CrosswordView.Cell?>>,
        ch: Char,
        position: Int
    ) {
        if (!activateOnMoveCursorToSolvedCellsMode)
            detectMoveCursorToSolvedCellsMode(selection, puzzleCells, ch, position)
    }

    override fun onBackPressed() {
        onBackPressedCallBefore = true
        if(!intent.getBooleanExtra(MainActivity.TRAINING_NAME_VARIABLE, false)) {
            setResult(
                when {
                    delete -> {
                        delete = false
                        MainActivity.ACTIVITY_GAME_REMOVE
                    }
                    else -> {
                        saveData()
                        MainActivity.ACTIVITY_GAME_OK
                    }
                }
            )
        }
        super.onBackPressed()
    }

    companion object {
        const val LETTER_OPEN_PRICE: Int = 1
        const val WORD_OPEN_PRICE: Int = 3
        const val BONUS_ON_SOLVE: Int = 5
        const val CONFIG_NAME: String = "star_number.json"
        const val STATE_SUFFIX: String = "Fill.json"
        const val DATA_SUFFIX: String = ".json"
        private const val email_for_errors = "sokolikkatya@gmail.com"

        fun readStarNumberFromConfig(path: File, resources: Resources): Int =
            with(File(path, CONFIG_NAME)) {
                if (exists()) FileInputStream(this).use { ConfigReader().readStarNumber(it) }
                else resources.openRawResource(R.raw.star_number)
                    .use { ConfigReader().readStarNumber(it) }
            }
    }
}
