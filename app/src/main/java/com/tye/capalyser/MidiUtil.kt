package com.tye.capalyser

import android.content.Context
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
            val shift = sym.contains("'")
            sym = sym.replace("'".toRegex(), "")    // registriertes shift löschen

            val oktC = sym.replace("[^\\d]".toRegex(), "")
            sym = sym.replace("[\\d]".toRegex(), "") // registrierte Oktave löschen

            var okt = if (oktC.length == 1) oktC[0] - '5' else -1 // Ohne Angabe wird 1 Oktave unter C4 (=60) also 48 begonnen.
            if (shift) okt++

            val midi = symPlain2Midi(sym) + okt * 12
//            println("($sym_ ->) $sym -> $midi  ('$okt)")
            return midi
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
                            override fun run() { SoundManager.instance.playClickSound( sym2Midi(einzelTöne[nr-1]) ) }
                        }, (tempo * nr).toLong()    // Jeder Ton wird nach einander aktiviert.
                ) // Ende erfolgt automatisch je nach TonDatei
            }
        }
    }
}
