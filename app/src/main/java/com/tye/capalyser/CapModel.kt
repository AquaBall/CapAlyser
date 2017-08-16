package com.tye.capalyser

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

//ToDo deaktiviert: import javafx.collections.FXCollections;
//ToDo deaktiviert: import javafx.collections.ObservableList;

internal class CapModel {

    private var capName: String = ""        // Die Capella Datei.
    fun isValid() = !(capName.isEmpty())
    val maxAkkord = 7

    @Suppress("unused")
    fun liedName(): String {
        var liedName = File(capName).name
        if (liedName.indexOf(".") > 0)
            liedName = liedName.substring(0, liedName.lastIndexOf("."))
        return liedName
    }

    val akkord = ArrayList<String>()

    var capDOM: Document? = null // die Daten in Baumform mit Nodes
    //ToDo deaktiviert: 	private ObservableList<CapStruktur>  struktur = FXCollections.observableArrayList();
    //ToDo deaktiviert: 	public ObservableList<CapStruktur>  getStruktur()  { return struktur; }

    var liedtext:   String = ""
    var liedAnfang: String = ""
    private val liedTexte   = ArrayList<ArrayList<String>>()    // 2Dim: Stimmen und Strophen
    private val liedAnfänge = ArrayList<ArrayList<String>>()    // 2Dim: Stimmen und Strophen
    private var tonVorzeichen = Int.MIN_VALUE
    val statistik = mutableListOf<CapStatistik>()

    @Suppress("unused") //Gar nicht war: Wird für Tonart benötigt.
    private val MU = MidiUtil() // Aber ich weiß nicht wie ich importieren muss.
    fun tonartSym() = MidiUtil.tonartSym(this.tonVorzeichen)

    /** Beinhaltet alle Texte der Datei: Titel/Autor/Anweisungen/Kommentare/Artikulationen(ein Buchstabe in Font Capella)
     */
    private inner class Text (var text: String, var system: Int, @Suppress("unused") internal var zeile: Int, @Suppress("unused") internal var stimme: Int)

    private val texte = ArrayList<Text>()

    // Fixtext, der in der ersten Zeile zuerst erscheint
    // Evl. Position prüfen	(könnte auch ein "Lebhaft" sein.)
    fun title(): String {
        var kandidat = texte
                .filter { (it.system == 1) }
                .filter { !it.text.contains("<Siggi") }
                .filter { !it.text.contains("\n") }
        if (kandidat.isEmpty())
            kandidat = texte
                    .filter { (it.system == 1) }
                    .filter { !it.text.contains("<Siggi") }
        if (kandidat.isEmpty())
            return "kein Titel"
        else
            return kandidat
                .first().text
    }

    // Fixtext, der in der ersten Zeile mehrzeilig ist
    // Evl. Position prüfen
    // evtl. Standard-Texte (frisch, majestätisch, schnell,...) ausfiltern?
    fun autor() = if (texte.size>0) texte
            .last { (it.system == 1) }.text else ""
//    Texte ausschließen, die NUR  "1. - 2."  enthalten

    // Fixtext, der als letztes erscheint
    fun editor() = if (texte.size>0) texte
            .last().text else ""

    // alle anderen Fixtexte
    fun kommentare(): String {
        var kom=""
        texte
                .filter { !it.text.contains(title())  }
                .filter { !it.text.contains(autor())  }
                .filter { !it.text.contains("<Siggi")  }
                .forEach{kom+= it.text + "\n"}
        return kom
    }

    /**	CapDatei wird geprüft und in das Modell aufgenommen.
     * Dabei wird die XML entzippt (auf Temp, und dann wieder gelöscht)
     * und der DOM-Baum erstellt
     * @param capDatei    aus dem Dateisystem
     */
    fun setupCapDatei(capDatei: File) {
        val tempFile = doUnzip(capDatei, "score.xml")    // in Win_Std_TEMP erstellen
        reSet()                // Clean Start vorbereiten
        if (tempFile == null) {
            System.err.println("kein SCORE gefunden")
            return
        }

        capName = capDatei.absolutePath    // Datei registrieren
        makeDOM(tempFile)        // XML-DOM aus extrahierter Datei erstellen
        tempFile.delete()        // Tempfile wieder löschen.
        if (!isValid()) {
            System.err.println("Defekt in XML!")
            return
        }

        fillStruktList()        // CapStruktur der XML durchzählen und registrieren
        fillStatistik()        // Systeme zählen

        sammleLiedtext()        // Der LiedText wird aufbereitet.
        sammleLiedAnfang()        // Der LiedAnfang (Spickzettel) wird aufbereitet.
    }

    private fun sammleLiedAnfang() {
        // ähnlich wie LiedText, aber nur 4 Worte
        liedAnfang = ""

        for (stimme in liedAnfänge) {
            for (strophe_ in stimme) {
                var zeile = strophe_
                        .trim { it <= ' ' }
                        .replace(" *- *".toRegex(), "")
                        .replace(" {2,}".toRegex(), " ")
                var j = 0
                for (i in 0..3) {
                    val j2 = zeile.indexOf(" ", j + 1)
                    if (j2 == -1) break
                    j=j2
                }
                if (j > 0) zeile = zeile.substring(0, j)
                liedAnfang += zeile + "\n"
            }
        }
    }


    private fun sammleLiedtext() {
        liedtext = ""

        var stimmNr = 1
        for (stimme in liedTexte) {
            if (stimme.size>0) {
                if (liedTexte.size>1) liedtext += "(Text $stimmNr):\n"
                stimmNr++
                var strophNr = 1
                for (strophe in stimme) {
                    liedtext += strophNr.toString() + ") " + strophe + "\n"
                    strophNr++
                }
                liedtext += "\n"
            }
        }
        liedtext = liedtext.replace(" *- *".toRegex(), "")
        liedtext = liedtext.replace(" {2,}".toRegex(), " ")
    }

    private fun reSet() {
        capName = ""
        liedTexte.clear()
        liedAnfänge.clear()
        //ToDo deaktiviert: 		struktur.clear();
        statistik.clear()
        texte.clear()
        tonVorzeichen = Int.MIN_VALUE

        currSystem = 0
        currZeile = 0
        currStimme = Int.MIN_VALUE
        currNote = 0
        akkord.clear()
    }

    private fun makeDOM(tempFile: File) {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder: DocumentBuilder
        try {
            dBuilder = dbFactory.newDocumentBuilder()
            capDOM = dBuilder.parse(tempFile)
        } catch(e: Exception) {
            System.err.println("DOM-ParseFehler: (File ungültig?) " + e.message)
        }
        finally {
            if (capDOM == null) {
                println("DOM ungültig")
                reSet()
            }
            else
                capDOM?.documentElement?.normalize()
        }
    }

    /** SCORE wird aus CAPX extrahiert nach TEMP.
     * @param zipDatei    CapellaDatei = umbenanntes ZIP
     * *
     * @param datName    Name der Datei, die nach TEMP entzippt werden soll
     * *
     * @return            TempDatei
     */
    private fun doUnzip(zipDatei: File, datName: String): File? {
        var tempFile = File("")
        try {
            val zis = ZipInputStream(FileInputStream(zipDatei))
            var ze: ZipEntry? = zis.nextEntry
            while (ze != null) {
                if (ze.name == datName) {
                    tempFile = File.createTempFile(datName, ".tmp")
                    val fos = FileOutputStream(tempFile)
                    zis.copyTo(fos)
                    fos.close()
                }
                ze = zis.nextEntry
            }
            zis.closeEntry()
            zis.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return tempFile
    }

    @Suppress("unused")
            /** Ein einzelnes Element wird in der XML-Datei gezählt.
     * @param strucktElement    Name des zu zählendes Elementes
     * *
     * @return Anzahl der Vorkommnisse, bei leerer CapStruktur wird -1 zurückgegeben
     */
    private fun countElement(strucktElement: String): Int = if (isValid()) XmlUtil.asList(capDOM?.getElementsByTagName(strucktElement)).size else -1

    private var currSystem = 0
    private var currZeile = 0
    private var currStimme = Int.MIN_VALUE
    private var currNote = 0

    private fun subVers(nNode: Element) {
        val silbe = nNode.textContent
        val strophNr = nNode.getAttribute("i").toInt() // Die Zeilennummer heißt schlicht 'i' in den Attributen
        val blank = if (nNode.getAttribute("hyphen").isEmpty())  " " else "" // Attr-"Hyphen" regelt die Silbentrennung

        while (currStimme >= liedTexte.size) liedTexte.add(ArrayList())    // evtl Stimmen auffüllen:
        val strophen = liedTexte[currStimme]
        while (strophNr >= strophen.size) strophen.add("")                        // evtl Strophen auffüllen:
        strophen[strophNr] = strophen[strophNr] + silbe.trim { it <= ' ' } + blank    // Silbe an die Strophe anhängen.

        // To Do: Warum behandle ich den Anfang anders, wenn ich eh nur 4 Worte nehme?
        // Wenn Text in 2. Stimme liegt, dann ist der Gesamt Texte nicht mehr relevant. (Aber finde ich die Strophe dann richtig?)
        if (currSystem == 1) {
            while (currStimme >= liedAnfänge.size) liedAnfänge.add(ArrayList())    // evtl Stimmen auffüllen:
            val strophenA = liedAnfänge[currStimme]
            while (strophNr >= strophenA.size) strophenA.add("")                        // evtl Strophen auffüllen:
            strophenA[strophNr] = strophen[strophNr]    // bisherige Strophe speichern.
        }
    }

    /** Aus der XML wird rekursiv der Liedtext in das Array Strophen/Stimmen extrahiert.
     * @param nodeRoot WurzelKnoten des XML-Document
     */
    private fun parse4Daten(nodeRoot: Node, statItem: CapStatistik) {
        var systemNr = 0
        for (node in XmlUtil.asList(nodeRoot.childNodes)) {        // Für jeden Subknoten:
            if (node.nodeType == Node.ELEMENT_NODE) {
                val nodeName = node.nodeName
                val nNode = node as Element
                var deutsch = ""
                when (nodeName) {
                    "system", "staff", "voice" -> {
                        systemNr++
                        when (nodeName) {
                            "system" -> {
                                deutsch = "System"
                                currSystem = systemNr
                                currStimme = 0        // Die stimmen zähle ich manuell, weil mir die Zeile egal ist
                            }
                            "staff" -> {
                                deutsch = "Zeile"
                                currZeile = systemNr
                            }
                            "voice" -> {
                                deutsch = "Stimme"
                                currStimme++    // nicht = systemNr!
                                currNote = 0
                            }
                        }
                        val neuSt = CapStatistik(deutsch+" " +systemNr)
                        parse4Daten(node, neuSt)    //Hier eine Rekursion aufrufen?? (Hat irgendwie mit Sammlern zu tun?)
                        statistik.add(neuSt)
                    }
                    "chord", "rest"  // Das sind die Container für Noten und pausen
                    -> {
                        if (currSystem == 1 && currNote < maxAkkord) currNote++
                        while (currStimme >= akkord.size) akkord.add("")  // evtl Stimmen auffüllen:

                        if (nodeName=="rest" && currSystem == 1 && currNote < maxAkkord) {
                            for (a in XmlUtil.asList(nNode.childNodes).filter { it.nodeName == "duration" }) {
                                var dauer:String=(a as Element).getAttribute("base")
//                                dauer = dauer.replace("1/","")        // Bruch verkürzen, oder
                                dauer = if (MidiUtil.pausen.get(dauer) != null) MidiUtil.duration.get(dauer).toString() else "?"
                                akkord[currStimme] = akkord[currStimme] + dauer
                            }
                            akkord[currStimme] = akkord[currStimme] + "_ "
                        }
                    }
                    "duration"
                    -> {
                        if (currSystem == 1 && currNote < maxAkkord) {
                            var dauer:String=nNode.getAttribute("base")
//                                dauer = dauer.replace("1/","")        // Bruch verkürzen, oder
                            dauer = if (MidiUtil.pausen.get(dauer) != null) MidiUtil.duration.get(dauer).toString() else "?"
                            akkord[currStimme] = akkord[currStimme] + dauer
//                            akkord[currStimme] = akkord[currStimme] + nNode.getAttribute("base").replace("1/","")
                        }
                        // nNode.getAttribute("dots") gäbe die Punktierung
                    }
                    // Das impliziert, dass wir nun im Container <heads> sind:
                    "head"        // <heads>  n*<head pitch="C5"/>  unsauber: 1x Heads, aber viele n x Head!
                    -> {
                        if (currSystem == 1 && currNote < maxAkkord) {
                            var ton = nNode.getAttribute("pitch").replace("B", "H")  // sofort auf 'deutsch' übersetzen
                            for (a in XmlUtil.asList(nNode.childNodes).filter { it.nodeName == "alter" }) {
                                    // Vorzeichen: <alter step=1: #	 step=-1: b  Auch Tonart wird berücksichtgt!
                                    // 'Auflösung' 	 <alter display="force"> und Step nach bedarf
                                    val s = (a as Element).getAttribute("step")
                                    if (s == "1") ton += "+"
                                    if (s == "-1") ton += "-"
                            }
                            akkord[currStimme] = akkord[currStimme] + ton + " "
                        }
                    }
                    "verse" -> {
                        statItem.silben++
                        subVers(nNode)
                    }
                    "stem" -> {
                    }
                    "beam" -> {
                    }
                    "keySign" -> if (tonVorzeichen < 0) {    // Ich lesen NUR die erste Tonart
                        tonVorzeichen = nNode.getAttribute("fifths").toInt()
                    }
                    "text" -> {
                        val txt = nNode.textContent.trim { it <= ' ' }
                        //					System.out.println("   ("+currSystem+"/"+currZeile+"/"+currStimme+")  '" + txt.replaceAll("\n", "_\\n_") + "'" )

                        if (txt.length > 2) {
                            statItem.fixtexte++
                            texte.add(Text(txt, currSystem, currZeile, currStimme))
                        } else {
                            statItem.artikulation++
                        }
                    }
                }
                statItem.noten++
                statItem.hälse++
                statItem.bögen++
                // Rekursion:
                //			bei folgenden Knoten tiefer gehen,
                // 			bereits gezählte dabei auslassen
                //			mit '_' Eindeutigkeit sichern
                //			und mit ' ' Doppelsprünge austricksen
                //			alle anderen Tags werden ignoriert (und nicht durchsucht).
                if (("_document_score_systems_ s y s t e m _staves_ s t a f f _voices_ v o i c e _noteObjects_chord_lyric_verse_" +
                        "_drawObjects_drawObj_text_barline_heads_head_stem_beam_").contains(nodeName + "_")) {
                    parse4Daten(node, statItem)  //	Rekursion!
                }
            }
        }
        if (tonVorzeichen < 0) tonVorzeichen = 0    //in C-dur wurde NICHTS notiert!
    }

    fun String.oktav() = MidiUtil.oktav(this)

    /** Die Normalisierung einer Melodie kann erst erfolgen, wenn alle Töne, aller Stimmen gelesen wurden.
     *  Suche niedrigste Oktav: Die Dauer wurde bereits codiert (leider (noch?) keine NotenSymbole)
     *  also ist nur mehr die Oktav als Zahl vorhanden.
     *  Vorher: Okt 3-6
     *  Nachher: gestrichen = ' "   kontra =  ,  (eigentlich auch groß + klein)
     */
    fun normalizeOktav() {
        var minOkt = Int.MAX_VALUE
        var maxOkt = Int.MIN_VALUE
        val count = arrayOf(0,0,0,0,0,0,0,0,0,0,0,0)     //QuickNDirty: vielleicht später besser
        akkord.forEach() {
            val einzelTöne= it.split(" +".toRegex()).filter{ it.isNotEmpty() }
            for (t in einzelTöne) {
//                print(" $t ")
                val okt = t.oktav()
                if (okt!=Int.MAX_VALUE) count[okt]++
                if (okt<minOkt)                         minOkt = okt
                if (okt>maxOkt && okt!=Int.MAX_VALUE)   maxOkt = okt
            }
        }
        // Eigentlich sollte Quantität mitentscheiden
//        println("TyE (CapModel): jetzt geht's los:  okt = $minOkt-$maxOkt  ")
        if ((count[minOkt+1] > count[minOkt] * 4) &&  count[minOkt-1]==0 ) minOkt++

        when (maxOkt - minOkt) {    // Ausrichtung nach Bedarf
            0,                  // bleibt ohne Oktavierung
            1,                  // erreicht 1 '
            2 -> minOkt -= 1    // erreicht 2 "
            3 -> minOkt += 0    // reicht von , bis "
            else -> println("TyE (CapModel): jetzt geht's schief:  okt = $minOkt-$maxOkt")
        }

        val ersatz = mapOf(('0'+minOkt).toString() to   MidiUtil.okt[0],     // "," geht niccht wg. Split später, anderes Komma?
                ('1'+ minOkt).toString() to  MidiUtil.okt[1],       // Hier will ich "", statt ' ', wegen nachfolgendem Zeichen
                ('2'+ minOkt).toString() to  MidiUtil.okt[2],
                ('3'+ minOkt).toString() to  MidiUtil.okt[3],
                ('4'+ minOkt).toString() to  MidiUtil.okt[4],   // Fehler
                ('5'+ minOkt).toString() to  MidiUtil.okt[5]    // Fehler
        )
        val iterate = akkord.listIterator()
        while (iterate.hasNext()) {
            var value = iterate.next()
            for((v,k) in ersatz) {
                value=value.replace(v,k)
            }
            iterate.set(value)
        }
    }

    fun codiereLänge() {
        val iterate = akkord.listIterator()
        while (iterate.hasNext()) {
            var value = iterate.next()
            for((v,k) in MidiUtil.duration) {
                value=value.replace(k,MidiUtil.durationN.getValue(v))
            }
            iterate.set(value)
        }
    }

    /** Die Systeme wird durchgezählt und aufgebaut.
     */
    private fun fillStatistik() {
        val neuSt = CapStatistik("neu")
        statistik.add(neuSt)
        // TyE 2017-08-16, FIXME: aufgegeben, NotenSymbole       for ( (k,v) in MidiUtil.pausen) println("TyE (CapModel): $k  =  ${v}  ${MidiUtil.töne.get(k)} ")
        parse4Daten(capDOM!!.documentElement, neuSt)
        normalizeOktav()
        codiereLänge()
    }

    /** Die ganze CapStruktur-Liste der XML-Datei wird gezählt und aufgebaut.
     */
    private fun fillStruktList() {
        // Für alle Knoten:
        CapStruktur.resetChronologie()
        for (node in XmlUtil.asList(capDOM?.getElementsByTagName("  *"))) {
            @Suppress("UNUSED_VARIABLE")
            val strucktElement = node.nodeName
            // ToDo? Das ist noch ziemlich primitiv:
            // Eigentlich sollte das mit 'contains' gehen	(Aber ich muss ja auch addieren?!)
            // oder HashList? (Aber geht die leicht ins TableView?)
            @Suppress("UNUSED_VARIABLE")
            val found = -1
            //ToDo deaktiviert: 			for (int i=0; i<struktur.size() && found<0; i ++) {
            //ToDo deaktiviert: 				if (struktur.get(i).getElement().equals(strucktElement)) { found =i; }
            //ToDo deaktiviert: 			}

            //ToDo deaktiviert: 			if (found<0) { struktur.add(new  CapStruktur(strucktElement,1));
            //ToDo deaktiviert: 			} else { struktur.get(found).incAnzahl();
            //ToDo deaktiviert: 			}
        }
    }

}
