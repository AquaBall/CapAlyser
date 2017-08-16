package com.tye.capalyser

import android.content.Context
import android.util.Log
import android.widget.Toast

class MidiUtil {

    companion object {
        private const val TAG = "TyE (MidiUtil)"
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
            var sym = sym_.replace("[\\d]".toRegex(), "") // Dauer löschen
            if (sym=="_") return 1 // pausen als '1' (sehr tief) zurückgeben

            // Shifts registrieren und löschen
            var strich = -1     // Normalerweise: SVM 1 Oktav tiefer
            for (i in 0 .. 5) if (i != 1 && sym.contains(okt[i])) strich=i-2
            val rep= "[" + okt[0]+okt[2]+okt[3]+okt[4]+okt[5] + "]"
            sym = sym.replace(rep.toRegex(), "")

            return symPlain2Midi(sym) + strich * 12
        }

        private fun symPlain2Midi(sym: String): Int {
            tonLeiter.forEach {
                when (sym) {
                    it.symStd, it.mathStd, it.symHar,it.mathHar -> return it.midiFrequ
                }
            }
            return 0
        }

        fun playSequenz(context: Context, seq: String, maxTöne: Int = 99, tempo: Int = 200):Int {
            Toast.makeText(context ,seq, Toast.LENGTH_SHORT).show()
            val basisTempo= (300+tempo)*3
            val einzelTöne= seq.split(" +".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            var raster = 0
            for (nr in 0.. Math.min(einzelTöne.size, maxTöne)-1 ) {
                val einzelton= einzelTöne[nr]
                doSound(sym2Midi(einzelton), raster,  basisTempo/einzelton.dauer())
                raster += basisTempo/einzelton.dauer()
            }
            return raster
        }

        fun doSound(freq:Int, start: Int, end: Int) {
            java.util.Timer().schedule(
                    object : java.util.TimerTask() {
                        override fun run() {
                            try {
                                SoundManager.instance.playClickSound( freq )
                            } catch (e: Exception) {
                                Log.d("TyE (MidiUtil)","Abgefangen: ==> ${e.localizedMessage} bei Ton $freq" )
                            }
                        }
                    }, (start).toLong()    // Jeder Ton wird im Raster aktiviert.
            )
            // // TyE 2017-08-16, FIXME: unsauber: Ende erfolgt (noch) automatisch je nach TonDatei
        }

        val okt = arrayListOf(  ".",  "",  "'",  "\"",  "=",  "*")
        val duration = mapOf("1" to   "g", // "\uD834\uDD3B",
                "1/2" to   "h", // "\uD834\uDD3C",
                "1/4" to   "v", // "\uD834\uDD3D",
                "1/8" to   "a", // "\uD834\uDD3E",
                "1/16" to  "s", // "\uD834\uDD3F",
                "1/32" to  "z" // "\uD834\uDD40"
        )
        val durationN = mapOf("1" to   "1", // "\uD834\uDD3B",
                "1/2" to   "2", // "\uD834\uDD3C",
                "1/4" to   "4", // "\uD834\uDD3D",
                "1/8" to   "8", // "\uD834\uDD3E",
                "1/16" to  "6", // "\uD834\uDD3F",
                "1/32" to  "3" // "\uD834\uDD40"
        )
        // Aus wohlgeformten Tönen kann direkt gelesen werden:
        fun String.dauer(): Int  = if (this[0]=='6') 16 else if (this[0]=='3') 32 else this[0]-'0'
        fun String.ton  (): Char = this[1]
        fun String.oktav(): Int  = if (this.length < 3) Int.MAX_VALUE else this[2] - '0'
        fun String.vz   (): Char = if (this.length < 4) ' '  else this[3]
        fun oktave(ton:String): Int  = ton.oktav()
    }
        // TyE 2017-08-16, FIXME: aufgegeben:  Ich bring keine Notensymbole (UniCodeBlock) auf Android hin.
/*        // Unicode Symbole, (mal sehen, ob das funktioniert) geht auf Windows, aber nicht auf Android trotz eigenem Font
        val töne = mapOf("1" to   String(Character.toChars(0x1D15D)), // "\uD834\uDD5D",
                "1/2" to   String(Character.toChars(0x1D15E)), // "\uD834\uDD5E",
                "1/4" to   String(Character.toChars(0x1D15F)), // "\uD834\uDD5F",
                "1/8" to   String(Character.toChars(0x1D160)), // "\uD834\uDD60",
                "1/16" to  String(Character.toChars(0x1D161)), // "\uD834\uDD61",
                "1/32" to  String(Character.toChars(0x1D162)) // "\uD834\uDD62"
        )
        val pausen = mapOf("1" to   String(Character.toChars(0x1D13B)), // "\uD834\uDD3B",
                "1/2" to   String(Character.toChars(0x1D13C)), // "\uD834\uDD3C",
                "1/4" to   String(Character.toChars(0x1D13D)), // "\uD834\uDD3D",
                "1/8" to   String(Character.toChars(0x1D13E)), // "\uD834\uDD3E",
                "1/16" to  String(Character.toChars(0x1D13F)), // "\uD834\uDD3F",
                "1/32" to  String(Character.toChars(0x1D140)) // "\uD834\uDD40"
        )
*/
}
