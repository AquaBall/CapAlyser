package com.tye.capalyser

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/** [Fragment]-Klasse für Vorspiel der Lieder. Kommunizierende Activities müssen [frLieder.lstnLied] implementieren. */

class frLieder :  Fragment() {

    companion object { private const val TAG = "TyE (frLieder)" }
    fun titel()="Liederauswahl"

    private var mainGoLstn: lstnLied? = null

    override fun onDetach() { super.onDetach(); mainGoLstn = null }
    override fun onCreate(savInsState: Bundle?) { super.onCreate(savInsState) }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is lstnLied) mainGoLstn = context
        else throw RuntimeException(context.toString() + " must implement lstnLied")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.frag_lieder, container, false)

        if (view is RecyclerView) {
            view.layoutManager = LinearLayoutManager(view.context)
            view.adapter = frLiedViewAdapter(LiederListe.liedList, mainGoLstn)
        }
        return view
    }

    interface lstnLied {
        fun meldeGewählt() // wird in CapAlyserMain implementiert
    }
}
