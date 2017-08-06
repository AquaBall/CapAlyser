package com.tye.capalyser

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CapAlyserMain : frLieder.lstnLied, frDisclaimer.clickArrowListener, AppCompatActivity() {

    companion object {
        private const val TAG = "TyE (main)"
        // Wenn ich hier keine Instanzen erstelle, werden die Felder doppel/falsch initailisiert
        @SuppressLint("StaticFieldLeak")
        val fPlay = frPlay(); val fData = frData(); val fSetting = frSetting(); val fDisclaimer=frDisclaimer()
        lateinit var root: File     //  = filesDir   // Geht im CompanionObject leider noch nicht!
        fun log(tag: String, msg:String){ fSetting.updateLog("$tag $msg") }     // sendet ein log an den SettingsScreen
    }

    /** The [ViewPager] that will host the section contents. */
    lateinit private var mViewPager: ViewPager
    private var seitenManager = clPageManager(supportFragmentManager)

    // Übergabe zwischen den Fragmenten:
    override fun meldeGewählt() {  // Zip aus frLieder an frPlay und frData
        fPlay.updateInfo() //Das ist eigentlich doppelt gemoppelt. (sollte im frPlay selbst passieren)
        fData.updateInfo() //Das ist eigentlich doppelt gemoppelt. (sollte im frPlay selbst passieren)
        fPlay.show()
    }
    override fun onClickArrow(view: View) { when (view.id) {R.id.arrLeft -> frQuint().show();  R.id.arrRight -> frLieder().show() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)

        setSupportActionBar(findViewById<View>(R.id.toolbar) as Toolbar)

        val prefs = clPrefs(applicationContext)
        val preBegrues = prefs.Disc

        if (preBegrues) seitenManager.addAll( fSetting, frQuint(), fDisclaimer, frLieder(), fPlay, fData )//, MyPreferenceFragment() as Fragment) Cast geht nicht
        else seitenManager.addAll( fSetting, frQuint(), frLieder(), fPlay, fData )//, MyPreferenceFragment() as Fragment) Cast geht nicht


        mViewPager = findViewById<View>(R.id.container) as ViewPager
        mViewPager.adapter = seitenManager
        mViewPager.offscreenPageLimit = 8       // Wichtig! Sonst kann ich nicht alle Seiten aktualisieren!!!
        when (prefs.Screen) {
            fSetting.titel()   -> fSetting.show()
            frQuint().titel()  -> frQuint().show()
            frLieder().titel() -> frLieder().show()
            fPlay.titel()      -> fPlay.show()
            fData.titel()      -> fData.show()
            fDisclaimer.titel()-> if (preBegrues) fDisclaimer.show() else frLieder().show()
            else -> if (preBegrues) fDisclaimer.show() else frLieder().show()
        }
        root=filesDir
        @SuppressLint("SimpleDateFormat")
        if (Date().after(SimpleDateFormat("yyyy-MM-dd").parse(this.getString(R.string.valid_until)))) {
            Log.d(TAG,getString(R.string.betaAlt))
            Toast.makeText(this, getString(R.string.betaAlt), Toast.LENGTH_SHORT).show()
            TimeUnit.SECONDS.sleep(1)
            System.exit(99)
        }
        copyAssets()
    }

    fun Fragment.show() { mViewPager.currentItem = seitenManager.nr(this) }

    override fun onCreateOptionsMenu(menu: Menu):Boolean {menuInflater.inflate(R.menu.menu_main, menu)
//        val seiten = arrayOf(frLieder().titel(), frQuint().titel(), frSetting().titel(), frData().titel())
        //ToDO add Menues manuell
        // menu.add()
        return true}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) { R.id.men_setting-> frSetting().show()
                             R.id.men_quint  -> frQuint()  .show()
                             R.id.men_auswahl-> frLieder() .show() }
        return super.onOptionsItemSelected(item)
    }

    private fun copyAssets() {
        val assFiles = assets.list("").filter { it.endsWith(".capx",true) || it.endsWith(".zip",true)  || it.endsWith(".html",true) }
        val locFiles = filesDir.list()

        fSetting.updateLog("$TAG ${assFiles.size} Dateien prüfen: $assFiles")
        // ToDo testen, ob assets überhaupt richtig ist.
        if (!locFiles.any { File(assFiles.toString(), it).exists() }) { // Überzählige löschen!
            Log.d(TAG,"Aufräumen?!?")
            locFiles.all { deleteFile(it) }
        }

        if (!assFiles.any { File(filesDir, it).exists() } ) {
            fSetting.updateLog("$TAG ${assFiles.size} Dateien aktualisiert.")
            for (filename in assFiles) {
                val inF = assets.open(filename)
                val outF = FileOutputStream(File(filesDir, filename))
                inF.copyTo(outF)  // Eigentlich wären nur neue Pakete nötig?!?
                inF.close()
                outF.close()
            }
        } else fSetting.updateLog("$TAG Daten sind aktuell.")
    }

}
