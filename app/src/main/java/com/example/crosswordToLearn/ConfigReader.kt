package com.example.crosswordToLearn

import android.util.JsonReader
import java.io.InputStream
import java.nio.charset.Charset

class ConfigReader {

    fun read(inputStream: InputStream): Int =
        with(
            JsonReader(
                inputStream.bufferedReader
                    (Charset.forName(MainActivity.DEFAULT_ENCODING))
            )
        ) {
            beginObject()
            val starNumber: Int
            when (val tag = nextName()) {
                "star_number" -> starNumber = nextInt()
                else -> throw RuntimeException("The wrong json tag: $tag")
            }
            endObject()
            starNumber
        }
}