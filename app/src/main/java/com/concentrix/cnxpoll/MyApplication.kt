package com.concentrix.cnxpoll

import android.app.Application
import android.content.Context

/*
* Created By kusuma on 17/05/19
*/

class MyApplication: Application(){
    init {
        instance = this
    }

    companion object {
        private var instance: MyApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}