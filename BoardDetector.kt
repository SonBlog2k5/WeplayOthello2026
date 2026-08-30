package com.weplay.othello

import android.media.Image
import kotlin.math.abs
import kotlin.math.max

object BoardDetector {

    private var lastNs = 0L

    fun process(image: Image, overlay: HintOverlay) {

        val now = System.nanoTime()
        if (now - lastNs < 200_000_000L) return
        lastNs = now

        val w = image.width
        val h = image.height

        if (w < 300 || h < 300) return

        // Vùng bàn Othello WePlay theo màn hình dọc
        val left = (w * 0.33f).toInt()
        val top = (h * 0.35f).toInt()
        val boardW = (w * 0.38f).toInt()
        val boardH = (h * 0.32f).toInt()

        val plane = image.planes.firstOrNull() ?: return
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride

        fun brightness(x: Int, y: Int): Int {

            if (x < 0 || y < 0 || x >= w || y >= h)
                return 0

            val pos = y * rowStride + x * pixelStride

            if (pos < 0 || pos >= buffer.limit())
                return 0

            return buffer.get(pos).toInt() and 255
        }

        val board = Array(8) {
            Array(8) { Disc.EMPTY }
        }

        var black = 0
        var white = 0

        for (r in 0..7) {
            for (c in 0..7) {

                val cellW = boardW / 8f
                val cellH = boardH / 8f

                val cx =
                    (left + (c + 0.5f) * cellW).toInt()

                val cy =
                    (top + (r + 0.5f) * cellH).toInt()

                val radius =
                    max(3, minOf(cellW, cellH).toInt() / 6)

                var center = 0.0
                var outside = 0.0
                var n1 = 0
                var n2 = 0

                for (dy in -radius..radius) {
                    for (dx in -radius..radius) {

                        center += brightness(cx + dx, cy + dy)
                        n1++
                    }
                }

                val outer = radius * 2

                for (dy in -outer..outer) {
                    for (dx in -outer..outer) {

                        if (
                            abs(dx) <= radius &&
                            abs(dy) <= radius
                        ) continue

                        outside += brightness(cx + dx, cy + dy)
                        n2++
                    }
                }

                if (n1 == 0 || n2 == 0) continue

                center /= n1
                outside /= n2

                val diff = center - outside

                if (diff < -18) {
                    board[r][c] = Disc.BLACK
                    black++
                } else if (diff > 18) {
                    board[r][c] = Disc.WHITE
                    white++
                }
            }
        }

        val occupied = black + white

        if (occupied < 4) {
            overlay.update("Chưa nhận bàn")
            return
        }

        val side =
            if ((occupied - 4) % 2 == 0)
                Disc.BLACK
            else
                Disc.WHITE

        val best = Othello.best(board, side)

        if (best == null) {
            overlay.update("Không có nước")
            return
        }

        val x =
            left +
            ((best.col + 0.5f) * boardW / 8f).toInt()

        val y =
            top +
            ((best.row + 0.5f) * boardH / 8f).toInt()

        overlay.update(
            "${best.text} +${best.flips}",
            x,
            y
        )
    }
}
