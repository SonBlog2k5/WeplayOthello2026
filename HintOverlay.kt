package com.weplay.othello

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Overlay menu:
 * - Small round button at top-left.
 * - Tap the round button to open/close the OTHELLO TOOL panel.
 * - Shows scan status and the recommended move.
 * - Does NOT click the game board.
 */
class HintOverlay(private val c: Context) {
    private val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val bubble = TextView(c)
    private val panel = LinearLayout(c)
    private val status = TextView(c)
    private val move = TextView(c)

    private val target = TargetView(c)

    private var shown = false
    private var opened = false
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var panelParams: WindowManager.LayoutParams? = null
    private var targetParams: WindowManager.LayoutParams? = null

    fun show() {
        if (shown) return
        shown = true

        val type = if (android.os.Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        // Panel receives touches. The target marker never blocks the game.
        val touchFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL

        val noTouchFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL

        // ---------- Round button ----------
        bubble.text = "◉"
        bubble.gravity = Gravity.CENTER
        bubble.textSize = 24f
        bubble.setTextColor(Color.WHITE)
        bubble.background = rounded(0xDD202020.toInt(), 100f)
        bubble.elevation = 12f

        bubbleParams = WindowManager.LayoutParams(
            58, 58, type, touchFlags, PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 12
            y = 95
        }

        bubble.setOnTouchListener(DragOrTap(bubbleParams!!) {
            togglePanel()
        })

        // ---------- Main panel ----------
        panel.orientation = LinearLayout.VERTICAL
        panel.setPadding(22, 18, 22, 18)
        panel.background = rounded(0xF21B1B1B.toInt(), 22f)
        panel.elevation = 14f

        val title = TextView(c).apply {
            text = "OTHELLO TOOL"
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, 10)
        }

        status.text = "●  ĐANG CHẠY"
        status.textSize = 15f
        status.setTextColor(Color.rgb(90, 235, 100))
        status.setPadding(0, 6, 0, 12)

        val scan = TextView(c).apply {
            text = "◉  Tự động quét"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, 10, 0, 10)
        }

        val guide = TextView(c).apply {
            text = "🎯  Hiện hướng đánh"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, 10, 0, 10)
        }

        move.text = "🎯  Chưa nhận bàn"
        move.textSize = 16f
        move.setTextColor(Color.WHITE)
        move.setPadding(0, 14, 0, 14)

        val help = TextView(c).apply {
            text = "❓  Hướng dẫn sử dụng\n\n" +
                    "1. Mở WePlay và vào bàn Othello.\n" +
                    "2. Cấp quyền hiển thị trên ứng dụng khác.\n" +
                    "3. Bật quét màn hình.\n" +
                    "4. Tool sẽ chỉ vị trí nên đánh.\n\n" +
                    "Không tự bấm vào game."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 10, 0, 4)
        }

        panel.addView(title)
        panel.addView(status)
        panel.addView(scan)
        panel.addView(guide)
        panel.addView(move)
        panel.addView(help)

        panelParams = WindowManager.LayoutParams(
            310, WindowManager.LayoutParams.WRAP_CONTENT,
            type, touchFlags, PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 12
            y = 160
        }

        // ---------- Aim marker ----------
        targetParams = WindowManager.LayoutParams(
            58, 58, type, noTouchFlags, PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        wm.addView(target, targetParams)
        wm.addView(bubble, bubbleParams)
        wm.addView(panel, panelParams)
        panel.visibility = View.GONE
    }

    private fun togglePanel() {
        opened = !opened
        panel.post {
            panel.visibility = if (opened) View.VISIBLE else View.GONE
        }
    }

    fun update(text: String, x: Int? = null, y: Int? = null) {
        panel.post {
            move.text = when {
                text == "đang nhận bàn…" ->
                    "🔎  Đang nhận bàn…"
                text == "không có nước" ->
                    "❌  Không có nước hợp lệ"
                text.contains("+") ->
                    "🎯  Nước đề xuất: $text"
                else ->
                    "✓  $text"
            }
        }

        if (x != null && y != null && targetParams != null) {
            targetParams!!.x = x - 29
            targetParams!!.y = y - 29
            target.post {
                runCatching { wm.updateViewLayout(target, targetParams) }
            }
        }
    }

    fun hide() {
        if (!shown) return
        runCatching { wm.removeView(panel) }
        runCatching { wm.removeView(bubble) }
        runCatching { wm.removeView(target) }
        shown = false
        opened = false
    }

    private fun rounded(color: Int, radius: Float): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }

    private class TargetView(c: Context) : View(c) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            color = Color.GREEN
            alpha = 235
        }

        override fun onDraw(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f
            canvas.drawCircle(cx, cy, 20f, p)
            canvas.drawLine(cx - 27, cy, cx - 10, cy, p)
            canvas.drawLine(cx + 10, cy, cx + 27, cy, p)
            canvas.drawLine(cx, cy - 27, cx, cy - 10, p)
            canvas.drawLine(cx, cy + 10, cx, cy + 27, p)
        }
    }

    private class DragOrTap(
        private val p: WindowManager.LayoutParams,
        private val onTap: () -> Unit
    ) : View.OnTouchListener {
        private var downX = 0f
        private var downY = 0f
        private var startX = 0
        private var startY = 0
        private var moved = false

        override fun onTouch(v: View, e: MotionEvent): Boolean {
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX
                    downY = e.rawY
                    startX = p.x
                    startY = p.y
                    moved = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = e.rawX - downX
                    val dy = e.rawY - downY
                    if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) moved = true

                    p.x = startX + dx.toInt()
                    p.y = startY + dy.toInt()

                    runCatching {
                        (v.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                            .updateViewLayout(v, p)
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (!moved) onTap()
                    return true
                }
            }
            return true
        }
    }
}
