package com.omen273.crossLingo

import android.content.res.Resources

class Utils {
    companion object {
        fun validateLevel(resources: Resources, level: String?) {
            if (level != null) {
                val levels = resources.getStringArray(R.array.levels)
                val index = levels.indexOf(level)
                if (index == -1) throw RuntimeException("Unknown level")
            }
        }
    }
}