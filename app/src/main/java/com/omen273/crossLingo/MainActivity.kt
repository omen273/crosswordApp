package com.omen273.crossLingo

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    class Position(val row: Int, val column: Int)
    internal var imageSize: Int = 0

    private var currentCrosswordPosition = Position(0,0)

    lateinit var data: HashMap<String, wordsWithTipsByTopic>
    lateinit var topics: HashMap<String, ArrayList<String>>

    private class LinearViewData(val path:File, val text: String)

    private class TableAdapter(val activity: MainActivity) : RecyclerView.Adapter<TableAdapter.TableHolder>() {
        class TableHolder(val tableRow: TableRow) : RecyclerView.ViewHolder(tableRow)

        val dataset: MutableList<MutableList<LinearViewData>> = mutableListOf()

        internal fun addItemToRecyclerView(path: File, text: String) {
            if (dataset.isEmpty() || dataset[dataset.lastIndex].size == ITEMS_IN_ROW) {
                dataset.add(mutableListOf(LinearViewData(path, text)))
                notifyItemInserted(dataset.lastIndex)
            } else {
                dataset[dataset.lastIndex].add(LinearViewData(path, text))
                notifyItemChanged(dataset.lastIndex)
            }
        }

        private fun createDefaultTextView(context: Context) =
            TextView(context).apply {
                gravity = Gravity.CENTER
                //try to make all pictures the same size
                textSize = 12f
                minLines = 2
            }

        private fun createDefaultImageView(context: Context) = ImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            val layoutParams = LinearLayout.LayoutParams(activity.imageSize, activity.imageSize)
            layoutParams.setMargins(MARGIN, MARGIN, MARGIN, MARGIN)
            this.layoutParams = layoutParams
        }

        private fun createGeneratingLayout(context: Context): LinearLayout {
            val item = LinearLayout(context)
            item.orientation = LinearLayout.VERTICAL
            createDefaultImageView(context).also { im ->
                im.setImageResource(R.drawable.edit)
                im.setOnClickListener {
                    val generated = Intent(context, ChooseTopicsActivity::class.java)
                    generated.putExtra(CROSSWORD_DATA_NAME_VARIABLE, activity.data)
                    generated.putExtra(CROSSWORD_TOPICS_NAME_VARIABLE, activity.topics)
                    activity.startActivityForResult(generated, ACTIVITY_CHOOSE)
                }
                item.addView(im)
            }
            item.addView(createDefaultTextView(context).also{
                it.text = activity.getString(R.string.generate_crossword)})
            return item
        }

        private fun createCrosswordLayout(context: Context): LinearLayout {
            val linearLayout = LinearLayout(context)
            linearLayout.orientation = LinearLayout.VERTICAL
            createDefaultImageView(context).also{im -> linearLayout.addView(im)}

            createDefaultTextView(context).also { t->
                linearLayout.addView(t)
            }
            return linearLayout
        }

        companion object ViewHolderType{
            val TOP_ROW = 0
            val ONLY_CROSSWORDS_ROW = 1
            val IMAGE_POSITION = 0
            val TEXT_POSITION = 1
        }

        override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
        ): TableHolder {
            val tableRow = TableRow(parent.context)
            when (viewType) {
                TOP_ROW -> {
                    tableRow.addView(createGeneratingLayout(parent.context))
                }
                ONLY_CROSSWORDS_ROW ->{
                    tableRow.addView(createCrosswordLayout(parent.context))
                }
            }

            for(i in 1 until ITEMS_IN_ROW ) {
                val view = createCrosswordLayout(parent.context)
                view.visibility = View.INVISIBLE
                tableRow.addView(view)
            }
            return TableHolder(tableRow)
        }

        override fun onBindViewHolder(holder: TableHolder, position: Int) {
            val currentRow = dataset[position]
            val lastIndex = currentRow.lastIndex
            for(currentColumn in 0 .. lastIndex) {
                if (getItemViewType(position) == ONLY_CROSSWORDS_ROW || currentColumn != 0) {
                    val currentLayout = holder.tableRow.getChildAt(currentColumn) as LinearLayout
                    val currentImageView = currentLayout.getChildAt(IMAGE_POSITION) as ImageView
                    currentImageView.setImageDrawable(Drawable.createFromPath(currentRow[currentColumn].path.absolutePath))
                    if (currentImageView.drawable == null) {
                        Log.e("ERROR", "The bad image")
                        Toast.makeText(
                            activity, R.string.damaged_data,
                            Toast.LENGTH_SHORT
                        ).show()
                        activity.deleteCrosswordImpl(Position(position, currentColumn), false)
                        break;
                    }
                    val currentTextView = currentLayout.getChildAt(TEXT_POSITION) as TextView
                    currentTextView.text = currentRow[currentColumn].text
                    currentImageView.setOnClickListener {
                        Intent(activity, GameActivity::class.java).apply {
                            putExtra(CROSSWORD_NAME_VARIABLE, currentTextView.text)
                            putExtra(CROSSWORD_IS_GENERATED_VARIABLE, false)
                            activity.currentCrosswordPosition = Position(position, currentColumn)
                            activity.startActivityForResult(this, ACTIVITY_GAME)
                        }
                    }
                    currentImageView.setOnLongClickListener {
                        activity.currentCrosswordPosition = Position(position, currentColumn)
                        activity.deleteCrossword(
                            currentTextView.text.toString().removeSuffix(IMAGE_FORMAT),
                            activity.currentCrosswordPosition
                        )
                        true
                    }
                    currentLayout.visibility = View.VISIBLE
                }
            }

            for(i in currentRow.size until ITEMS_IN_ROW ) {
                val layout = holder.tableRow.getChildAt(i) as LinearLayout
                layout.visibility = View.INVISIBLE
            }
        }

        override fun getItemViewType(position: Int) = if(position == 0) 0 else 1
        override fun getItemCount(): Int = dataset.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val transformer = DataTransformer(resources.openRawResource(R.raw.data).use { WordsReader().read(it,
            fun(level: String){Utils.validateLevel(resources, level)}) })
        data = transformer.dataByLevelsByTopics
        topics = transformer.sortedTopicsByLevel
        if(ChooseTopicsActivity.readLevelFromConfig(filesDir, resources) == null) {
            showLevelDialog()
        }

        imageSize = computeImageSize(resources)
        tableLayout.also {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = TableAdapter(this)
        }

        tableLayout.setHasFixedSize(true)
        //will add path and name for the first item inside recylceview
        (tableLayout.adapter as TableAdapter).addItemToRecyclerView( File(""), "")
        addItems()

        settingsImage.setOnClickListener {
            val settings = Intent(this, SettActivity::class.java)
            startActivity(settings) }
    }

    class DeleteCrossword(
            private val context: MainActivity,
            private val name: String,
            private val pos: Position
    ) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog =
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setMessage(getString(R.string.delete_crossword_question, name))
                    .setPositiveButton(R.string.yes) { _, _ -> context.deleteCrosswordImpl(pos) }
                    .setNegativeButton(R.string.no) { _, _ -> }
                    .create()
            } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun deleteCrosswordData(pos: Position) {
        val name = (tableLayout.adapter as TableAdapter).
        dataset[pos.row][pos.column].text

        val pathToImage = pathToImage(name)
        if(!pathToImage.exists()) {
            Log.e("ERROR", "The path to the image doesn't " +
                    "exist: ${pathToImage.absolutePath}")
        } else pathToImage.delete()

        val pathToCrosswordData = File(filesDir, name + GameActivity.DATA_SUFFIX)
        if(!pathToCrosswordData.exists()) {
            Log.e("ERROR", "The path to the crossword data doesn't " +
                    "exist: ${pathToCrosswordData.absolutePath}")
        } else pathToCrosswordData.delete()

        val pathToCrosswordState = File(filesDir, name + GameActivity.STATE_SUFFIX)
        if(!pathToCrosswordState.exists()) {
            Log.e("ERROR", "The path to the crossword state doesn't " +
                    "exist: ${pathToCrosswordData.absolutePath}")
        } else pathToCrosswordState.delete()
    }

    private fun deleteCrossword(name: String, pos: Position) = DeleteCrossword(this, name, pos).apply {
        show(supportFragmentManager, "DeleteCrossword")
    }

    internal fun deleteCrosswordImpl(pos: Position, notify: Boolean = true) {
        deleteCrosswordData(pos)
        val adapter = tableLayout.adapter as TableAdapter
        val data = adapter.dataset
        val lastIndexRow = data.lastIndex
        val lastRowColumnIndex = data[lastIndexRow].lastIndex
        var prev = data[lastIndexRow][lastRowColumnIndex]
        for (i in lastIndexRow downTo pos.row) {
            val rowLastIndex = data[i].lastIndex
            for (j in rowLastIndex downTo 0) {
                if(i == lastIndexRow && j == lastRowColumnIndex) continue
                data[i][j] = prev.also{prev = data[i][j]}
                if (i == pos.row && j == pos.column) break
            }
        }
        val lastRow = data[lastIndexRow]
        lastRow.removeAt(lastRowColumnIndex)
        if(lastRow.isEmpty()) {
            data.removeAt(lastIndexRow)
            if(notify) adapter.notifyItemRemoved(lastIndexRow)
        }
        else if(notify){
             adapter.notifyItemChanged(lastIndexRow)
        }

        if(notify) adapter.notifyItemRangeChanged(pos.row, lastIndexRow - pos.row)
    }

    private fun addItems() {
        val path = pathToDrawable()
        if (path.exists()) {
            for (file in path.listFiles()
                ?.sortedWith(compareByDescending { it.lastModified() }) ?: return) {
                    if(file.extension == IMAGE_FORMAT.removePrefix(".")) {
                        val name = file.name.removeSuffix(IMAGE_FORMAT)
                        val pathToCrosswordData = File(filesDir, name + GameActivity.DATA_SUFFIX)
                        if (!pathToCrosswordData.exists()) {
                            Log.e("ERROR", "The path to the crossword data doesn't " +
                                    "exist: ${pathToCrosswordData.absolutePath}")
                            continue
                        }
                        val pathToCrosswordState = File(filesDir, name + GameActivity.STATE_SUFFIX)
                        if (!pathToCrosswordState.exists()) {
                            Log.e("ERROR", "The path to the crossword state doesn't " +
                                    "exist: ${pathToCrosswordData.absolutePath}")
                            continue
                        }
                        try {
                            (tableLayout.adapter as TableAdapter).addItemToRecyclerView(file, name)
                        } catch (e: Exception){
                            Log.e("ERROR", e.message.toString())
                        }
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ACTIVITY_CHOOSE -> when (resultCode) {
                ACTIVITY_GAME_OK -> {
                    val sharedPref =
                            getSharedPreferences("1", Context.MODE_PRIVATE) ?: return
                    val name = sharedPref.getString(CROSSWORD_NAME_VARIABLE, "") ?: return
                    val path = pathToImage(name)
                    if (!path.exists()) {
                        Log.e("ERROR", "The path to the image doesn't exist: ${path.absolutePath}")
                        return
                    }
                    try {
                        (tableLayout.adapter as TableAdapter).addItemToRecyclerView(path, name)
                    } catch (e: Exception) {
                        Log.e("ERROR", e.message.toString())
                        return
                    }
                    val adapter = tableLayout.adapter as TableAdapter
                    val lastRowIndex = adapter.dataset.lastIndex
                    val tableRow = adapter.dataset[lastRowIndex]
                    rightShift(lastRowIndex, tableRow.lastIndex)
                    adapter.notifyDataSetChanged()
                }
                ACTIVITY_GAME_REMOVE -> deleteCrosswordImpl(currentCrosswordPosition)
                ACTIVITY_GAME_BAD_DATA -> Toast.makeText(this@MainActivity, R.string.damaged_data,
                        Toast.LENGTH_SHORT).show()
            }
            ACTIVITY_GAME -> when (resultCode) {
                ACTIVITY_GAME_OK -> {
                    rightShift(currentCrosswordPosition.row, currentCrosswordPosition.column)
                    shiftFocusToStart()
                    tableLayout.adapter?.notifyDataSetChanged()
                }
                ACTIVITY_GAME_REMOVE -> deleteCrosswordImpl(currentCrosswordPosition)
                ACTIVITY_GAME_BAD_DATA -> Toast.makeText(this@MainActivity, R.string.damaged_data,
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shiftFocusToStart() =
        (tableLayout.layoutManager as LinearLayoutManager).scrollToPosition(0)

    private fun pathToDrawable() = File(getExternalFilesDir(null), IMAGE_DIRECTORY)

    private fun pathToImage(name: String) = File(pathToDrawable(), "$name${IMAGE_FORMAT}")

    private fun rightShift(row: Int, column: Int) {
        val adapter = tableLayout.adapter as TableAdapter
        val data = adapter.dataset
        var temp = data[0][1]
        data[0][1] = data[row][column]
        for (i in 1..row) {
            for (j in 0 until ITEMS_IN_ROW) {
                data[i][j] = temp.also{temp = data[i][j]}
                if (i == row && j == column) return
            }
        }
    }

    private fun showLevelDialog() {
        val types = resources.getStringArray(R.array.levels)
        val titleView = TextView(this)
        titleView.text = getString(R.string.choose_level_dialog_text)
        titleView.textSize = 18f
        titleView.setPadding(20,10,20,10)
        titleView.setTypeface(null,BOLD)
        titleView.setTextColor(Color.BLACK)
        val b = AlertDialog.Builder(this).setCustomTitle(titleView).
        setItems(types) { dialog, selectedItem -> dialog.dismiss()
            openFileOutput(ChooseTopicsActivity.LEVEL_NAME, MODE_PRIVATE).use {
                ConfigWriter().write(it, types[selectedItem])
            } }.create()
        b.show()
    }

    companion object {
        private const val ITEMS_IN_ROW = 2
        private const val MARGIN = 28
        const val IMAGE_FORMAT: String = ".jpg"
        const val ACTIVITY_GAME: Int = 1
        const val ACTIVITY_CHOOSE: Int = 2
        const val ACTIVITY_GAME_OK: Int = 1
        const val ACTIVITY_GAME_FAIL: Int = 2
        const val ACTIVITY_GAME_REMOVE: Int = 3
        const val ACTIVITY_GAME_BAD_DATA: Int = 4
        const val IMAGE_DIRECTORY: String = "drawable"
        const val CROSSWORD_NAME_VARIABLE: String = "name"
        const val CROSSWORD_IS_GENERATED_VARIABLE: String = "isGenerated"
        const val CROSSWORD_DATA_NAME_VARIABLE: String = "data"
        const val CROSSWORD_TOPICS_NAME_VARIABLE: String = "topics"
        const val DEFAULT_ENCODING: String = "UTF-8"
        fun computeImageSize(resources: Resources): Int =
            resources.displayMetrics.widthPixels / ITEMS_IN_ROW - 2 * MARGIN
    }
}
