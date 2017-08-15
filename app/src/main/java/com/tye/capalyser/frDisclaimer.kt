package com.tye.capalyser

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.frag_disclaimer.*
import java.io.File


/** [Fragment]-Klasse für EröffnungsBildschirm. Main muss [frDisclaimer.clickArrowListener] implementieren. */
class frDisclaimer : Fragment() {
    companion object { private const val TAG = "TyE (discl)" }
    fun titel()= "Begrüßung"

    private lateinit var mListener: clickArrowListener

    @TargetApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arrLeft.setOnClickListener  { mListener.onClickArrow(it) }
        arrRight.setOnClickListener { mListener.onClickArrow(it) }

        val disc = File(CapAlyserMain.root,"Disclaimer.html").readText()

//        java.lang.NoSuchMethodError: No static method fromHtml(Ljava/lang/String;I)Landroid/text/Spanned;
//              in class Landroid/text/Html; or its super classes (declaration of 'android.text.Html' appears in /system/framework/framework.jar)
//              at com.tye.capalyser.frDisclaimer.onViewCreated(frDisclaimer.kt:33)
//        txDisclaim.text = Html.fromHtml(disc,Html.FROM_HTML_MODE_COMPACT)       // TyE 2017-08-15, FIXME. Nicht Deprecated, aber stürzt ab!  .. obwohl nur auf realem Handy!!
//        txDisclaim.text = Html.fromHtml(disc,Html.FROM_HTML_MODE_LEGACY )       // TyE 2017-08-15, FIXME. Nicht Deprecated, aber stürzt ab!  .. obwohl nur auf realem Handy!!
        @Suppress("DEPRECATION")
        txDisclaim.text = Html.fromHtml(disc)             // TyE 2017-08-15, FIXME: unsauber:  Deprecated, aber funktioniert!
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.frag_disclaimer, container, false)
    }

    override fun onCreate(savInsState: Bundle?) { super.onCreate(savInsState) }
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is clickArrowListener) mListener = context
        else throw RuntimeException(context.toString() + " must implement clickArrowListener")
    }

    override fun onDetach() { super.onDetach() }

    interface clickArrowListener{
        fun onClickArrow(view: View)    // wird in CapAlyserMain implementiert, weil von mViewPager verwaltet
    }

}
