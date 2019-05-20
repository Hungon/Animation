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
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.REVERSE
import android.app.Activity
import android.graphics.Point
import android.graphics.PointF
import android.os.Handler
import android.provider.Settings
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myAnimatorSet = MyAnimatorSet(this, image1)
        button_start?.setOnClickListener {
            myAnimatorSet.scaleAnimationInfinite(
                PointF(0.0f, 0.0f),
                PointF(1.0f, 1.0f),
                MyAnimatorSet.TransformPivotType.CENTER
            )
            Handler().postDelayed({
                myAnimatorSet.changeTarget(image2)
            }, 3000L)
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

    class MyAnimatorSet(private val activity: Activity, private var view: View) {

        private val animatorSet = AnimatorSet()
        private var animationChangedListener: AnimationChangedListener? = null

        abstract class AnimationChangedListener {
            open fun onAnimationEnd() {}
            open fun onAnimationStart() {}
        }

        enum class TransformPivotType {
            LEFT_TOP,
            RIGHT_TOP,
            CENTER,
            LEFT_BOTTOM,
            RIGHT_BOTTOM
        }

        private fun start(delay: Long, duration: Long) {
            animatorSet.startDelay = delay
            animatorSet.duration = duration
            activity.runOnUiThread {
                animatorSet.start()
            }
        }

        private fun getTranslationPivot(type: TransformPivotType, size: PointF): PointF {
            return when (type) {
                TransformPivotType.LEFT_TOP -> {
                    PointF()
                }
                TransformPivotType.RIGHT_TOP -> {
                    PointF(size.x, 0f)
                }
                TransformPivotType.CENTER -> {
                    PointF(size.x / 2, size.y / 2)
                }
                TransformPivotType.LEFT_BOTTOM -> {
                    PointF(0f, size.y)
                }
                TransformPivotType.RIGHT_BOTTOM -> {
                    PointF(size.x, size.y)
                }
            }
        }

        fun changeTarget(_view: View) {
            view = _view
            animatorSet.setTarget(view)
            activity.runOnUiThread {
                animatorSet.start()
            }
        }

        fun setAnimationChangedListener(_animationChangedListener: AnimationChangedListener?) {
            animationChangedListener = _animationChangedListener
            if (animationChangedListener != null) {
                animatorSet.removeAllListeners()
            }
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    animationChangedListener?.onAnimationEnd()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    animationChangedListener?.onAnimationStart()
                }
            })
        }

        fun cancelAnimation() {
            animatorSet.cancel()
        }

        fun alphaAnimation(
            minAlpha: Float = view.alpha,
            maxAlpha: Float = 1.0f,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            if (animatorSet.isRunning) animatorSet.cancel()
            val alpha = ObjectAnimator.ofFloat(view, "alpha", minAlpha, maxAlpha)
            animatorSet.play(alpha)
            start(delay, duration)
        }

        fun alphaAnimationInfite(
            minAlpha: Float = view.alpha,
            maxAlpha: Float = 1.0f,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            if (animatorSet.isRunning) animatorSet.cancel()
            val alpha = ObjectAnimator.ofFloat(view, "alpha", minAlpha, maxAlpha)
            alpha.repeatCount = INFINITE
            alpha.repeatMode = REVERSE
            animatorSet.play(alpha)
            start(delay, duration)
        }

        fun scaleAnimation(
            minScale: PointF = PointF(view.scaleX, view.scaleY),
            maxScale: PointF = PointF(1.0f, 1.0f),
            transformPivotType: TransformPivotType = TransformPivotType.CENTER,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            if (animatorSet.isRunning) animatorSet.cancel()
            val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", minScale.y, maxScale.y)
            val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", minScale.x, maxScale.x)
            val transformPivot = getTranslationPivot(
                transformPivotType,
                PointF(view.measuredWidth.toFloat(), view.measuredHeight.toFloat())
            )
            val pivotY = ObjectAnimator.ofFloat(view, "pivotY", transformPivot.y)
            val pivotX = ObjectAnimator.ofFloat(view, "pivotX", transformPivot.x)
            animatorSet.playTogether(scaleDownX, scaleDownY, pivotX, pivotY)
            start(delay, duration)
        }

        fun scaleAnimationInfinite(
            minScale: PointF = PointF(view.scaleX, view.scaleY),
            maxScale: PointF = PointF(1.0f, 1.0f),
            transformPivotType: TransformPivotType = TransformPivotType.CENTER,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            if (animatorSet.isRunning) animatorSet.cancel()
            val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", minScale.y, maxScale.y)
            val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", minScale.x, maxScale.x)
            val transformPivot = getTranslationPivot(
                transformPivotType,
                PointF(view.measuredWidth.toFloat(), view.measuredHeight.toFloat())
            )
            val pivotY = ObjectAnimator.ofFloat(view, "pivotY", transformPivot.y)
            val pivotX = ObjectAnimator.ofFloat(view, "pivotX", transformPivot.x)
            with(scaleDownX) {
                repeatCount = INFINITE
                repeatMode = REVERSE
            }
            with(scaleDownY) {
                repeatCount = INFINITE
                repeatMode = REVERSE
            }
            with(pivotX) {
                repeatCount = INFINITE
                repeatMode = REVERSE
            }
            with(pivotY) {
                repeatCount = INFINITE
                repeatMode = REVERSE
            }
            animatorSet.playTogether(scaleDownX, scaleDownY, pivotX, pivotY)
            start(delay, duration)
        }

        fun rotateAnimation(
            src: PointF = PointF(view.rotationX, view.rotationY),
            dst: PointF,
            transformPivotType: TransformPivotType = TransformPivotType.CENTER,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            if (animatorSet.isRunning) animatorSet.cancel()
            val rotationY = ObjectAnimator.ofFloat(view, "rotationY", src.y, dst.y)
            val rotationX = ObjectAnimator.ofFloat(view, "rotationX", src.x, dst.x)
            val transformPivot = getTranslationPivot(
                transformPivotType,
                PointF(view.measuredWidth.toFloat(), view.measuredHeight.toFloat())
            )
            val pivotY = ObjectAnimator.ofFloat(view, "pivotY", transformPivot.y)
            val pivotX = ObjectAnimator.ofFloat(view, "pivotX", transformPivot.x)
            animatorSet.playTogether(rotationX, rotationY, pivotX, pivotY)
            start(delay, duration)
        }

        fun rotateAnimationInfinite(
            src: PointF = PointF(view.rotationX, view.rotationY),
            dst: PointF,
            transformPivotType: TransformPivotType = TransformPivotType.CENTER,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            if (animatorSet.isRunning) animatorSet.cancel()
            val rotationY = ObjectAnimator.ofFloat(view, "rotationY", src.y, dst.y)
            val rotationX = ObjectAnimator.ofFloat(view, "rotationX", src.x, dst.x)
            val transformPivot = getTranslationPivot(
                transformPivotType,
                PointF(view.measuredWidth.toFloat(), view.measuredHeight.toFloat())
            )
            val pivotY = ObjectAnimator.ofFloat(view, "pivotY", transformPivot.y)
            val pivotX = ObjectAnimator.ofFloat(view, "pivotX", transformPivot.x)
            with(rotationX) {
                repeatCount = INFINITE
                repeatMode = REVERSE
            }
            with(rotationY) {
                repeatCount = INFINITE
                repeatMode = REVERSE
            }
            with(pivotX) {
                repeatCount = INFINITE
                repeatMode = REVERSE
            }
            with(pivotY) {
                repeatCount = INFINITE
                repeatMode = REVERSE
            }
            animatorSet.playTogether(rotationX, rotationY, pivotX, pivotY)
            start(delay, duration)
        }

        fun clockwiseRotationAnimation(
            src: Float = view.rotation,
            degree: Float, delay: Long = 0L, duration: Long = 500L
        ) {
            val rotation = ObjectAnimator.ofFloat(view, "rotation", src, degree)
            animatorSet.play(rotation)
            start(delay, duration)
        }

        fun translationAnimation(
            src: PointF,
            dst: PointF,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            val transY = ObjectAnimator.ofFloat(view, "translationY", src.y, dst.y)
            val transX = ObjectAnimator.ofFloat(view, "translationX", src.x, dst.x)
            animatorSet.play(transX).with(transY)
            start(delay, duration)
        }
    }
}
