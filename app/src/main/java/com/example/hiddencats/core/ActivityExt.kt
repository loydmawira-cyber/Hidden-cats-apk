package com.example.hiddencats.core

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** Unwraps a Compose/Context chain to the hosting Activity (needed to show ads). */
fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
