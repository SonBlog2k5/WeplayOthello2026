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

        /*
         * WEPLAY PORTRAIT
         *
         * Bàn nằm khoảng:
         * X = 33% -> 71%
         * Y = 33% -> 67%
         */
        val left = (w * 0.33f).toInt()
        val top = (h * 0.33f).toInt()
        val boardW = (w * 0.38f).toInt()
        val boardH = (h * 0.34f).toInt()

        val plane = image.planes.firstOrNull() ?: return
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride

        fun rgb(x: Int, y: Int): IntArray? {

            if (x < 0 || x >= w || y < 0 || y >= h)
                return null

            val pos = y * rowStride + x * pixelStride

            if (pos < 0 || pos + 3 >= buffer.limit())
                return null

            val r = buffer.get(pos).toInt() and 255
            val g = buffer.get(pos + 1).toInt() and 255
            val b = buffer.get(pos + 2).toInt() and 255

            return intArrayOf(r, g, b)
        }

        fun lum(p: IntArray): Double {
            return 0.2126 * p[0] +
                   0.7152 * p[1] +
                   0.0722 * p[2]
        }

        val board = Array(8) {
            Array(8) { Disc.EMPTY }
        }

        var black = 0
        var white = 0

        /*
         * Đọc từng ô.
         *
         * Không dùng ngưỡng sáng tuyệt đối nữa,
         * vì ô bàn màu sáng có thể bị nhầm thành quân trắng.
         */
        for (r in 0..7) {
            for (c in 0..7) {

                val cellW = boardW / 8f
                val cellH = boardH / 8f

                val cx =
                    (left + (c + 0.5f) * cellW).toInt()

                val cy =
                    (top + (r + 0.5f) * cellH).toInt()

                var centerSum = 0.0
                var edgeSum = 0.0
                var centerCount = 0
                var edgeCount = 0

                val radius =
                    max(2, minOf(cellW, cellH).toInt() / 6)

                // Trung tâm
                for (dy in -radius..radius) {
                    for (dx in -radius..radius) {

                        val p = rgb(cx + dx, cy + dy)
                            ?: continue

                        centerSum += lum(p)
                        centerCount++
                    }
                }

                // Vành ngoài của ô
                val outer = max(
                    radius + 2,
                    minOf(cellW, cellH).toInt() / 3
                )

                for (dy in -outer..outer) {
                    for (dx in -outer..outer) {

                        if (
                            abs(dx) < radius ||
                            abs(dy) < radius
                        ) continue

                        val p = rgb(cx + dx, cy + dy)
                            ?: continue

                        edgeSum += lum(p)
                        edgeCount++
                    }
                }

                if (centerCount == 0 || edgeCount == 0)
                    continue

                val center = centerSum / centerCount
                val edge = edgeSum / edgeCount

                val difference = center - edge

                /*
                 * Quân đen:
                 * trung tâm tối hơn nền xung quanh.
                 */
                if (difference < -22) {

                    board[r][c] = Disc.BLACK
                    black++

                /*
                 * Quân trắng:
                 * trung tâm sáng hơn nền xung quanh.
                 */
                } else if (difference > 22) {

                    board[r][c] = Disc.WHITE
                    white++

                } else {

                    board[r][c] = Disc.EMPTY
                }
            }
        }

        val occupied = black + white

        /*
         * Không đủ quân => chưa bắt được bàn.
         */
        if (occupied < 4 || occupied > 64) {
            overlay.update("đang nhận bàn…")
            return
        }

        /*
         * Xác định bên đi dựa trên số quân đã xuất hiện.
         */
        val movesPlayed = occupied - 4

        var side =
            if (movesPlayed % 2 == 0)
                Disc.BLACK
            else
                Disc.WHITE

        var best =
            Othello.best(board, side)

        /*
         * Nếu không có nước thì thử bên còn lại.
         */
        if (best == null) {

            side =
                if (side == Disc.BLACK)
                    Disc.WHITE
                else
                    Disc.BLACK

            best =
                Othello.best(board, side)
        }

        if (best != null) {

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

        } else {

            overlay.update("không có nước")
        }
    }
}
