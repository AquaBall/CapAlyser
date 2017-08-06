package com.tye.capalyser
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.frag_data.*

class frData : Fragment() {

    companion object { private const val TAG = "TyE (data)" }

    fun titel() = "Daten"
    lateinit var prefs : clPrefs

    fun updateInfo() {
        if (prefs.isValid) {
            try {
                txTitel.text= prefs.title
                txAutor.text= prefs.autor
                txText .text= prefs.liedtext
                txKomm .text= prefs.kommentare
            } catch(e: Exception) {
                CapAlyserMain.log(TAG,"Ist Data nicht bereit? (beim Rotieren?): "+e.localizedMessage) // ToDo
            }
        } else {
            txTitel.text="T?"
            txAutor.text="A?"
            txText.text="L?"
            txKomm.text="K?"
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = clPrefs(context)
        updateInfo()
    }
    override fun onCreate(savInsState: Bundle?) { super.onCreate(savInsState)   }
    override fun onAttach(context: Context?)    { super.onAttach(context)       }
    override fun onDetach()                     { super.onDetach()              }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater!!.inflate(R.layout.frag_data, container, false)
}
