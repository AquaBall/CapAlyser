package com.tye.capalyser

import android.annotation.TargetApi
import android.graphics.Color
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.tye.capalyser.LiederListe.FilePls
import com.tye.capalyser.frLieder.lstnLied
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/** [RecyclerView.Adapter] that can display a [FilePls] and makes a call to the specified [lstnLied]. */
class frLiedViewAdapter(private val liedListe: List<FilePls>,
                        private val mainGoLstn: lstnLied?) :
        RecyclerView.Adapter<frLiedViewAdapter.LiederListView>() {

    companion object { private const val TAG = "TyE (ViewAdapt)" }

    lateinit var view: View

    inner class LiederListView(mView: View) : RecyclerView.ViewHolder(mView) {
        val txTyp: TextView = mView.findViewById<View>(R.id.txTyp) as TextView
        val txTitel: TextView = mView.findViewById<View>(R.id.tyTitel) as TextView

        init {
            mView.setOnClickListener { click(adapterPosition) } // Woher kommt die Pos denn?
            view =mView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiederListView {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frag_lied_detail, parent, false)
        return LiederListView(view)
    }

    override fun onBindViewHolder(liederList: LiederListView, position: Int) {
        val datei = liedListe[position]
        val typ  = liederList.txTyp
        val name = liederList.txTitel
            val n = datei.name.replace(CAP, "", true)
            name.setBackgroundColor(Color.WHITE)
            typ.setBackgroundColor(Color.WHITE)
            if (datei.isDirectory || datei.isZIP()) {
                typ.text = "+"
                name.setTextColor(Color.BLUE)
                name.text= n
            } else if (!datei.isCap()) {
                if (position != 0) typ.text = "  ?"
                name.setTextColor(Color.GRAY)
            } else {
                typ.text = ""
                name.setTextColor(Color.BLACK)
            }
            name.text = n
    }

    fun click(position: Int) {
        notifyDataSetChanged()
        if (position == 0) {
            LiederListe.fill()
            return
        }
        val selected = liedListe[position]
        if (selected.isZIP()) LiederListe.fill(File(selected.absolutePath))
        if (selected.isCap()) {
            data2Prefs(File(liedListe[0].absolutePath), selected.name)
//            Handler().postDelayed(  // Test, ob das Fragemnt mehr Zeit braucht:
//                {     //Weil funktioniert nach Rotation nicht mehr richtig
                    mainGoLstn!!.meldeGewählt()
//                }, (2000).toLong())       // Nutzt nichts
        }
    }
    override fun getItemCount(): Int = liedListe.size

    fun data2Prefs(zip: File, lied: String){
        CapAlyserMain.log(TAG, "Gewählt wurde: $zip/$lied")

        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
        var feedback = "neues Lied wählen!"
        val model = CapModel()
        val prefs = clPrefs(view.context)
        prefs.Zip = zip.absolutePath
        prefs.Lied = lied
        val unzipped = unZippFile(zip, lied)        //  <<--- Zip holen
        model.setupCapDatei(unzipped)               //  <<--- Modell lesen
        prefs.isValid = model.isValid()
        if (prefs.isValid) {                        //  <<-- in Prefs übernehmen
            prefs.liedAnfang  = model.liedAnfang
            prefs.tonartSym   = model.tonartSym()
            val ak = model.akkord
                prefs.akkord1     = if (ak.size>0) ak[0] else ""
                prefs.akkord2     = if (ak.size>1) ak[1] else ""
                prefs.akkord3     = if (ak.size>2) ak[2] else ""
                prefs.akkord4     = if (ak.size>3) ak[3] else ""
                prefs.akkord5     = if (ak.size>4) ak[4] else ""
                prefs.akkord6     = if (ak.size>5) ak[5] else ""
            prefs.title       = model.title()
            prefs.autor       = model.autor()
            prefs.liedtext    = model.liedtext
            prefs.kommentare  = model.kommentare()
            feedback = "ok!"
        } else {
            prefs.liedAnfang  = ""
            prefs.tonartSym   = ""
            prefs.akkord1     = ""
            prefs.akkord2     = ""
            prefs.akkord3     = ""
            prefs.akkord4     = ""
            prefs.akkord5     = ""
            prefs.akkord6     = ""
            prefs.title       = ""
            prefs.autor       = ""
            prefs.liedtext    = ""
            prefs.kommentare  = ""
            feedback = "Cap ungültig?"
        }
        if (unzipped.name.endsWith(".tmp",true)) unzipped.delete()
        Toast.makeText(view.context, feedback, Toast.LENGTH_SHORT).show()
    }

    private fun unZippFile(zipDatei: File, lied: String): File {

        var zf= ZipFile(zipDatei)       // Läuft, kann aber keine Umlaute:  .nextElement() crasht!
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)  // nur neuere Handys können CharSet (Umlaute)
            zf= ZipFile(zipDatei, Charset.forName("CP437"))      // Estended IBM-ASCII

        val zes = zf.entries()
        while(zes.hasMoreElements()){
            val ze: ZipEntry?
            try {
                ze = zes.nextElement()      // Stürzt evtl ab!
            } catch(e: Exception) {
                continue
            }
            val n= ze!!.name.replace(CHAR_MAXINT, CHAR_UNDERLINE)
            if (n == lied) {
                val tempFile = File.createTempFile(n, ".tmp")
                val fos = FileOutputStream(tempFile)
                zf.getInputStream(ze).copyTo(fos)
                fos.close()
                return tempFile
            }
        }
        CapAlyserMain.log(TAG,"Warum wird Zip '$lied' aus '$zipDatei' nicht gefunden?")
        return File("")
    }

}
