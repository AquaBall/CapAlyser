package com.tye.capalyser

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.EditText
import kotlinx.android.synthetic.main.frag_setting.*


/** [Fragment]-Klasse für Vorspiel der Lieder. */  // Schade, dass PreferenceFragment hier (im SliderView) nicht funktioniert!
class frSetting : Fragment() {
    companion object { private const val TAG = "TyE (setting)" }
    fun titel() = "Setting"
    lateinit var prefs : clPrefs
    val seiten = arrayOf(frLieder().titel(), frQuint().titel(), frDisclaimer().titel())


    private var nachtrag = ""
    fun updateLog(msg: String, newLine:Boolean = true) {
        Log.d(TAG, msg)
        val tx= msg + if (newLine) "\n" else ""
        try {                                           // ToDo Kann doch nicht wahr sein, diese Konstruktion: Wie kann ich sicherstellen, dass View bereit ist?
            txLog.text = "%s%s%s".format(txLog.text,nachtrag ,tx)
            nachtrag=""
        } catch(e: Exception) {
            nachtrag += tx      // merken für späteren Nachtrag
            Log.d(TAG, "    !Log-Screen nicht bereit!")
        }
    }

    fun kommentiereSetting()  {
        fun dauer2Text(ed:EditText) =if (ed.text.toString().toInt()==0) "chorisch " else "als "+ ed.text.toString() +"/4 "
        txModusKom.text     = getString(R.string.txModusKom)    .format(if (txModusS.isChecked) getString(R.string.txMelodie) else getString(R.string.txTonart))
        txDiscKom.text      = getString(R.string.txDiscKom)     .format(if (txDiscS.isChecked) "" else getString(R.string.txNicht))
        txLiedZahlKom.text  = getString(R.string.txLiedZahlKom) .format(txLiedZahlT.text.toString().toInt())
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            txLiedDauerKom.text = getString(R.string.txLiedDauerKom).format(dauer2Text(txLiedDauerT))
            txArtDauerKom.text  = getString(R.string.txArtDauerKom) .format(dauer2Text(txArtDauerT))
        } else {
            txLiedDauerKom.text = getString(R.string.txDauerKomL).format(dauer2Text(txLiedDauerT))
            txArtDauerKom.text  = getString(R.string.txDauerKomL) .format(dauer2Text(txArtDauerT))
        }
    }

    fun initFields() {
        txPrimaryP.adapter =  ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, seiten)
        txPrimaryP.setSelection(seiten.indexOf(prefs.Screen))
        txModusS.isChecked=prefs.Modus
        txDiscS.isChecked =prefs.Disc
        txLiedDauerT.setText(prefs.LiedDauer.toString())
        txLiedZahlT.setText(prefs.LiedZahl.toString())
        txArtDauerT.setText(prefs.ArtDauer.toString())

        txModusS.setOnCheckedChangeListener    { _, _ -> prefs.Modus = txModusS.isChecked; kommentiereSetting() }
        txDiscS.setOnCheckedChangeListener     { _, _ -> prefs.Disc = txDiscS.isChecked;  kommentiereSetting() }
        txLiedDauerT.setOnEditorActionListener { v, _, _ -> prefs.LiedDauer = checkRange(v,prefs.LiedDauer,0..3); true }
        txLiedZahlT.setOnEditorActionListener  { v, _, _ -> prefs.LiedZahl = checkRange(v,prefs.LiedZahl, 1..8); true }
        txArtDauerT.setOnEditorActionListener  { v, _, _ -> prefs.ArtDauer = checkRange(v,prefs.ArtDauer,0..3); true }

        //  ohne 'post'  crasht jede Rotation! https@ //stackoverflow.com/questions/2562248/how-to-keep-onitemselected-from-firing-off-on-a-newly-instantiated-spinner
        txPrimaryP.post(Runnable {  txPrimaryP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {  prefs.Screen =seiten[position] }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        } })
        swShowLog.setOnCheckedChangeListener   { _, _ ->
            updateLog("",false) // um Nachträge auszulösen
            txLog.visibility= if (swShowLog.isChecked) View.VISIBLE else View.GONE
        }
        kommentiereSetting()
    }

    fun checkRange(v: TextView, alt: Int, range: IntRange):Int  {
        var wert = v.text.toString().toInt()
        if (! (wert in range)) {
            Toast.makeText(context, "nur $range", Toast.LENGTH_SHORT).show()
            v.setText(alt.toString())
            wert = alt
        }
        kommentiereSetting()
        return wert
    }

    override fun onViewCreated(v: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        fbReload.setOnClickListener { Snackbar.make(it, getString(R.string.txReload), Snackbar.LENGTH_LONG).setAction("Action", null).show() }
        prefs = clPrefs(context)
        initFields()
    }

    override fun onCreate(savInsState: Bundle?)  { super.onCreate(savInsState) }
    override fun onAttach(context: Context?)     { super.onAttach(context) }
    override fun onDetach()                      { super.onDetach() }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savInsState: Bundle?): View? {
        return inflater!!.inflate(R.layout.frag_setting, container, false)
    }
}
