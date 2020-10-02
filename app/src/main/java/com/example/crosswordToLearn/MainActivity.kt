package com.example.crosswordToLearn

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private val crosswords = hashSetOf<String>()
    private var data = arrayListOf<ArrayList<LanguageItem>>()
    private var imageSize: Int = 0

    private class ImageData(var lastModificationDate: Long, var row: Int, var column: Int)

    private val imageDatas = hashMapOf<String, ImageData>()
    private var loadedName = ""

    private class TableAdapter : RecyclerView.Adapter<TableAdapter.TableHolder>() {
        class TableHolder(val tableRow: TableRow) : RecyclerView.ViewHolder(tableRow)

        val dataset: MutableList<MutableList<LinearLayout>> = mutableListOf()

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): TableHolder = TableHolder(TableRow(parent.context))

        override fun onBindViewHolder(holder: TableHolder, position: Int) {
            holder.tableRow.removeAllViews()
            for (layout in dataset[position]) {
                (layout.parent as TableRow?)?.removeAllViews()
                holder.tableRow.addView(layout)
            }
        }

        override fun getItemCount(): Int = dataset.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        data = resources.openRawResource(R.raw.data).use { WordsReader().read(it) }
        imageSize = computeImageSize()
        tableLayout.also {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = TableAdapter()
        }
        addItemToRecyclerView(createGeneratingImage())
        addItems()
    }

    private fun computeImageSize(): Int =
        resources.displayMetrics.widthPixels / ITEMS_IN_ROW - 2 * MARGIN

    private fun createGeneratingImage(): TableRow {
        val item = LinearLayout(this)
        item.orientation = LinearLayout.VERTICAL
        ImageView(this).apply {
            setImageResource(R.drawable.edit)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            val layoutParams = LinearLayout.LayoutParams(imageSize, imageSize)
            layoutParams.setMargins(MARGIN, 0, MARGIN, 2 * MARGIN)
            this.layoutParams = layoutParams
        }.also { im ->
            im.setOnClickListener {
                val generated = Intent(this, ChooseTopicsActivity::class.java)
                generated.putExtra(CROSSWORD_DATA_NAME_VARIABLE, data)
                generated.putExtra(CROSSWORD_IMAGE_SIZE_VARIABLE, imageSize)
                startActivityForResult(generated, ACTIVITY_CHOOSE)
            }
            item.addView(im)
        }
        return TableRow(this).apply { addView(item) }
    }

    private fun addItemToRecyclerView(linearLayout: LinearLayout) {
        val adapter = tableLayout.adapter as TableAdapter
        val dataset = adapter.dataset
        if (dataset.isEmpty() || dataset[dataset.lastIndex].size == ITEMS_IN_ROW) {
            dataset.add(mutableListOf(linearLayout))
            adapter.notifyItemInserted(dataset.lastIndex)
        } else {
            dataset[dataset.lastIndex].add(linearLayout)
            adapter.notifyItemChanged(dataset.lastIndex)
        }
    }

    //TODO show dialog above or below crossword image
    //TODO add test for deletion
    class DeleteCrossword(
        private val context: MainActivity,
        private val name: String
    ) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog =
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setMessage(getString(R.string.delete_crossword_question, name))
                    .setPositiveButton(R.string.yes) { _, _ -> context.deleteCrosswordImpl(name) }
                    .setNegativeButton(R.string.no) { _, _ -> }
                    .create()
            } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun deleteCrosswordData(name: String) {
        pathToImage(name).delete()
        File(filesDir, name + GameActivity.DATA_SUFFIX).delete()
        File(filesDir, name + GameActivity.STATE_SUFFIX).delete()
    }

    private fun deleteCrossword(name: String) = DeleteCrossword(this, name).apply {
        show(supportFragmentManager, "DeleteCrossword")
    }

    internal fun deleteCrosswordImpl(name: String) {
        val item = imageDatas[name]
        if (item != null) {
            leftShift(item.row, item.column)
            val adapter = tableLayout.adapter as TableAdapter
            val dataset = adapter.dataset
            val lastRow = dataset[dataset.lastIndex]
            lastRow.removeAt(lastRow.lastIndex)
            if (lastRow.isNotEmpty()) {
                adapter.notifyItemChanged(dataset.lastIndex)
            } else {
                dataset.removeAt(dataset.lastIndex)
                adapter.notifyItemRemoved(dataset.size)
            }
            deleteCrosswordData(name)
        }
    }

    private fun addItem(path: File) {
        val name = path.name.removeSuffix(IMAGE_FORMAT)
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        val imageView = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_XY
            adjustViewBounds = true
            setImageDrawable(Drawable.createFromPath(path.absolutePath))
            val layoutParams = LinearLayout.LayoutParams(imageSize, imageSize)
            layoutParams.setMargins(MARGIN, 0, MARGIN, 0)
            this.layoutParams = layoutParams
            linearLayout.addView(this)
        }
        val textView = TextView(this).apply {
            text = name
            gravity = Gravity.CENTER
            crosswords.add(text.toString())
            linearLayout.addView(this)
        }
        imageView.setOnClickListener {
            Intent(this@MainActivity, GameActivity::class.java).apply {
                putExtra(CROSSWORD_NAME_VARIABLE, textView.text)
                putExtra(CROSSWORD_IS_GENERATED_VARIABLE, false)
                putExtra(CROSSWORD_IMAGE_SIZE_VARIABLE, imageSize)
                loadedName = textView.text.toString().removeSuffix(IMAGE_FORMAT)
                startActivityForResult(this, ACTIVITY_GAME)
            }
        }
        imageView.setOnLongClickListener {
            deleteCrossword(textView.text.toString().removeSuffix(IMAGE_FORMAT))
            true
        }
        addItemToRecyclerView(linearLayout)
        val dataset = (tableLayout.adapter as TableAdapter).dataset
        imageDatas[name] = ImageData(
            path.lastModified(), dataset.lastIndex, dataset[dataset.lastIndex].lastIndex
        )
    }

    private fun addItems() {
        val path = pathToDrawable()
        if (path.exists()) {
            for (file in path.listFiles()
                ?.sortedWith(compareByDescending { it.lastModified() }) ?: return) {
                addItem(file)
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
                    addItem(pathToImage(name))
                    val adapter = tableLayout.adapter as TableAdapter
                    val tableRow = adapter.dataset[adapter.dataset.lastIndex]
                    val linearLayout = tableRow[tableRow.lastIndex]
                    rightShift(adapter.dataset.lastIndex, tableRow.lastIndex, linearLayout)
                    adapter.notifyDataSetChanged()
                }
                ACTIVITY_GAME_REMOVE -> deleteCrosswordImpl(loadedName)
            }
            ACTIVITY_GAME -> when (resultCode) {
                ACTIVITY_GAME_OK -> {
                    val item = imageDatas[loadedName] ?: return
                    val adapter = tableLayout.adapter as TableAdapter
                    val linearLayout = adapter.dataset[item.row][item.column]
                    (linearLayout.getChildAt(0) as ImageView).setImageDrawable(
                        Drawable.createFromPath(pathToImage(loadedName).absolutePath)
                    )
                    rightShift(item.row, item.column, linearLayout)
                    shiftFocusToStart()
                    adapter.notifyDataSetChanged()
                }
                ACTIVITY_GAME_REMOVE -> deleteCrosswordImpl(loadedName)
            }
        }
    }

    private fun shiftFocusToStart() =
        (tableLayout.layoutManager as LinearLayoutManager).scrollToPosition(0)

    private fun pathToDrawable() = File(getExternalFilesDir(null), IMAGE_DIRECTORY)

    private fun pathToImage(name: String) = File(pathToDrawable(), "$name${IMAGE_FORMAT}")

    private fun swapItems(
        i: Int,
        j: Int,
        tempImage: Drawable,
        tempText: CharSequence
    ): Pair<Drawable, CharSequence> {
        var tempImage1 = tempImage
        var tempText1 = tempText
        val dataset = (tableLayout.adapter as TableAdapter).dataset
        val linearLayout = dataset[i][j]
        val imageView = linearLayout.getChildAt(0) as ImageView
        val textView = linearLayout.getChildAt(1) as TextView
        imageView.setImageDrawable(tempImage1.also { tempImage1 = imageView.drawable })
        textView.text = tempText1.also { tempText1 = textView.text }
        val name = textView.text.removePrefix(IMAGE_FORMAT).toString()
        val date = imageDatas[name]?.lastModificationDate
        if (date != null) imageDatas[name] = ImageData(date, i, j)
        return Pair(tempImage1, tempText1)
    }

    private fun rightShift(row: Int, column: Int, tempLayout: LinearLayout) {
        var tempImage = (tempLayout.getChildAt(0) as ImageView).drawable
        var tempText = (tempLayout.getChildAt(1) as TextView).text
        for (i in 0..row) {
            for (j in 0 until ITEMS_IN_ROW) {
                if (i != 0 || j != 0) {
                    val p = swapItems(i, j, tempImage, tempText)
                    tempImage = p.first
                    tempText = p.second
                }
                if (i == row && j == column) return
            }
        }
    }

    private fun leftShift(row: Int, column: Int) {
        val adapter = tableLayout.adapter as TableAdapter
        val dataset = adapter.dataset
        val lastRow = dataset[dataset.lastIndex]
        val tempLayout = lastRow[lastRow.lastIndex]
        var tempImage = (tempLayout.getChildAt(0) as ImageView).drawable
        var tempText = (tempLayout.getChildAt(1) as TextView).text
        for (i in adapter.itemCount - 1 downTo row) {
            for (j in adapter.dataset[i].lastIndex downTo 0) {
                if (i != 0 || j != 0) {
                    val p = swapItems(i, j, tempImage, tempText)
                    tempImage = p.first
                    tempText = p.second
                }
                if (i == row && j == column) return
            }
        }
    }

    companion object {
        private const val ITEMS_IN_ROW = 2
        private const val MARGIN = 10
        const val IMAGE_FORMAT: String = ".jpg"
        const val ACTIVITY_GAME: Int = 1
        const val ACTIVITY_CHOOSE: Int = 2
        const val ACTIVITY_GAME_OK: Int = 1
        const val ACTIVITY_GAME_FAIL: Int = 2
        const val ACTIVITY_GAME_REMOVE: Int = 3
        const val IMAGE_DIRECTORY: String = "drawable"
        const val CROSSWORD_NAME_VARIABLE: String = "name"
        const val CROSSWORD_IS_GENERATED_VARIABLE: String = "isGenerated"
        const val CROSSWORD_DATA_NAME_VARIABLE: String = "data"
        const val CROSSWORD_IMAGE_SIZE_VARIABLE: String = "imageSize"
        const val DEFAULT_ENCODING: String = "UTF-8"
    }
}
