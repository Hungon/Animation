package com.trials.sample

import android.app.Instrumentation
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import android.R.attr.process
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Point
import android.graphics.PointF
import android.provider.Settings
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myAnimatorSet = MyAnimatorSet(this)
        button_start?.setOnClickListener {
            myAnimatorSet.translationAnimation(text, PointF(100.0f, 100.0f))
            myAnimatorSet.scaleAnimation(image)
        }
    }

    fun ex(permission: String) {
        val process = Runtime.getRuntime()
        val command = String.format(Locale.ENGLISH, "pm grant %s %s", packageName, permission)
        val res = process.exec(command)
        val inputStreamReader = InputStreamReader(res.inputStream)
        val line = inputStreamReader.readLines()
        val inputStreamReader1 = InputStreamReader(res.errorStream)
        val line1 = inputStreamReader1.readLines()
        inputStreamReader.close()
        inputStreamReader1.close()
        Log.d(MainActivity::class.java.simpleName, "result: $line error: $line1")
    }

    class MyAnimatorSet(private val activity: Activity) {

        private fun start(animatorSet: AnimatorSet, delay: Long, duration: Long) {
            animatorSet.startDelay = delay
            animatorSet.duration = duration
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })
            activity.runOnUiThread {
                animatorSet.start()
            }
        }

        fun alphaAnimation(
            v: View,
            minAlpha: Float = v.alpha,
            maxAlpha: Float = 1.0f,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            val alpha = ObjectAnimator.ofFloat(v, "alpha", minAlpha, maxAlpha)
            val animatorSet = AnimatorSet()
            animatorSet.play(alpha)
            start(animatorSet, delay, duration)
        }

        fun scaleAnimation(
            v: View,
            minScale: PointF = PointF(v.scaleX, v.scaleY),
            maxScale: PointF = PointF(1.0f, 1.0f),
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            val scaleDownY = ObjectAnimator.ofFloat(v, "scaleY", minScale.y, maxScale.y)
            val scaleDownX = ObjectAnimator.ofFloat(v, "scaleX", minScale.x, maxScale.x)
            val animatorSet = AnimatorSet()
            animatorSet.play(scaleDownX).with(scaleDownY)
            start(animatorSet, delay, duration)
        }

        fun translationAnimation(v: View, dst: PointF, delay: Long = 0L, duration: Long = 500L) {
            val transY = ObjectAnimator.ofFloat(v, "translationY", v.translationY, dst.y)
            val transX = ObjectAnimator.ofFloat(v, "translationX", v.translationX, dst.x)
            val animatorSet = AnimatorSet()
            animatorSet.play(transX).with(transY)
            start(animatorSet, delay, duration)
        }
    }
}
