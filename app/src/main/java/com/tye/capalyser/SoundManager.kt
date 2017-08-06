package com.tye.capalyser

/**
* Created by TyE on 2017-06-05.
*/

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import com.tye.capalyser.R.raw

class SoundManager {
    internal var soundPool: SoundPool

    internal var sounds = IntArray(130) // eigentlich nur von 36 bis 74

    init {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            soundPool = SoundPool.Builder().setMaxStreams(8).build()
        } else {
            @Suppress("DEPRECATION")
            soundPool = SoundPool(AudioManager.STREAM_MUSIC, 1, 5)
        }
    }

    fun loadSound(context: Context) {
        // val fields = raw::class.java.declaredFields
        //        try {
        //            for( int i=0; i< fields.length; i++ ) {
        //                Field f = fields[i] ;
        //
        //                int lfd = Integer.parseInt(f.getName().substring(2,3));
        //                sounds[lfd]= soundPool.load(view, , 1);
        //            }
        //        } catch (NoSuchFieldException e) {
        //            e.printStackTrace();
        //        }
        // Brutalo-Methode:
        sounds[36] = soundPool.load(context, raw.m36, 1)
        sounds[37] = soundPool.load(context, raw.m37, 1)
        sounds[38] = soundPool.load(context, raw.m38, 1)
        sounds[39] = soundPool.load(context, raw.m39, 1)
        sounds[40] = soundPool.load(context, raw.m40, 1)
        sounds[41] = soundPool.load(context, raw.m41, 1)
        sounds[42] = soundPool.load(context, raw.m42, 1)
        sounds[43] = soundPool.load(context, raw.m43, 1)
        sounds[44] = soundPool.load(context, raw.m44, 1)
        sounds[45] = soundPool.load(context, raw.m45, 1)
        sounds[46] = soundPool.load(context, raw.m46, 1)
        sounds[47] = soundPool.load(context, raw.m47, 1)
        sounds[48] = soundPool.load(context, raw.m48, 1)
        sounds[49] = soundPool.load(context, raw.m49, 1)
        sounds[50] = soundPool.load(context, raw.m50, 1)
        sounds[51] = soundPool.load(context, raw.m51, 1)
        sounds[52] = soundPool.load(context, raw.m52, 1)
        sounds[53] = soundPool.load(context, raw.m53, 1)
        sounds[54] = soundPool.load(context, raw.m54, 1)
        sounds[55] = soundPool.load(context, raw.m55, 1)
        sounds[56] = soundPool.load(context, raw.m56, 1)
        sounds[57] = soundPool.load(context, raw.m57, 1)
        sounds[58] = soundPool.load(context, raw.m58, 1)
        sounds[59] = soundPool.load(context, raw.m59, 1)
        sounds[60] = soundPool.load(context, raw.m60, 1)
        sounds[61] = soundPool.load(context, raw.m61, 1)
        sounds[62] = soundPool.load(context, raw.m62, 1)
        sounds[63] = soundPool.load(context, raw.m63, 1)
        sounds[64] = soundPool.load(context, raw.m64, 1)
        sounds[65] = soundPool.load(context, raw.m65, 1)
        sounds[66] = soundPool.load(context, raw.m66, 1)
        sounds[67] = soundPool.load(context, raw.m67, 1)
        sounds[68] = soundPool.load(context, raw.m68, 1)
        sounds[69] = soundPool.load(context, raw.m69, 1)
        sounds[70] = soundPool.load(context, raw.m70, 1)
        sounds[71] = soundPool.load(context, raw.m71, 1)
        sounds[72] = soundPool.load(context, raw.m72, 1)
        sounds[73] = soundPool.load(context, raw.m73, 1)
        sounds[74] = soundPool.load(context, raw.m74, 1)
    }

    fun playClickSound(sound: Int) = soundPool.play(sounds[sound], 1.0f, 1.0f, 0, 0, 1.0f)

    companion object {

        private var singleton: SoundManager? = null

        fun initialize(context: Context) {
            val soundManager = instance
            soundManager.loadSound(context)
        }

        val instance: SoundManager
            @Synchronized get() {
                if (singleton == null) {
                    singleton = SoundManager()
                }
                return singleton as SoundManager
            }
    }

}