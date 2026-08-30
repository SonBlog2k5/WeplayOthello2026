package com.weplay.othello

enum class Disc { EMPTY, BLACK, WHITE }
data class Move(val row:Int,val col:Int,val flips:Int) { val text get() = "${('A'.code+col).toChar()}${row+1}" }

object Othello {
    private val dirs = arrayOf(-1 to -1,-1 to 0,-1 to 1,0 to -1,0 to 1,1 to -1,1 to 0,1 to 1)
    fun best(board:Array<DiscArray>, me:Disc): Move? {
        val opp = if(me==Disc.BLACK) Disc.WHITE else Disc.BLACK
        var best:Move?=null
        for(r in 0..7) for(c in 0..7) if(board[r][c]==Disc.EMPTY){
            val n=flips(board,r,c,me,opp)
            if(n>0 && (best==null || n>best!!.flips)) best=Move(r,c,n)
        }
        return best
    }
    private fun flips(b:Array<DiscArray>,r0:Int,c0:Int,me:Disc,opp:Disc):Int{
        var total=0
        for((dr,dc) in dirs){ var r=r0+dr; var c=c0+dc; var n=0
            while(r in 0..7 && c in 0..7 && b[r][c]==opp){n++;r+=dr;c+=dc}
            if(n>0 && r in 0..7 && c in 0..7 && b[r][c]==me) total+=n
        }; return total
    }
}
typealias DiscArray = Array<Disc>
