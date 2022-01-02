package com.omen273.crossLingo

import android.content.res.Resources
import android.util.JsonReader
import android.util.JsonWriter
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

class Utils {
    companion object {
        fun validateLevel(resources: Resources, level: String) {
            val levels = resources.getStringArray(R.array.levels)
            val index = levels.indexOf(level)
            if (index == -1) throw RuntimeException("Unknown level")
        }

        fun writeInt(outputStream: OutputStream, value: Int, name: String): Unit =
            with(JsonWriter(outputStream.writer(Charset.forName(MainActivity.DEFAULT_ENCODING)))) {
                beginObject()
                name(name).value(value)
                endObject()
                close()
            }

        fun readInt(inputStream: InputStream, name: String) = with(
            JsonReader(
                inputStream.bufferedReader
                    (Charset.forName(MainActivity.DEFAULT_ENCODING))
            )
        ) {
            beginObject()
            val starNumber = when (val tag = nextName()) {
                name -> nextInt()
                else -> throw RuntimeException("The wrong json tag: $tag")
            }
            endObject()
            starNumber
        }
    }
}