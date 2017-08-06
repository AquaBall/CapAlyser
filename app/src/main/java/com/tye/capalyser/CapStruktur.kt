package com.tye.capalyser

class CapStruktur
(val element: String = ""  , var anzahl : Int =0) {
    val chronologisch: Int

    init {
        lfd++
        chronologisch = lfd
    }

    companion object {
        internal var lfd = 0
        fun resetChronologie() {
            lfd = 0
        }
    }

}
