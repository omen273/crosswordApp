package com.omen273.crossLingo

import android.util.JsonWriter
import java.io.OutputStream
import java.nio.charset.Charset

class ConfigWriter {

    fun write(outputStream: OutputStream, starNumber: Int): Unit =
        Utils.writeInt(outputStream, starNumber, "star_number")


    fun write(outputStream: OutputStream, level: String): Unit =
        with(JsonWriter(outputStream.writer(Charset.forName(MainActivity.DEFAULT_ENCODING)))) {
            beginObject()
            name("level").value(level)
            endObject()
            close()
        }

    fun write(outputStream: OutputStream, moveSelectionToSolvedSquares: Boolean): Unit =
        with(JsonWriter(outputStream.writer(Charset.forName(MainActivity.DEFAULT_ENCODING)))) {
            beginObject()
            name("move_selection_to_solved_squares").value(moveSelectionToSolvedSquares)
            endObject()
            close()
        }

    fun writeSolvedCrosswordNumber(outputStream: OutputStream, solvedNumber: Int): Unit =
        Utils.writeInt(outputStream, solvedNumber, "solved_crossword_number")
}

