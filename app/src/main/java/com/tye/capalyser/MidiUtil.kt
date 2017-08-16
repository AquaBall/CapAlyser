package com.tye.capalyser

import android.content.Context
import android.util.Log
import android.widget.Toast

class MidiUtil {

    companion object {
        private var akkords= arrayOf(
            Akkord ("Dur"           , arrayOf(0, 4, 7) ),
            Akkord ("Moll"          , arrayOf(0, 3, 7) ),
            Akkord ("Dur_7"         , arrayOf(0, 4, 7, 10) ),
            Akkord ("Dur_maj7"      , arrayOf(0, 4, 7, 11) ),
            Akkord ("Moll_m 7"      , arrayOf(0, 3, 7, 10) ),
            Akkord ("Moll_m maj7"   , arrayOf(0, 3, 7, 11) ),
            Akkord ("Vermindert"    , arrayOf(0, 3, 6) ),
            Akkord ("Übermäßig"     , arrayOf(0, 4, 8) )
        )
        private var quintenzirkel = arrayOf(        // ToDo nächstes Vorzeichen ergänzen
            TonArt("Ces", "as",  -7),
            TonArt("Ges", "es",  -6),
            TonArt("Des", "b",   -5),
            TonArt("As",  "f",   -4),
            TonArt("Es",  "c",   -3),
            TonArt("B",   "g",   -2),  // Capella hat hier (international) "Bes"
            TonArt("F",   "d",   -1),
            TonArt("C",   "a",    0),
            TonArt("G",   "e",    1),
            TonArt("D",   "h",    2),
            TonArt("A",   "fis",  3),
            TonArt("E",   "cis",  4),
            TonArt("H",   "gis",  5),  // Capella hat hier (international) "B"
            TonArt("Fis", "dis",  6),
            TonArt("Cis", "ais",  7)
        )
        private var tonLeiter = arrayOf(// Ais/Hes  Eis/Fes werden nicht verwendet.
            Ton("C",   "",    "H+", "",   60),
            Ton("Cis", "Des", "C+", "D-", 61),
            Ton("D",   "",    "",   "",   62),
            Ton("Dis", "Es",  "D+", "E-", 63),
            Ton("E",   "",    "",   "F-", 64),
            Ton("F",   "",    "E+", "",   65),
            Ton("Fis", "Ges", "F+", "G-", 66),
            Ton("G",   "",    "",   "",   67),
            Ton("Gis", "As",  "G+", "A-", 68),
            Ton("A",   "",    "",   "B-", 69),
            Ton("B",   "",    "A+", "H-", 70),  // Deutsche Variante
            Ton("H",   "",    "B+", "C-", 71)   // Deutsche Variante
        )

        private class TonArt (internal val symDur: String, internal val symMoll: String, internal val vorzeichen: Int)
        private class Akkord (internal val name: String, internal val töne: Array<Int>)
        private class Ton (
                internal val symStd: String,
                internal val symHar: String,
                internal val mathStd: String,  // 'mathematische' Bezeichnung
                internal var mathHar: String,  // 'mathematische' Bezeichnung
                internal val midiFrequ: Int
        )

        fun akkTöne(nr:Int): Array<Int> = akkords[nr].töne
        fun akkNamen(): MutableList<String> {
            val steps = mutableListOf<String>()
            akkords.forEach { steps.add(it.name) }
            return steps
        }

        /** Gibt die Tonart mit n'#' zurück
         * @param vorzeichen Anzahl der '#' (negativ = 'b') */
        fun tonartSym(vorzeichen: Int): String {
            var found = quintenzirkel.findLast{ it.vorzeichen == vorzeichen }
            if (found == null) found= quintenzirkel[quintenzirkel.size/2] // 'C' ist der Dummy
            return found.symDur
        }

        /** Gibt die Anzahl der '#' zur Tonart zurück
         *  @param sym  String */
        fun vorzeichenDur(sym: String): Int {
            val found = quintenzirkel.findLast{ it.symDur == sym}
            if (found == null) return 0  // 'C' ist Dummy
            else return found.vorzeichen
        }

        /** Gibt die laufende Nr der Tonart zurück ('C' = 7)
         *  @param sym  String */
        fun tonartNrDur(sym: String)= vorzeichenDur(sym) + quintenzirkel.size/2 // 'C' sitzt in der Mitte

        /**
         *
         */
        fun midi2Sym(midi: Int, enhar: Boolean = false): String {
            val t =tonLeiter[midi % tonLeiter.size]
            return if (!enhar || t.symHar =="") t.symStd else t.symHar
        }

        fun sym2Midi(sym_: String): Int {
            var sym = sym_

            // Shifts registrieren und löschen
            var shift = 0
            if (sym.contains(MidiUtil.okt[0])) shift=-1
            if (sym.contains(MidiUtil.okt[2])) shift=1
            if (sym.contains(MidiUtil.okt[3])) shift=2
            if (sym.contains(MidiUtil.okt[4])) shift=3
            if (sym.contains(MidiUtil.okt[5])) shift=4
            sym = sym.replace("[.'\"]".toRegex(), "")    // TyE 2017-08-16, FIXME: unsauber, weil nicht variabel!
//            sym = sym.replace("["+MidiUtil.okt[0]+MidiUtil.okt[2]+MidiUtil.okt[3]+MidiUtil.okt[4]+MidiUtil.okt[5]+"]".toRegex(), "")

            val oktC = "" //sym.replace("[^\\d]".toRegex(), "")     // ALte Version las aus der Oktave
            sym = sym.replace("[\\d]".toRegex(), "") // registrierte Oktave (und Dauer) löschen

            if (sym=="_") return 1 // pausen als '0' zurückgeben

            var okt = if (oktC.length == 1) oktC[0] - '5' else -1 // Ohne Angabe wird 1 Oktave unter C4 (=60) also 48 begonnen.
            okt += shift
            Log.d("TyE (MidiUtil)","$sym_ => $sym /  $okt  = ${symPlain2Midi(sym)}")
            return symPlain2Midi(sym) + okt * 12
        }

        private fun symPlain2Midi(sym: String): Int {
            tonLeiter.forEach {
                when (sym) {
                    it.symStd, it.mathStd, it.symHar,it.mathHar -> return it.midiFrequ
                }
            }
            return 0
        }

        fun playSequenz(context: Context, seq: String, maxTöne: Int = 99, tempo: Int = 200) {
            Toast.makeText(context ,seq, Toast.LENGTH_SHORT).show()
            val einzelTöne= seq.split(" +".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            for (nr in 1.. Math.min(einzelTöne.size, maxTöne)) {
                java.util.Timer().schedule(
                        object : java.util.TimerTask() {
                            override fun run() {
                                try {
                                    SoundManager.instance.playClickSound( sym2Midi(einzelTöne[nr-1]) )
                                } catch (e: Exception) {
                                    Log.d("TyE (MidiUtil)","${e.localizedMessage} bei Ton ${einzelTöne[nr-1]} = ${sym2Midi(einzelTöne[nr-1])}" )

                                }
                            }
                        }, (tempo * nr).toLong()    // Jeder Ton wird nach einander aktiviert.
                ) // Ende erfolgt automatisch je nach TonDatei
            }
        }

        val okt = arrayListOf(".","","'","\"","=","*")
        // Unicode Symbole, (mal sehen, ob das funktioniert) geht auf Windows, aber auf ANdroid fehlt (anscheinend) ein Font
        // Ton 1D15D   1D15E   1D15F   1D160   1D161   1D162
        // Paus1D13B   1D13C   1D13D   1D13E   1D13F   1D140
        // TyE 2017-08-16, FIXME: aufgegeben:  Ich bring keine Notensymbole (UniCodeBlock) auf Android hin.
//                "1/2" to   Character.toChars(0x1D15E), // "\uD834\uDD5E",
        val töne = mapOf("1/1" to   String(Character.toChars(0x1D15D)), // "\uD834\uDD5D",
                "1/2" to   String(Character.toChars(0x1D15E)), // "\uD834\uDD5E",
                "1/4" to   String(Character.toChars(0x1D15F)), // "\uD834\uDD5F",
                "1/8" to   String(Character.toChars(0x1D160)), // "\uD834\uDD60",
                "1/16" to  String(Character.toChars(0x1D161)), // "\uD834\uDD61",
                "1/32" to  String(Character.toChars(0x1D162)) // "\uD834\uDD62"
        )
        val pausen = mapOf("1/1" to   String(Character.toChars(0x1D13B)), // "\uD834\uDD3B",
                "1/2" to   String(Character.toChars(0x1D13C)), // "\uD834\uDD3C",
                "1/4" to   String(Character.toChars(0x1D13D)), // "\uD834\uDD3D",
                "1/8" to   String(Character.toChars(0x1D13E)), // "\uD834\uDD3E",
                "1/16" to  String(Character.toChars(0x1D13F)), // "\uD834\uDD3F",
                "1/32" to  String(Character.toChars(0x1D140)) // "\uD834\uDD40"
        )
        val duration = mapOf("1/1" to   "g", // "\uD834\uDD3B",
                "1/2" to   "h", // "\uD834\uDD3C",
                "1/4" to   "v", // "\uD834\uDD3D",
                "1/8" to   "a", // "\uD834\uDD3E",
                "1/16" to  "s", // "\uD834\uDD3F",
                "1/32" to  "z" // "\uD834\uDD40"
        )
        val durationN = mapOf("1/1" to   "1", // "\uD834\uDD3B",
                "1/2" to   "2", // "\uD834\uDD3C",
                "1/4" to   "4", // "\uD834\uDD3D",
                "1/8" to   "8", // "\uD834\uDD3E",
                "1/16" to  "6", // "\uD834\uDD3F",
                "1/32" to  "3" // "\uD834\uDD40"
        )
        // tonanalye:   [dezDauer] [alphaTon] [Oktave] {VZ}
        // fun String.tonArr() = this.split("(?<=[A-H/+/-/'])|(?=[A-H/+/-/'])".toRegex()).filter { it.isNotEmpty() } scheiter an "3-"
        // fun String.tonArr() = this.split("(?<=[0-9,'])|(?=[0-9,'])".toRegex()).filter { it.isNotEmpty() }  scheitert an 2zelligen Zahlen

        fun String.is2stellig() = if (this[1]>='0' && this[1]<='9') 1 else 0
        fun dauer(ton:String): Int {
            return ton.substring(0,1+ton.is2stellig()).toInt()
        }
        fun ton(ton:String): Char {
            return ton[1+ton.is2stellig()]
        }
        fun oktav(ton:String): Int {
            println( " okt: "+ ton)
            if (ton(ton)=='_') return Int.MAX_VALUE
            return ton[2+ton.is2stellig()] - '0'
        }
        fun vz(ton:String): Char {
            if (ton(ton)=='_') return ' '
            if (ton.length < 4+ ton.is2stellig()) return ' '
            return ton[3+ton.is2stellig()]
        }
    }
}
