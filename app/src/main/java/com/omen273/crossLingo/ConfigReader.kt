package com.omen273.crossLingo

import android.util.JsonReader
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
            var starNumber = 0
            while (hasNext()) {
                when (val tag = nextName()) {
                    "star_number" -> starNumber = nextInt()
                    else -> throw RuntimeException("The wrong json tag: $tag")
                }
            }
            endObject()
            starNumber
        }

    fun readLevel(inputStream: InputStream, levelValidator : (level: String?) -> Unit): String? =
            with(
                    JsonReader(
                            inputStream.bufferedReader
                            (Charset.forName(MainActivity.DEFAULT_ENCODING))
                    )
            ) {
                beginObject()
                var level: String? = null
                while(hasNext()) {
                    when (val tag = nextName()) {
                        "level" -> {
                            val l = nextString()
                            levelValidator(l)
                            level = l
                        }
                        else -> throw RuntimeException("The wrong json tag: $tag")
                    }
                }
                endObject()
                level
            }
}
