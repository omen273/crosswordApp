package com.omen273.crossLingo

import android.util.JsonReader
import android.util.JsonToken
import java.io.InputStream
import java.nio.charset.Charset

class ConfigReader {

    fun readStarNumber(inputStream: InputStream): Int = Utils.readInt(inputStream, "star_number")

    fun readLevel(inputStream: InputStream, levelValidator : (level: String) -> Unit): String? =
            with(
                    JsonReader(
                            inputStream.bufferedReader
                            (Charset.forName(MainActivity.DEFAULT_ENCODING))
                    )
            ) {
                beginObject()
                var level: String? = null
                when (val tag = nextName()) {
                    "level" -> {
                        if(peek() == JsonToken.STRING) {
                            val l = nextString()
                            levelValidator(l)
                            level = l
                        }
                        else {
                            nextNull()
                        }
                    }
                    else -> throw RuntimeException("The wrong json tag: $tag")
                }
                endObject()
                level
            }

    fun moveCursorToSolvedSquares(inputStream: InputStream): Boolean =
        with(
            JsonReader(
                inputStream.bufferedReader
                    (Charset.forName(MainActivity.DEFAULT_ENCODING))
            )
        ) {
            beginObject()
            val moveCursor = when (val tag = nextName()) {
                "move_selection_to_solved_squares" -> nextBoolean()
                else -> throw RuntimeException("The wrong json tag: $tag")
            }
            endObject()
            moveCursor
        }

    fun enableSound(inputStream: InputStream): Boolean =
        with(
            JsonReader(
                inputStream.bufferedReader
                    (Charset.forName(MainActivity.DEFAULT_ENCODING))
            )
        ) {
            beginObject()
            val moveCursor = when (val tag = nextName()) {
                "enable_sound" -> nextBoolean()
                else -> throw RuntimeException("The wrong json tag: $tag")
            }
            endObject()
            moveCursor
        }

    fun solvedCrosswordNumber(inputStream: InputStream): Int =
        Utils.readInt(inputStream, "solved_crossword_number")
}
