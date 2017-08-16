package com.tye.capalyser

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.frag_play.*
import android.graphics.Typeface
import java.util.*
import android.widget.TextView



/** [Fragment]-Klasse für Vorspiel der Lieder. */
class frPlay : Fragment() {

    var vorspielZeit: Int = 0
    lateinit var akkord:  ArrayList<String> // Einzelzeilen in Prefs, brauch ich als Array.
    lateinit var prefs : clPrefs
    lateinit var ctx : Context

    fun updateInfo() {
        if (prefs.isValid) {
            try {
                var liedName = prefs.Lied
                liedName= liedName.substring(0,liedName.indexOf(".cap",0,true)) // Damit wird auch .tmp + Hash abgeschnitten
                // Strange fehler: Wenn ich hier setText, statt Property amwende, kommt es zu Null-Exeption.
                //  Wenn ich das Feld als editText lasse, dann wird es anders behandelt, und nicht immer aktualisiert
                tvDatei.text = liedName
                tvAnfang.text = prefs.liedAnfang
                tvTonart.text = getString(R.string.dur).format(prefs.tonartSym)
                akkord = arrayListOf(prefs.akkord1, prefs.akkord2, prefs.akkord3, prefs.akkord4, prefs.akkord5, prefs.akkord6)      // ich später im Array
                tvAkkorde.text = akkord.toString().replace(",".toRegex(), "\n").replace("\\[\n*".toRegex(), "").replace("]\n*".toRegex(), "")   // aus ArrayList
            } catch(e: Exception) {
                CapAlyserMain.log(TAG,"Screenprobleme:\n" + prefs.Lied +"\n" + prefs.liedAnfang +"\n" +e.localizedMessage) // ToDo
                val feedback = "Anzeigefehler!\nBitte Bildschirm \num 90° drehen und wieder retour."
                Toast.makeText(ctx,feedback,Toast.LENGTH_SHORT).show()
            }
        } else {
            tvDatei.text = ""
            tvAnfang.text=""
            tvTonart.text=""
            tvAkkorde.text=""
        }
        try {
            setButtons()
        } catch(e: Exception) {
        }
    }

    // TyE 2017-08-15, FIXME: Versuch, weil NotenFont nicht angezeigt wird!     https://stackoverflow.com/questions/20834610/custom-fonts-not-display-in-android-4-4
    // Ergebnis: bringt nichts
    object TypefaceClass {
        val cache = Hashtable<String, Typeface>()
        operator fun get(c: Context, assetPath: String): Typeface? {
            synchronized(cache) {
                if (!cache.containsKey(assetPath)) {
                    try {
                        val t = Typeface.createFromAsset(c.assets,
                                assetPath)
                        cache.put(assetPath, t)
                    } catch (e: Exception) {
                        return null
                    }
                }
                return cache.get(assetPath)
            }
        }
    }

    fun setFont(){  // TyE 2017-08-15, FIXME: Läuft alles nicht     weder OTF, nich TTF
//        tvAkkorde.typeface = TypefaceClass.get(context,"fonts/notomono-regular.otf")

        tvAkkorde.setTypeface(Typeface.createFromAsset(context.assets,"fonts/notomono-regular.otf"))

//        val tf = Typeface.createFromAsset(context.getAssets(), "fonts/notomono-regular.otf")
//        val tv = (TextView) findViewById(R.id.tvAkkorde)
//        tv.typeface = tf

        CapAlyserMain.log(TAG,"installed: " +tvAkkorde.typeface)
    }

    private fun setButtons() {
        setFont()
        // normierten Symbol des Tones angepasst, brauch ich für Tonvorspiel!
        fun sprungTon(ton:Int, vorzeichen:Int, halbe:Int, okt:Int)= MidiUtil.midi2Sym(ton+ halbe,vorzeichen<0) + (if (halbe >= okt) "'" else "")
        val valid = prefs.isValid
        var tonArtSym = MidiUtil.sym2Midi(prefs.tonartSym) // BasisTon bestimmen C = 48 (Tiefer als normal)
        val vorzeichen = MidiUtil.vorzeichenDur(prefs.tonartSym)
        ivTonart.setImageResource(imgTonart[vorzeichen+7])    // Midi würde 'C-dur'=60 zurückgeben

        if (swTonart.isChecked) {
            val oktSprung = MidiUtil.sym2Midi("C") - tonArtSym + 12
            if (tonArtSym < 0) tonArtSym += 12
            btS1.text = sprungTon(tonArtSym, vorzeichen,12,oktSprung)
            btS2.text = sprungTon(tonArtSym, vorzeichen, 7,oktSprung)
            btS3.text = sprungTon(tonArtSym, vorzeichen, 4,oktSprung)
            btS4.text = sprungTon(tonArtSym, vorzeichen, 0,oktSprung)
            sbDauer.progress = prefs.ArtDauer
        } else {
            // ToDo Stimmname könnte optimalerweise aus der Datei entnommen werden (nur 2 stellig?)
            btS1.text = getString(R.string.T1)
            btS2.text = getString(R.string.T2)
            btS3.text = getString(R.string.B1)
            btS4.text = getString(R.string.B2)
            sbDauer.progress = prefs.LiedDauer
        }
        // Alle Buttons evlt. disablen
        arrayOf(btS1, btS2, btS3, btS4, btAkkord, swTonart).forEach { it.isEnabled = valid }
        sbAnzahl.isEnabled = valid && !swTonart.isChecked
    }

    override fun onCreate(savInsState: Bundle?) { super.onCreate(savInsState) }

    override fun onViewCreated(v: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        ctx=context
        prefs = clPrefs(ctx)

        arrayOf(btS1, btS2, btS3, btS4, btAkkord).forEach { it.setOnClickListener({ spieleTon(it) })} // war ursprünglich direkt im XML: android:onClick="spieleTon"
        swTonart.setOnCheckedChangeListener { _, _ -> setButtons() }
        swTonart.isChecked= !prefs.Modus
        sbAnzahl.progress = prefs.LiedZahl -1
        CapAlyserMain.log(TAG, tvAkkorde.typeface.toString())
        setFont()
        CapAlyserMain.log(TAG, tvAkkorde.typeface.toString())
        SoundManager.initialize(ctx) // Muss frühzeitig vorbereitet werden, weil das Ding Zeit braucht?!?
        updateInfo()
    }


    fun spieleTon(view: View) {
        fun String.ersterTon()= this.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0] + " "
        val bt = view.id
        var töne = sbAnzahl.progress + 1             // Tonfolge aus slider
        vorspielZeit = sbDauer.progress * 250     // Spieldauer aus slider

        if (swTonart.isChecked) {                    // Tonart wird (meist) chorisch vorgespielt
            if (bt == R.id.btAkkord) {
                val anfang = btS4.text.toString() + " " + btS3.text + " " + btS2.text + " " + btS1.text + " "
                playSequenz(anfang, 4)    // wenns überhaupt 4 gibt!
            } else {                                    // AnfangsNote wird einzeln vorgespielt
                töne = 0
                playSequenz((view as Button).text.toString() + "", 1)
            }
        } else {
            if (bt == R.id.btAkkord) {
                töne = 4
                var anfang = ""
                if (akkord.size>4) anfang +=       akkord[4].ersterTon()
                if (akkord.size>3) anfang += " " + akkord[3].ersterTon()
                if (akkord.size>2) anfang += " " + akkord[2].ersterTon()
                if (akkord.size>1) anfang += " " + akkord[1].ersterTon()

                playSequenz(anfang, 4)    // wenns überhaupt 4 gibt!
            } else {                                    // Anfang wird squentiell vorgespielt
                try {
                    when (bt) {
                        R.id.btS1 -> playSequenz(akkord[1], töne)
                        R.id.btS2 -> playSequenz(akkord[2], töne)
                        R.id.btS3 -> playSequenz(akkord[3], töne)
                        R.id.btS4 -> playSequenz(akkord[4], töne)
                    }
                } catch(e: Exception) {
                    CapAlyserMain.log(TAG,"Vermutlich leere Stimme.")   // ToDo Fehler tritt schon bei AKkord auf, nicht erst bei PlaySequenz()
                }
            }
        }
        if (töne * vorspielZeit > 250) {      // ich kanns mir erlauben, die Anzeige NACH dem Abspielbefehl anzupassen!
            arrayOf(btS1, btS2, btS3, btS4, btAkkord).forEach { it.isEnabled=false }
        }
        Handler().postDelayed(// und die Wiederherstellung trotzdem sofort veranlassen.
                {  arrayOf(btS1, btS2, btS3, btS4, btAkkord).forEach { it.isEnabled=true }
                }, (vorspielZeit * töne).toLong())
    }

    fun playSequenz(seq: String, töne: Int) {
        MidiUtil.playSequenz(context,seq,töne,vorspielZeit)
    }

    override fun onAttachFragment(childFragment: Fragment?) {
        super.onAttachFragment(childFragment)
//        Toast.makeText(context,"Hallo Attach",Toast.LENGTH_SHORT).show()
//        CapAlyserMain.log(TAG,"Hallo Attach")
//        updateInfo()
    }

    override fun onPause() {
        super.onPause()
//        Toast.makeText(context,"Hallo Pause",Toast.LENGTH_SHORT).show()
//        CapAlyserMain.log(TAG,"Hallo Pause")
//        updateInfo()
    }
    override fun onResume() {
        super.onResume()
//        Toast.makeText(context,"Hallo Resume",Toast.LENGTH_SHORT).show()
//        CapAlyserMain.log(TAG,"Hallo Resume")
//        updateInfo()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.frag_play, container, false) }
    override fun onAttach(context: Context?) { super.onAttach(context) }
    override fun onDetach() { super.onDetach()}

    companion object {
        private const val TAG = "TyE (play)"
        var imgTonart = arrayOf(
                R.drawable.tonart_01_ces_dur_as_moll, R.drawable.tonart_02_ges_dur_es_moll,
                R.drawable.tonart_03_des_dur_b_moll, R.drawable.tonart_04_as_dur_f_moll,
                R.drawable.tonart_05_es_dur_c_moll, R.drawable.tonart_06_b_dur_g_moll,
                R.drawable.tonart_07_f_dur_d_moll, R.drawable.tonart_08_c_dur_a_moll,
                R.drawable.tonart_09_g_dur_e_moll, R.drawable.tonart_10_d_dur_h_moll,
                R.drawable.tonart_11_a_dur_fis_moll, R.drawable.tonart_12_e_dur_cis_moll,
                R.drawable.tonart_13_h_dur_gis_moll, R.drawable.tonart_14_fis_dur_dis_moll,
                R.drawable.tonart_15_cis_dur_ais_moll)
    }
    fun titel()="Vorspiel"
}
