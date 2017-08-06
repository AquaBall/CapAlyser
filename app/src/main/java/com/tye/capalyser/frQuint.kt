package com.tye.capalyser

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import kotlinx.android.synthetic.main.frag_stimm.*

/** [Fragment]-Klasse für Quintenzirkel.*/
class frQuint : Fragment() {

    companion object { private const val TAG = "TyE (quint)" }
    lateinit var prefs: clPrefs
    fun titel(): String {
        try { return if (prefs.Ansicht) "Stimmpfeife" else "Quintenzirkel"
        } catch(e: Exception) { // Beim Initialisieren gibt's ein Reihenfolge-Problem
            return "Anstimmen"
        }
    }

    fun play(btTon:View) {
        // Akkord abholen:
        val steps: MutableList<Int> = MidiUtil.akkTöne(spLeiter.selectedItemPosition).toMutableList()

        // Umkehrung einstellen:
        val umkehr= spAkkord.selectedItemPosition
        for (i in 1..umkehr) {
            if (!steps.contains(steps[0]+12)) steps.add(steps[0]+12)
            steps.removeAt(0)
        }
        // Evtl. Oktave dazu:
        if (swOktave.isChecked && !steps.contains(steps[0]+12)) steps.add(steps[0]+12)

        val ton =  (btTon as Button).tag.toString()  // Noch sehr 'freihändig'  sollte wohl mit Midi-Tonleiter abgestimmt sein
        val grundTon = MidiUtil.sym2Midi(ton)
        val oktSprung = MidiUtil.sym2Midi("C") - grundTon + 12
        val oktavierung = if (steps[0]>=oktSprung) -1 else 0

        // Gespielt wird dann aus dem String(!)
        var seq=""
        val vorzeichen = MidiUtil.vorzeichenDur(ton)  // + = #  - = b
        steps.forEach{ seq += MidiUtil.midi2Sym(grundTon +  it,vorzeichen<0) + (if (it + oktavierung*12 >= oktSprung) "'" else "") + " " }
        MidiUtil.playSequenz(context,seq, tempo = clPrefs(activity.applicationContext).ArtDauer * 250)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        arrayOf( R.id.btGes, R.id.btDes, R.id.btAs, R.id.btEs, R.id.btB, R.id.btF, R.id.btC, R.id.btG, R.id.btD, R.id.btA,
                 R.id.btE, R.id.btH, R.id.btFis).forEach { (this.view?.findViewById<View>(it) as Button) .setOnClickListener { play(it)}  } // Tricky: 2 unterschiedliche 'it'
        spLeiter.adapter =  ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, MidiUtil.akkNamen())
        // Ungelöst: 1) buttons selbst aus dem View herausfinden (ohne ArrayList)   2) ConstraintBias setzen, (um Verschieben berechnen zu können)
    }

    override fun onCreate(savInsState: Bundle?) { super.onCreate(savInsState)   }
    override fun onAttach(context: Context?)    { super.onAttach(context)       }
    override fun onDetach()                     { super.onDetach()              }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val prefs = clPrefs(context)
        val frag_typ = if (prefs.Ansicht) R.layout.frag_quint else R.layout.frag_stimm
        return inflater!!.inflate(frag_typ, container, false)
    }
}
