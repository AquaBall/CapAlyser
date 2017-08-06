package com.tye.capalyser

import android.annotation.TargetApi
import android.os.Build
import java.io.File
import java.nio.charset.Charset
import java.util.*
import java.util.zip.ZipFile

const val CAP=".capx"
const val ZIP=".zip"
const val CHAR_MAXINT =65533.toChar()
const val CHAR_UNDERLINE ='_'


object LiederListe {

    /** LiedListe zur Anzeige im Listview über Adapter. */
    val liedList: MutableList<FilePls> = ArrayList()

    /** Erweiterung der [File]-Klasse zur Überprüfung auf [CAP] und [ZIP] */
    class FilePls(fName: String): File(fName) {
        /** Überprüft, ob Datei ([FilePls]-File) eine CapellaDatei ist, also  mit [CAP] endet. */
        fun isCap() =name.endsWith(CAP, true)
        /** Überprüft, ob Datei ([FilePls]-File) ein Zip-File ist, also  mit [ZIP] endet. */
        fun isZIP() =name.endsWith(ZIP, true)
    }

    init { fill() }

    /**  Füllt die Liedliste mit Zip-Dateien aus [CapAlyserMain.root], oder dem Inhalt der Zip-Datei [zip].
     *   Zur Verwendung im Listview über Adapter.
     *  @param [zip] (optional) Name der Zipdatei, oder Root wird verwendet */
    fun fill(zip: File = CapAlyserMain.root) {
        liedList.clear()
        if (zip == CapAlyserMain.root) {
            liedList.add(FilePls("Lieder-Pakete:"))
            zip.listFiles().filter { it.name.endsWith(".zip") }.forEach { liedList.add(FilePls(it.absolutePath)) }
        }
        else
        {
            liedList.add(FilePls(zip.absolutePath))
            var entries = ZipFile(zip).entries()  // Extended ASCII
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)  // nur neuere Handys können CharSet (Umlaute)
                entries = ZipFile(zip, Charset.forName("CP437")).entries()  // Extended IBM-ASCII

            while(entries.hasMoreElements()) {
                try {
                    val n= entries.nextElement().name.replace(CHAR_MAXINT, CHAR_UNDERLINE)
                    liedList.add(FilePls(n))
                } catch(e: Exception) {
                    liedList.add(FilePls("unlesbar"))   //Falls ein System doch noch keine Umlaute(?) kann
                }
            }
        }
    }
}
