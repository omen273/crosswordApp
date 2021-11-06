package com.omen273.crossLingo

import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_sett.*
import kotlinx.android.synthetic.main.toolbar_sett.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SettActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sett)
        setSupportActionBar(sett_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        sett_toolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        level_list.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                openFileOutput(ChooseTopicsActivity.LEVEL_NAME, MODE_PRIVATE).use {
                    ConfigWriter().write(it, parent?.selectedItem.toString())
                }
            }
        }

        move_cursor_to_filled_cell.setOnCheckedChangeListener { _, p1 ->
            writePrintToFilledCellsToConfig(this, p1)
        }

        val currentLevel = ChooseTopicsActivity.readLevelFromConfig(filesDir, resources)
        if (currentLevel != null) {
            val levels = resources.getStringArray(R.array.levels)
            val index = levels.indexOf(currentLevel)
            level_list.setSelection(index)
        }
        move_cursor_to_filled_cell.isChecked = readPrintToFilledCellsFromConfig(filesDir, resources)
    }

    companion object {
        private const val MOVE_TO_FILLED_CELLS_FILE: String = "move_cursor_to_solved_squares.json"
        fun readPrintToFilledCellsFromConfig(path: File, resources: Resources): Boolean = with(
            File(
                path,
                MOVE_TO_FILLED_CELLS_FILE
            )
        ) {
            if (exists()) FileInputStream(this).use { ConfigReader().moveCursorToSolvedSquares(it) }
            else resources.openRawResource(R.raw.move_cursor_to_solved_squares)
                .use { ConfigReader().moveCursorToSolvedSquares(it) }
        }

        fun writePrintToFilledCellsToConfig(context: Context, flag: Boolean) {
            context.openFileOutput(MOVE_TO_FILLED_CELLS_FILE, MODE_PRIVATE).use {
                ConfigWriter().write(it, flag)
            }
        }
    }
}
