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
import android.animation.ValueAnimator.*
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
            myAnimatorSet.alphaAnimation(0.0f, 1.0f)
        }
        myAnimatorSet.setAnimationChangedListener(object : MyAnimatorSet.AnimationChangedListener() {
            override fun onAnimationEnd(id: Int) {
                Log.d(MainActivity::class.java.simpleName, "onAnimationEnd() id -> $id")
                myAnimatorSet.changeTarget(nextImage(id))
                myAnimatorSet.alphaAnimation(0.0f, 1.0f, 100L)
            }

            override fun onAnimationRepeat(id: Int) {
                Log.d(MainActivity::class.java.simpleName, "onAnimationRepeat() id -> $id")
            }

            override fun onAnimationStart() {
            }
        })
    }

    private fun nextImage(id: Int): View = when (id) {
        R.id.image1 -> image2
        R.id.image2 -> image1
        else -> image1
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

    class MyAnimatorSet(private val activity: Activity, private var targetView: View) {

        private var animatorSet: AnimatorSet? = AnimatorSet()
        private var animationChangedListener: AnimationChangedListener? = null

        abstract class AnimationChangedListener {
            open fun onAnimationRepeat(id: Int) {}
            open fun onAnimationEnd(id: Int) {}
            open fun onAnimationStart() {}
        }

        enum class TransformPivotType {
            LEFT_TOP,
            RIGHT_TOP,
            CENTER,
            LEFT_BOTTOM,
            RIGHT_BOTTOM
        }

        enum class RepeatType(val value: Int) {
            REVERSE_MODE(REVERSE),
            RESTART_MODE(RESTART)
        }

        private fun start(delay: Long, _duration: Long) {
            animatorSet?.run {
                startDelay = delay
                duration = _duration
                activity.runOnUiThread {
                    start()
                }   
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
            // before change target remove previous instance to animate object
            animatorSet?.removeAllListeners()
            animatorSet = null
            animatorSet = AnimatorSet()
            animatorSet?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                    animationChangedListener?.onAnimationRepeat(targetView.id)
                }

                override fun onAnimationEnd(animation: Animator?) {
                    animationChangedListener?.onAnimationEnd(targetView.id)
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    animationChangedListener?.onAnimationStart()
                }
            })
            targetView = _view
        }

        fun setAnimationChangedListener(_animationChangedListener: AnimationChangedListener?) {
            animationChangedListener = _animationChangedListener
            if (animationChangedListener != null) {
                animatorSet?.removeAllListeners()
            }
            animatorSet?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                    animationChangedListener?.onAnimationRepeat(targetView.id)
                }

                override fun onAnimationEnd(animation: Animator?) {
                    animationChangedListener?.onAnimationEnd(targetView.id)
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    animationChangedListener?.onAnimationStart()
                }
            })
        }

        fun cancelAnimation() {
            animatorSet?.cancel()
        }

        fun alphaAnimation(
            minAlpha: Float = targetView.alpha,
            maxAlpha: Float = 1.0f,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            val alpha = ObjectAnimator.ofFloat(targetView, "alpha", minAlpha, maxAlpha)
            animatorSet?.play(alpha)
            start(delay, duration)
        }

        fun alphaAnimationInfinite(
            minAlpha: Float = targetView.alpha,
            maxAlpha: Float = 1.0f,
            mode: RepeatType = RepeatType.REVERSE_MODE,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            val alpha = ObjectAnimator.ofFloat(targetView, "alpha", minAlpha, maxAlpha)
            alpha.repeatCount = INFINITE
            alpha.repeatMode = mode.value
            Log.d(MainActivity::class.java.simpleName, "alphaAnimationInfinite() repeatMode -> ${alpha.repeatMode}")
            animatorSet?.play(alpha)
            start(delay, duration)
        }

        fun scaleAnimation(
            minScale: PointF = PointF(targetView.scaleX, targetView.scaleY),
            maxScale: PointF = PointF(1.0f, 1.0f),
            transformPivotType: TransformPivotType = TransformPivotType.CENTER,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            val scaleDownY = ObjectAnimator.ofFloat(targetView, "scaleY", minScale.y, maxScale.y)
            val scaleDownX = ObjectAnimator.ofFloat(targetView, "scaleX", minScale.x, maxScale.x)
            val transformPivot = getTranslationPivot(
                transformPivotType,
                PointF(targetView.measuredWidth.toFloat(), targetView.measuredHeight.toFloat())
            )
            val pivotY = ObjectAnimator.ofFloat(targetView, "pivotY", transformPivot.y)
            val pivotX = ObjectAnimator.ofFloat(targetView, "pivotX", transformPivot.x)
            animatorSet?.playTogether(scaleDownX, scaleDownY, pivotX, pivotY)
            start(delay, duration)
        }

        fun scaleAnimationInfinite(
            minScale: PointF = PointF(targetView.scaleX, targetView.scaleY),
            maxScale: PointF = PointF(1.0f, 1.0f),
            transformPivotType: TransformPivotType = TransformPivotType.CENTER,
            mode: RepeatType = RepeatType.REVERSE_MODE,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            val scaleDownY = ObjectAnimator.ofFloat(targetView, "scaleY", minScale.y, maxScale.y)
            val scaleDownX = ObjectAnimator.ofFloat(targetView, "scaleX", minScale.x, maxScale.x)
            val transformPivot = getTranslationPivot(
                transformPivotType,
                PointF(targetView.measuredWidth.toFloat(), targetView.measuredHeight.toFloat())
            )
            val pivotY = ObjectAnimator.ofFloat(targetView, "pivotY", transformPivot.y)
            val pivotX = ObjectAnimator.ofFloat(targetView, "pivotX", transformPivot.x)
            with(scaleDownX) {
                repeatCount = INFINITE
                repeatMode = mode.value
            }
            with(scaleDownY) {
                repeatCount = INFINITE
                repeatMode = mode.value
            }
            with(pivotX) {
                repeatCount = INFINITE
                repeatMode = mode.value
            }
            with(pivotY) {
                repeatCount = INFINITE
                repeatMode = mode.value
            }
            animatorSet?.playTogether(scaleDownX, scaleDownY, pivotX, pivotY)
            start(delay, duration)
        }

        fun rotateAnimation(
            src: PointF = PointF(targetView.rotationX, targetView.rotationY),
            dst: PointF,
            transformPivotType: TransformPivotType = TransformPivotType.CENTER,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            val rotationY = ObjectAnimator.ofFloat(targetView, "rotationY", src.y, dst.y)
            val rotationX = ObjectAnimator.ofFloat(targetView, "rotationX", src.x, dst.x)
            val transformPivot = getTranslationPivot(
                transformPivotType,
                PointF(targetView.measuredWidth.toFloat(), targetView.measuredHeight.toFloat())
            )
            val pivotY = ObjectAnimator.ofFloat(targetView, "pivotY", transformPivot.y)
            val pivotX = ObjectAnimator.ofFloat(targetView, "pivotX", transformPivot.x)
            animatorSet?.playTogether(rotationX, rotationY, pivotX, pivotY)
            start(delay, duration)
        }

        fun rotateAnimationInfinite(
            src: PointF = PointF(targetView.rotationX, targetView.rotationY),
            dst: PointF,
            transformPivotType: TransformPivotType = TransformPivotType.CENTER,
            mode: RepeatType = RepeatType.REVERSE_MODE,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            val rotationY = ObjectAnimator.ofFloat(targetView, "rotationY", src.y, dst.y)
            val rotationX = ObjectAnimator.ofFloat(targetView, "rotationX", src.x, dst.x)
            val transformPivot = getTranslationPivot(
                transformPivotType,
                PointF(targetView.measuredWidth.toFloat(), targetView.measuredHeight.toFloat())
            )
            val pivotY = ObjectAnimator.ofFloat(targetView, "pivotY", transformPivot.y)
            val pivotX = ObjectAnimator.ofFloat(targetView, "pivotX", transformPivot.x)
            with(rotationX) {
                repeatCount = INFINITE
                repeatMode = mode.value
            }
            with(rotationY) {
                repeatCount = INFINITE
                repeatMode = mode.value
            }
            with(pivotX) {
                repeatCount = INFINITE
                repeatMode = mode.value
            }
            with(pivotY) {
                repeatCount = INFINITE
                repeatMode = mode.value
            }
            animatorSet?.playTogether(rotationX, rotationY, pivotX, pivotY)
            start(delay, duration)
        }

        fun clockwiseRotationAnimation(
            src: Float = targetView.rotation,
            degree: Float, delay: Long = 0L, duration: Long = 500L
        ) {
            val rotation = ObjectAnimator.ofFloat(targetView, "rotation", src, degree)
            animatorSet?.play(rotation)
            start(delay, duration)
        }

        fun clockwiseRotationAnimationInfinite(
            src: Float = targetView.rotation,
            degree: Float, delay: Long = 0L, duration: Long = 500L,
            mode: RepeatType = RepeatType.REVERSE_MODE
        ) {
            val rotation = ObjectAnimator.ofFloat(targetView, "rotation", src, degree)
            rotation.repeatMode = mode.value
            animatorSet?.play(rotation)
            start(delay, duration)
        }

        fun translationAnimation(
            src: PointF,
            dst: PointF,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            val transY = ObjectAnimator.ofFloat(targetView, "translationY", src.y, dst.y)
            val transX = ObjectAnimator.ofFloat(targetView, "translationX", src.x, dst.x)
            animatorSet?.play(transX)?.with(transY)
            start(delay, duration)
        }

        fun translationAnimationInfinite(
            src: PointF,
            dst: PointF,
            mode: RepeatType = RepeatType.REVERSE_MODE,
            delay: Long = 0L,
            duration: Long = 500L
        ) {
            val transY = ObjectAnimator.ofFloat(targetView, "translationY", src.y, dst.y)
            val transX = ObjectAnimator.ofFloat(targetView, "translationX", src.x, dst.x)
            transY.repeatMode = mode.value
            transX.repeatMode = mode.value
            animatorSet?.play(transX)?.with(transY)
            start(delay, duration)
        }
    }
}
