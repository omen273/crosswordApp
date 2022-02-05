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
import android.speech.tts.TextToSpeech
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.EditText
import androidx.core.graphics.drawable.DrawableCompat

class GameActivity : AppCompatActivity(), CrosswordView.OnLongPressListener,
    CrosswordView.OnStateChangeListener, CrosswordView.OnSelectionChangeListener,
    CrosswordView.OnPrintLetterListener,
    CrosswordView.OnWordSolved {

    private lateinit var crosswordView: CrosswordView
    internal var name = ""
    private var delete = false
    private var onBackPressedCallBefore = false
    private lateinit var cellMenuItem: MenuItem
    private lateinit var wordMenuItem: MenuItem
    private var freeClue = false
    private var clueCount = MAX_HINTS_NUMBER
    private var starNumber = 0

    private val freeClueTimer = Timer(15000, { giveFreeClueSquare(this) },
        { giveFreeClueWord(this) }, { changeCondition(this) })
    private val dimmer = Dimmer(500, { changeMenuButtonColor(this, R.color.colorDimmer) },
        { changeMenuButtonColor(this, R.color.white) }
    )

    private var TTS: TextToSpeech? = null

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
        star_number.text = readStarNumberFromConfig(filesDir, resources).toString()
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
                clueCount = savedInstanceState.getInt("clueCount")
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
            cv.moveSelectionToSolvedSquares =
                SettActivity.readMoveSelectionToSolvedSquares(filesDir, resources)
            activateOnMoveCursorToSolvedCellsMode = cv.moveSelectionToSolvedSquares
            val state = savedInstanceState?.getParcelable("state")
                    as CrosswordState?
            if (state != null)
                crosswordView.restoreState(state)
            else {
                if (!isGenerated) try {
                    readCrosswordState(name).also { st -> cv.restoreState(st) }
                    readClueCount(name)
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

        if (SettActivity.readEnableSound(filesDir, resources))
        {
            initTTS()
        }
    }

    private fun initTTS()
    {
        TTS = TextToSpeech(this, TextToSpeech.OnInitListener { initStatus ->
            if (initStatus == TextToSpeech.SUCCESS) {
                TTS?.language = Locale.US
                TTS?.setPitch(1.3f)
                TTS?.setSpeechRate(0.7f)
            } else if (initStatus == TextToSpeech.ERROR) {
                Toast.makeText(this, R.string.TTS_unavailable, Toast.LENGTH_LONG).show()
                TTS = null
            }
        })
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

    private fun readCrosswordState(name: String): CrosswordState =
        openFileInput(name + STATE_SUFFIX).use {
            it.bufferedReader(Charset.forName(MainActivity.DEFAULT_ENCODING)).use { br ->
                Gson().fromJson(br.readLine().toString(), CrosswordState::class.java)
            }
        }

    private fun readClueCount(name: String) {
        val f = File(filesDir, name + CLUE_COUNT_NAME + STATE_SUFFIX)
        if (f.exists()) {
            openFileInput(f.name).use {
                clueCount = Utils.readInt(it, CLUE_COUNT_NAME)
            }
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
        if (!delete) {
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
        dimmer.stop()
        freeClueTimer.stop()
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

    private fun writeConfig() = openFileOutput(CONFIG_NAME, MODE_PRIVATE).use {
        val starIndicatorText = star_number.text.toString()
        ConfigWriter().write(
            it, if (starIndicatorText.toIntOrNull() != null)
                starIndicatorText.toInt() else starNumber
        )
    }

    private fun writeState() {
        openFileOutput(name + STATE_SUFFIX, MODE_PRIVATE).use {
            it.write(Gson().toJson(crosswordView.state).toString().toByteArray())
        }
        openFileOutput(name + CLUE_COUNT_NAME + STATE_SUFFIX, MODE_PRIVATE).use {
            Utils.writeInt(it, clueCount, CLUE_COUNT_NAME)
        }
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
        outState.putInt("clueCount", clueCount)
        dimmer.stop()
        freeClueTimer.stop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_game, menu)
        cellMenuItem = menu.findItem(R.id.menu_solve_cell)
        wordMenuItem = menu.findItem(R.id.menu_solve_word)
        freeClueRestart()
        return true
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        dimmer.stop()
        changeMenuButtonColor(this, R.color.white)
        return super.onMenuOpened(featureId, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val starIndicatorText = star_number.text.toString()
        when (item.itemId) {
            R.id.menu_solve_cell -> {
                if (crosswordView.isSelectedCellSolved() == false
                ) {
                    if (cellMenuItem.title == getString(R.string.solve_square_free)) {
                        cellMenuItem.title = getString(R.string.solve_square)
                        --clueCount
                        freeClueRestart()
                        freeClue = false
                        star_number.text = starNumber.toString()
                        crosswordView.selectedWord?.let {
                            crosswordView.solveChar(
                                it,
                                crosswordView.selectedCell
                            )
                        }
                        return true
                    }
                    if (starIndicatorText.toIntOrNull() ?: starNumber >= LETTER_OPEN_PRICE) {
                        star_number.text =
                            ((starIndicatorText.toIntOrNull() ?: starNumber) - LETTER_OPEN_PRICE).toString()
                        starNumber = star_number.text.toString().toInt()
                        crosswordView.selectedWord?.let {
                            crosswordView.solveChar(
                                it,
                                crosswordView.selectedCell
                            )
                        }
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
                    if (wordMenuItem.title == getString(R.string.solve_word_free)) {
                        freeClue = false
                        wordMenuItem.title = getString(R.string.solve_word)
                        star_number.text = starNumber.toString()
                        --clueCount
                        freeClueRestart()
                        crosswordView.selectedWord?.let { crosswordView.solveWord(it) }
                        return true
                    }
                    if (starIndicatorText.toIntOrNull() ?: starNumber >= WORD_OPEN_PRICE) {
                        star_number.text = ((starIndicatorText.toIntOrNull() ?: starNumber) -
                                WORD_OPEN_PRICE).toString()
                        starNumber = star_number.text.toString().toInt()
                        crosswordView.selectedWord?.let { crosswordView.solveWord(it) }
                        return true
                    }
                    val dialog = AlertDialog.Builder(this).setMessage(
                        getString(
                            R.string.not_enough_stars_word,
                            star_number.text.toString()
                        )
                    )
                        .setNeutralButton(R.string.okButton) { _, _ -> }.create()
                    dialog.show()
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
        if (!isFinishing) {
            val dialog = AlertDialog.Builder(this)
                .setPositiveButton(R.string.reset) { _, _ -> crosswordView.reset() }
                .setNegativeButton(R.string.another_crossword) { _, _ -> onBackPressed() }
                .setNeutralButton(R.string.remove) { _, _ ->
                    delete = true
                    onBackPressed()
                }
            if (!shownAgain) dialog.setMessage(R.string.youve_solved_the_puzzle)
            val builder = dialog.create()
            builder.setCancelable(false)
            builder.show()
            builder.window?.setGravity(Gravity.BOTTOM)
        }
    }

    private fun requestReview() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                manager.launchReviewFlow(this, reviewInfo).addOnCompleteListener {
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
        star_number.text = ((star_number.text.toString().toIntOrNull() ?: starNumber) + BONUS_ON_SOLVE).toString()

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

    private fun freeClueRestart() {
        if (clueCount > 0) freeClueTimer.restart()
    }

    override fun onWordSolved(word: Crossword.Word) {
        freeClueRestart()
        dimmer.stop()
        TTS?.speak(
                word.cells.joinToString(separator = "", transform = { it.chars }),
                TextToSpeech.QUEUE_FLUSH,
                null,
                hashCode().toString()
            )
    }

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
        setResult(
            if (delete) {
                delete = false
                MainActivity.ACTIVITY_GAME_REMOVE
            } else {
                saveData()
                MainActivity.ACTIVITY_GAME_OK
            }
        )
        super.onBackPressed()
    }

    override fun onStop()
    {
        super.onStop()
        TTS?.shutdown()
    }

    companion object {
        const val LETTER_OPEN_PRICE: Int = 1
        const val WORD_OPEN_PRICE: Int = 3
        const val BONUS_ON_SOLVE: Int = 5
        const val CONFIG_NAME: String = "star_number.json"
        const val STATE_SUFFIX: String = "Fill.json"
        const val DATA_SUFFIX: String = ".json"
        const val CLUE_COUNT_NAME: String = "ClueCount"
        private const val email_for_errors = "sokolikkatya@gmail.com"

        fun readStarNumberFromConfig(path: File, resources: Resources): Int =
            with(File(path, CONFIG_NAME)) {
                if (exists()) FileInputStream(this).use { ConfigReader().readStarNumber(it) }
                else resources.openRawResource(R.raw.star_number)
                    .use { ConfigReader().readStarNumber(it) }
            }

        private fun giveFreeClueSquare(activity: GameActivity) {
            activity.cellMenuItem.title = activity.getString(R.string.solve_square_free)
            activity.freeClue = true
            val starNumber = activity.star_number.text.toString()
            if (starNumber.toIntOrNull() != null) {
                activity.starNumber = starNumber.toInt()
            }
            activity.star_number.text = activity.getString(R.string.free)
            activity.dimmer.start()
        }

        private fun giveFreeClueWord(activity: GameActivity) {
            activity.wordMenuItem.title = activity.getString(R.string.solve_word_free)
            activity.freeClue = true
            val starNumber = activity.star_number.text.toString()
            if (starNumber.toIntOrNull() != null) {
                activity.starNumber = starNumber.toInt()
            }
            activity.star_number.text = activity.getString(R.string.free)
            activity.dimmer.start()
        }

        private fun changeMenuButtonColor(activity: GameActivity, color: Int) {
            var drawable = activity.game_toolbar.overflowIcon
            if (drawable != null) {
                drawable = DrawableCompat.wrap(drawable)
                DrawableCompat.setTint(drawable.mutate(), activity.resources.getColor(color))
                activity.game_toolbar.overflowIcon = drawable
            }
        }

        private fun changeCondition(activity: GameActivity) = (activity.clueCount - 1) % 3 == 0

        private val MAX_HINTS_NUMBER = 9
    }
}
