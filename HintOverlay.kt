package com.weplay.othello

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.widget.TextView

class HintOverlay(private val c: Context) {
    private val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val menu = TextView(c)
    private val target = TargetView(c)
    private var shown = false
    private var menuParams: WindowManager.LayoutParams? = null
    private var targetParams: WindowManager.LayoutParams? = null

    fun show() {
        if (shown) return
        shown = true
        menu.text = "OTHELLO\n● QUÉT\n🎯 --"
        menu.setTextColor(Color.WHITE)
        menu.textSize = 12f
        menu.setPadding(12, 8, 12, 8)
        menu.background = GradientDrawable().apply {
            setColor(0x77808080)
            cornerRadius = 14f
        }
        val type = if (android.os.Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val commonFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        menuParams = WindowManager.LayoutParams(125, 70, type, commonFlags, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 12; y = 100
        }
        targetParams = WindowManager.LayoutParams(52, 52, type, commonFlags, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0; y = 0
        }
        menu.setOnTouchListener(DragTouch(menuParams!!))
        wm.addView(target, targetParams)
        wm.addView(menu, menuParams)
    }

    fun update(text: String, x: Int? = null, y: Int? = null) {
        menu.post { menu.text = "OTHELLO\n● QUÉT\n🎯 $text" }
        if (x != null && y != null && targetParams != null) {
            targetParams!!.x = x - 26
            targetParams!!.y = y - 26
            target.post { wm.updateViewLayout(target, targetParams) }
        }
    }

    fun hide() {
        if (!shown) return
        runCatching { wm.removeView(menu) }
        runCatching { wm.removeView(target) }
        shown = false
    }

    private class TargetView(c: Context) : View(c) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            color = Color.WHITE
            alpha = 230
        }
        override fun onDraw(canvas: Canvas) {
            val cx = width / 2f; val cy = height / 2f
            canvas.drawCircle(cx, cy, 19f, p)
            canvas.drawLine(cx - 25, cy, cx - 10, cy, p)
            canvas.drawLine(cx + 10, cy, cx + 25, cy, p)
            canvas.drawLine(cx, cy - 25, cx, cy - 10, p)
            canvas.drawLine(cx, cy + 10, cx, cy + 25, p)
        }
    }

    private class DragTouch(private val p: WindowManager.LayoutParams) : View.OnTouchListener {
        private var x = 0f; private var y = 0f; private var px = 0; private var py = 0
        override fun onTouch(v: View, e: MotionEvent): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { x = e.rawX; y = e.rawY; px = p.x; py = p.y }
                MotionEvent.ACTION_MOVE -> {
                    p.x = px - (e.rawX - x).toInt()
                    p.y = py + (e.rawY - y).toInt()
                    (v.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).updateViewLayout(v, p)
                }
            }
            return true
        }
    }
}
