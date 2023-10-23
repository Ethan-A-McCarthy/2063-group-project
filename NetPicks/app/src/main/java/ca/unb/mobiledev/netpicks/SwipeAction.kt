package ca.unb.mobiledev.netpicks

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import kotlin.math.abs


private const val DEBUG_TAG = "Gestures"

class SwipeAction :
    Activity(),
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {
    private lateinit var mDetector: GestureDetectorCompat

    private var leftToRightCount = 0
    private var rightToLeftCount = 0

    private var leftToRight: TextView? = null
    private var rightToLeft: TextView? = null

    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100


    @SuppressLint("MissingInflatedId")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.swipe_action)
        mDetector = GestureDetectorCompat(this, this)
        mDetector.setOnDoubleTapListener(this)

        leftToRight = findViewById(R.id.left_Swipe)
        rightToLeft = findViewById(R.id.right_Swipe)

        if (savedInstanceState != null){
            leftToRightCount = savedInstanceState.getInt(LEFT_TO_RIGHT_VALUE)
        }

        updateCountsDisplay()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(LEFT_TO_RIGHT_VALUE, leftToRightCount)
        savedInstanceState.putInt(RIGHT_TO_LEFT_VALUE, rightToLeftCount)

        // Must always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun updateCountsDisplay() {
        leftToRight!!.text = getString(R.string.leftToRightMessage, leftToRightCount)
        rightToLeft!!.text = getString(R.string.RightToLeftMessage, rightToLeftCount)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    companion object {
        // String for LogCat documentation
        private const val TAG = "Lab 2 - Activity One"

        // Strings will serve as keys when saving state between activities
        private const val LEFT_TO_RIGHT_VALUE = "left to right"
        private const val RIGHT_TO_LEFT_VALUE = "right to left"
    }

    override fun onDown(event: MotionEvent): Boolean {
            Log.d(DEBUG_TAG, "onDown: $event")
            return true
    }

    override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            Log.d(DEBUG_TAG, "onFling: $event1 $event2")
            try {
                val diffX = event2.x - event1.x
                val diffY = event2.y - event1.y

                if (abs(diffX) > abs(diffY)){
                    if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                        if (diffX > 0) {
                           leftToRightCount++
                           updateCountsDisplay()
                        }
                        else {
                            rightToLeftCount++
                            updateCountsDisplay()
                        }
                    }
                }
            }
            catch (exception: Exception) {
                exception.printStackTrace()
            }
        return true
        }

    override fun onLongPress(event: MotionEvent) {
        Log.d(DEBUG_TAG, "onLongPress: $event")
    }

    override fun onScroll(
        event1: MotionEvent,
        event2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.d(DEBUG_TAG, "onScroll: $event1 $event2")
        return true
    }

    override fun onShowPress(event: MotionEvent) {
        Log.d(DEBUG_TAG, "onShowPress: $event")
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onSingleTapUp: $event")
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onDoubleTap: $event")
        return true
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: $event")
        return true
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: $event")
        return true
    }

}