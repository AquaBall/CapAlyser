package com.tye.capalyser

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/** A [FragmentPagerAdapter] that returns a fragment corresponding to one of the sections/tabs/pages. */
class clPageManager(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private val fragMap = LinkedHashMap<String, Fragment>()
    fun addAll(vararg fragments: Fragment) { fragments.forEach { fragMap.put(id(it),it) } }

// PreferenceFragment Geht nicht mit FragmentPagerAdapter
//    https://stackoverflow.com/questions/35142878/preferencefragment-with-appcompatactivity-android
//    fun addPref(frag: PreferenceFragment) { fragMap.put(fragMap.toString(),frag as Fragment) }

    private fun id(f: Fragment) = f.toString().substring(0,f.toString().indexOf("{"))
    fun nr(frag: Fragment) = fragMap.keys.indexOf(id(frag))

    override fun getItem(position: Int) =if (position<fragMap.size) ArrayList<Fragment>(fragMap.values).get(position) else frDummy.newInstance(position + 1)
    override fun getCount() = fragMap.size

    companion object { private const val TAG = "TyE (SelPageAdapt)" }
}

