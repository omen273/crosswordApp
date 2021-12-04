package com.omen273.crossLingo

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.keyboard.view.*
import android.view.inputmethod.InputConnection
import android.widget.Button


class KeyboardView(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
    LinearLayout(context, attrs, defStyleAttr), View.OnClickListener{

    constructor(context: Context):
        this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?):
        this(context, attrs, 0)

    init {
        LayoutInflater.from(context).inflate(R.layout.keyboard, this, true);
        button_q.setOnClickListener(this)
        button_w.setOnClickListener(this)
        button_e.setOnClickListener(this)
        button_r.setOnClickListener(this)
        button_t.setOnClickListener(this)
        button_y.setOnClickListener(this)
        button_u.setOnClickListener(this)
        button_i.setOnClickListener(this)
        button_o.setOnClickListener(this)
        button_p.setOnClickListener(this)
        button_a.setOnClickListener(this)
        button_s.setOnClickListener(this)
        button_d.setOnClickListener(this)
        button_f.setOnClickListener(this)
        button_g.setOnClickListener(this)
        button_h.setOnClickListener(this)
        button_j.setOnClickListener(this)
        button_k.setOnClickListener(this)
        button_l.setOnClickListener(this)
        button_z.setOnClickListener(this)
        button_x.setOnClickListener(this)
        button_c.setOnClickListener(this)
        button_v.setOnClickListener(this)
        button_b.setOnClickListener(this)
        button_n.setOnClickListener(this)
        button_m.setOnClickListener(this)
        button_backspace.setOnClickListener(this)
    }

    var inputConnection: InputConnection? = null

    override fun onClick(p0: View?) {

        if (inputConnection == null || p0 == null) return

        if(p0.id == R.id.button_backspace) {
            inputConnection!!.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
        }
        else {
            val value = (p0 as Button).text
            inputConnection!!.commitText(value, 1)
        }
    }
}
