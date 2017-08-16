package com.tye.capalyser

import android.content.Context
import android.content.SharedPreferences

class clPrefs(context: Context) {
    private val PREFS_FILENAME = "capPrefs"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0);

    //Programm Einstellungen:
    private val PRE_STARTSCREEN = "SCREEN"  ; var Screen: String     ; get() = prefs.getString   (PRE_STARTSCREEN, "")   ; set (value) = prefs.edit().putString   (PRE_STARTSCREEN , value).apply()
    private val PRE_STARTMODE   = "MODE"    ; var Modus: Boolean     ; get() = prefs.getBoolean  (PRE_STARTMODE  , true) ; set (value) = prefs.edit().putBoolean  (PRE_STARTMODE   , value).apply()
    private val PREF_DISCLAIM   = "DISCLAIM"; var Disc: Boolean      ; get() = prefs.getBoolean  (PREF_DISCLAIM  , true) ; set (value) = prefs.edit().putBoolean  (PREF_DISCLAIM   , value).apply()
    private val PREF_LIEDDAUER  = "LIEDDAUER";var LiedDauer: Int     ; get() = prefs.getInt      (PREF_LIEDDAUER , 2)    ; set (value) = prefs.edit().putInt      (PREF_LIEDDAUER  , value).apply()
    private val PREF_LIEDZAHL   = "LIEDZAHL"; var LiedZahl: Int      ; get() = prefs.getInt      (PREF_LIEDZAHL  , 6)    ; set (value) = prefs.edit().putInt      (PREF_LIEDZAHL   , value).apply()
    private val PREF_ARTDAUER   = "ARTDAUER"; var ArtDauer: Int      ; get() = prefs.getInt      (PREF_ARTDAUER  , 2)    ; set (value) = prefs.edit().putInt      (PREF_ARTDAUER   , value).apply()
    private val PREF_ANSICHT    = "ANSICHT" ; var Ansicht: Boolean   ; get() = prefs.getBoolean  (PREF_ANSICHT   , false); set (value) = prefs.edit().putBoolean  (PREF_ANSICHT    , value).apply()

    // gelesene Daten:
    private val PREF_ZIP        = "ZIP"     ; var Zip: String        ; get() = prefs.getString   (PREF_ZIP       , "")   ; set (value) = prefs.edit().putString   (PREF_ZIP        , value).apply()
    private val PREF_LIED       = "LIED"    ; var Lied: String       ; get() = prefs.getString   (PREF_LIED      , "")   ; set (value) = prefs.edit().putString   (PREF_LIED       , value).apply()

    private val MOD_VALID       = "VALID"   ; var isValid: Boolean   ; get() = prefs.getBoolean  (MOD_VALID      , false); set (value) = prefs.edit().putBoolean  (MOD_VALID       , value).apply()
    private val MOD_ANFANG      = "ANFANG"  ; var liedAnfang: String ; get() = prefs.getString   (MOD_ANFANG     , "")   ; set (value) = prefs.edit().putString   (MOD_ANFANG      , value).apply()
    private val MOD_TASYM       = "TASYM"   ; var tonartSym: String  ; get() = prefs.getString   (MOD_TASYM      , "")   ; set (value) = prefs.edit().putString   (MOD_TASYM       , value).apply()
    private val MOD_TITEL       = "TITEL"   ; var title: String      ; get() = prefs.getString   (MOD_TITEL      , "")   ; set (value) = prefs.edit().putString   (MOD_TITEL       , value).apply()
    private val MOD_AUTOR       = "AUTOR"   ; var autor: String      ; get() = prefs.getString   (MOD_AUTOR      , "")   ; set (value) = prefs.edit().putString   (MOD_AUTOR       , value).apply()
    private val MOD_TEXT        = "TEXT"    ; var liedtext: String   ; get() = prefs.getString   (MOD_TEXT       , "")   ; set (value) = prefs.edit().putString   (MOD_TEXT        , value).apply()
    private val MOD_ANNOT       = "ANNOT"   ; var kommentare: String ; get() = prefs.getString   (MOD_ANNOT      , "")   ; set (value) = prefs.edit().putString   (MOD_ANNOT       , value).apply()
    private val MOD_AKKORD1     = "AKKORD1" ; var akkord1: String    ; get() = prefs.getString   (MOD_AKKORD1    , "")   ; set (value) = prefs.edit().putString   (MOD_AKKORD1     , value).apply()
    private val MOD_AKKORD2     = "AKKORD2" ; var akkord2: String    ; get() = prefs.getString   (MOD_AKKORD2    , "")   ; set (value) = prefs.edit().putString   (MOD_AKKORD2     , value).apply()
    private val MOD_AKKORD3     = "AKKORD3" ; var akkord3: String    ; get() = prefs.getString   (MOD_AKKORD3    , "")   ; set (value) = prefs.edit().putString   (MOD_AKKORD3     , value).apply()
    private val MOD_AKKORD4     = "AKKORD4" ; var akkord4: String    ; get() = prefs.getString   (MOD_AKKORD4    , "")   ; set (value) = prefs.edit().putString   (MOD_AKKORD4     , value).apply()
    private val MOD_AKKORD5     = "AKKORD5" ; var akkord5: String    ; get() = prefs.getString   (MOD_AKKORD5    , "")   ; set (value) = prefs.edit().putString   (MOD_AKKORD5     , value).apply()
    private val MOD_AKKORD6     = "AKKORD6" ; var akkord6: String    ; get() = prefs.getString   (MOD_AKKORD6    , "")   ; set (value) = prefs.edit().putString   (MOD_AKKORD6     , value).apply()

}