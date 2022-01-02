package com.omen273.crossLingo

import android.os.Handler
import android.os.Looper

class Timer(private val duration : Long, private val action1 : () -> Unit, private val action2 : () -> Unit,
            val changeCondition : () -> Boolean) {

    private val handler = Handler(Looper.getMainLooper())
    private fun start(act: () -> Unit) = handler.postDelayed({
        act()
    }, duration)

    fun stop() = handler.removeCallbacksAndMessages(null)

    fun restart() {
        stop()
        start(if(changeCondition()) action2 else action1)
    }
}

class Dimmer(private val duration : Long, private val action1 : () -> Unit, private val action2 : () -> Unit) {

    enum class Action{ACTION1, ACTION2}
    var action = Action.ACTION1
    private val handler = Handler(Looper.getMainLooper())
    fun start() {
        handler.postDelayed({
            when (action) {
                Action.ACTION1 -> {
                    action1()
                    action = Action.ACTION2
                    start()
                }
                Action.ACTION2 -> {
                    action2()
                    action = Action.ACTION1
                    start()
                }
            }
        }, duration)
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
        action = Action.ACTION1
    }
}
