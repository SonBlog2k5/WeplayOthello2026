package com.weplay.othello

import android.media.Image
import kotlin.math.abs

/** Lightweight WePlay detector tuned for the 8x8 board layout shown in the supplied screenshot.
 * It uses normalized board coordinates so different screen resolutions work without a manual crop.
 */
object BoardDetector {
    private var lastNs = 0L

    fun process(image: Image, overlay: HintOverlay) {
        val now = System.nanoTime()
        if (now - lastNs < 180_000_000L) return // ~5.5 scans/sec
        lastNs = now

        val w = image.width
        val h = image.height
        if (w < 300 || h < 300) return

        // Inner 8x8 playing area from the supplied WePlay layout.
        val left = (w * 0.30f).toInt()
        val top = (h * 0.365f).toInt()
        val size = (w * 0.58f).toInt()
        if (left + size > w || top + size > h) return

        val board = Array(8) { Array(8) { Disc.EMPTY } }
        var black = 0
        var white = 0
        val plane = image.planes.firstOrNull() ?: return
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * w

        fun pixel(x: Int, y: Int): IntArray? {
            if (x !in 0 until w || y !in 0 until h) return null
            val pos = y * rowStride + x * pixelStride
            if (pos < 0 || pos + 3 >= buffer.limit()) return null
            val r = buffer.get(pos).toInt() and 0xff
            val g = buffer.get(pos + 1).toInt() and 0xff
            val b = buffer.get(pos + 2).toInt() and 0xff
            return intArrayOf(r, g, b)
        }

        for (r in 0..7) for (c in 0..7) {
            val cx = left + ((c + 0.5f) * size / 8f).toInt()
            val cy = top + ((r + 0.5f) * size / 8f).toInt()
            var sum = 0
            var count = 0
            // Small center patch avoids a single noisy pixel while staying inside the disc/cell.
            val radius = maxOf(2, size / 8 / 5)
            for (dy in -radius..radius) for (dx in -radius..radius) {
                val p = pixel(cx + dx, cy + dy) ?: continue
                val l = (0.2126 * p[0] + 0.7152 * p[1] + 0.0722 * p[2]).toInt()
                sum += l
                count++
            }
            if (count == 0) continue
            val lum = sum / count
            val d = when {
                lum < 75 -> Disc.BLACK
                lum > 205 -> Disc.WHITE
                else -> Disc.EMPTY
            }
            board[r][c] = d
            if (d == Disc.BLACK) black++ else if (d == Disc.WHITE) white++
        }

        // Valid WePlay position: normally starts with four discs. This rejects random screens.
        val occupied = black + white
        if (occupied < 4 || occupied > 64) {
            overlay.update("đang nhận bàn…")
            return
        }

        // In standard Othello black moves first. Move parity identifies whose turn it is.
        val movesPlayed = occupied - 4
        var side = if (movesPlayed % 2 == 0) Disc.BLACK else Disc.WHITE
        var best = Othello.best(board, side)
        if (best == null) {
            side = if (side == Disc.BLACK) Disc.WHITE else Disc.BLACK
            best = Othello.best(board, side)
        }

        if (best != null) {
            val x = left + ((best.col + 0.5f) * size / 8f).toInt()
            val y = top + ((best.row + 0.5f) * size / 8f).toInt()
            overlay.update("${best.text}  +${best.flips}", x, y)
        } else {
            overlay.update("không có nước")
        }
    }
}
