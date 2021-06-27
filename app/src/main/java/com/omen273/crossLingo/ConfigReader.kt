package com.omen273.crossLingo

import android.util.JsonReader
import android.util.JsonToken
import java.io.InputStream
import java.nio.charset.Charset

class ConfigReader {

    fun readStarNumber(inputStream: InputStream): Int =
        with(
            JsonReader(
                inputStream.bufferedReader
                    (Charset.forName(MainActivity.DEFAULT_ENCODING))
            )
        ) {
            beginObject()
            val starNumber = when (val tag = nextName()) {
                "star_number" -> nextInt()
                else -> throw RuntimeException("The wrong json tag: $tag")
            }
            endObject()
            starNumber
        }

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
}
