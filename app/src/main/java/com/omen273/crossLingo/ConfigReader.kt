package com.omen273.crossLingo

import android.util.JsonReader
import java.io.InputStream
import java.nio.charset.Charset

class ConfigData(
    var starNumber:Int = 0,
    var level:String? = null)

class ConfigReader {

    fun read(inputStream: InputStream): ConfigData =
        with(
            JsonReader(
                inputStream.bufferedReader
                    (Charset.forName(MainActivity.DEFAULT_ENCODING))
            )
        ) {
            beginObject()
            val configData = ConfigData(10, null)
            when (val tag = nextName()) {
                "star_number" -> configData.starNumber = nextInt()
                "level" -> configData.level = nextString()
                else -> throw RuntimeException("The wrong json tag: $tag")
            }
            endObject()
            configData
        }
}