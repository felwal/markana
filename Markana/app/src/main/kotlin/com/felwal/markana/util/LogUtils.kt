package com.felwal.markana.util

import android.util.Log

const val LOG_TAG = "Markana"

fun v(msg: String) = Log.v(LOG_TAG, msg)

fun d(msg: String) = Log.d(LOG_TAG, msg)

fun i(msg: String) = Log.i(LOG_TAG, msg)

fun w(msg: String) = Log.w(LOG_TAG, msg)

fun e(msg: String) = Log.e(LOG_TAG, msg)