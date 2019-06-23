package com.timerly.timerlyplugin.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.Nullable
import com.timerly.timerlyplugin.R
import com.timerly.timerlyplugin.Utils
import com.timerly.timerlyplugin.managers.StopwatchManager
import com.timerly.timerlyplugin.managers.TimerManager
import com.timerly.timerlyplugin.models.Constants.COMMAND_FROM_SERVICE_BUTTON_1_CLICKED
import com.timerly.timerlyplugin.models.Constants.COMMAND_FROM_SERVICE_BUTTON_2_CLICKED
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_ADD
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_REMOVE
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_RESET
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_START
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_STOP
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_UPDATE_TITLE
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_UPDATE_VALUE
import com.timerly.timerlyplugin.models.Stopwatch
import com.timerly.timerlyplugin.models.Timer
import com.timerly.timerlyplugin.models.TimerlyTimerEvent
import io.flutter.plugin.common.EventChannel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class FloatingWidgetService : Service(), View.OnClickListener, EventChannel.StreamHandler {

    private var mWindowManager: WindowManager? = null
    private var mFloatingWidgetViewMap: MutableMap<Int, View>? = mutableMapOf()
    private var eventSink: EventChannel.EventSink? = null
    private var remove_image_view: ImageView? = null
    private val szWindow = Point()
    private var removeFloatingWidgetView: View? = null

    private var x_init_cord: Int = 0
    private var y_init_cord: Int = 0
    private var x_init_margin: Int = 0
    private var y_init_margin: Int = 0
    var LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    else
        WindowManager.LayoutParams.TYPE_PHONE


    //Variable to check if the Floating widget view is on left side or in right side
    // initially we are displaying Floating widget view to Left side so set it to true
    private var isLeft = true
    private val mBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        internal val service: FloatingWidgetService
            get() = this@FloatingWidgetService
    }

    /*  Detect if the floating view is collapsed or expanded */
    private fun isViewCollapsed(id: Int): Boolean =
            mFloatingWidgetViewMap!!.get(id) == null || mFloatingWidgetViewMap!!.get(id)!!.findViewById<RelativeLayout>(R.id.collapse_view).getVisibility() == View.VISIBLE


    /*  return status bar height on basis of device display metrics  */
    private val statusBarHeight: Int
        get() = Math.ceil((25 * baseContext.resources.displayMetrics.density).toDouble()).toInt()

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        //init WindowManager
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        getWindowManagerDefaultDisplay()
        if (!EventBus.getDefault().isRegistered(this)) {
            Log.d("FloatingWidgetService", "Event Bus registered")
            EventBus.getDefault().register(this)
        }

        Log.d("FloatingWidgetService", "Service started")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotificationEvent(timerlyTimerEvent: TimerlyTimerEvent) {
        Log.d("FloatingWidgetService", "Command Received : " + timerlyTimerEvent.command)
        when (timerlyTimerEvent.command) {
            COMMAND_TO_SERVICE_START -> {
                when (timerlyTimerEvent.widgetType) {
                    TimerManager.widgetType -> {
                        startTimer(timerlyTimerEvent.id)
                    }
                    StopwatchManager.widgetType -> {
                        startStopwatch(timerlyTimerEvent.id)
                    }
                }
            }
            COMMAND_TO_SERVICE_UPDATE_VALUE -> {
                when (timerlyTimerEvent.widgetType) {
                    TimerManager.widgetType -> {
                        updateTimerValue(Utils.gson.fromJson(timerlyTimerEvent.data, Timer::class.java))
                    }
                    StopwatchManager.widgetType -> {
                        updateStopwatchValue(Utils.gson.fromJson(timerlyTimerEvent.data, Stopwatch::class.java))
                    }
                }

            }
            COMMAND_TO_SERVICE_UPDATE_TITLE -> {
                when (timerlyTimerEvent.widgetType) {
                    TimerManager.widgetType -> {
                        updateTimerName(timerlyTimerEvent.id, timerlyTimerEvent.data)
                    }
                    StopwatchManager.widgetType -> {
                        updateStopwatchName(timerlyTimerEvent.id, timerlyTimerEvent.data)
                    }
                }
            }
            COMMAND_TO_SERVICE_STOP -> {
                when (timerlyTimerEvent.widgetType) {
                    TimerManager.widgetType -> {
                        stopTimer(timerlyTimerEvent.id)
                    }
                    StopwatchManager.widgetType -> {
                        stopStopwatch(timerlyTimerEvent.id)
                    }
                }
            }
            COMMAND_TO_SERVICE_RESET -> {
                when (timerlyTimerEvent.widgetType) {
                    TimerManager.widgetType -> {
                        resetTimer(Utils.gson.fromJson(timerlyTimerEvent.data, Timer::class.java))
                    }
                    StopwatchManager.widgetType -> {
                        resetStopwatch(Utils.gson.fromJson(timerlyTimerEvent.data, Stopwatch::class.java))
                    }
                }
            }
            COMMAND_TO_SERVICE_REMOVE -> {
                when (timerlyTimerEvent.widgetType) {
                    TimerManager.widgetType -> {
                        removeTimer(timerlyTimerEvent.id)
                    }
                    StopwatchManager.widgetType -> {
                        removeStopwatch(timerlyTimerEvent.id)
                    }
                }
            }
            COMMAND_TO_SERVICE_ADD -> {
                when (timerlyTimerEvent.widgetType) {
                    TimerManager.widgetType -> {
                        addTimer(Utils.gson.fromJson(timerlyTimerEvent.data, Timer::class.java))
                    }
                    StopwatchManager.widgetType -> {
                        addStopwatch(Utils.gson.fromJson(timerlyTimerEvent.data, Stopwatch::class.java))
                    }
                }
            }
        }
    }


    private fun addTimer(timer: Timer) {
//        addRemoveView()
        val timerView = addFloatingWidgetView(timer.id, R.layout.floating_widget_timer_layout)
        implementClickListeners(timer.id)
        implementTouchListenerToFloatingWidgetView(timer.id)
        updateTimerValue(timer)
        updateTimerName(timer.id, timer.name)

        val button_1 = timerView!!.findViewById<TextView>(R.id.button_1)
        val button_2 = timerView!!.findViewById<TextView>(R.id.button_2)
        button_1.setOnClickListener {
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timer.id, TimerManager.widgetType, COMMAND_FROM_SERVICE_BUTTON_1_CLICKED, Utils.gson.toJson(timer))))
        }
        button_2.setOnClickListener {
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timer.id, TimerManager.widgetType, COMMAND_FROM_SERVICE_BUTTON_2_CLICKED, Utils.gson.toJson(timer))))
        }
    }

    private fun addStopwatch(stopwatch: Stopwatch) {
        addFloatingWidgetView(stopwatch.id, R.layout.floating_widget_stopwatch_layout)
        implementClickListeners(stopwatch.id)
        implementTouchListenerToFloatingWidgetView(stopwatch.id)
        updateStopwatchValue(stopwatch)
    }

    private fun startTimer(id: Int) {
        if (mFloatingWidgetViewMap!!.containsKey(id)) {
            val view = mFloatingWidgetViewMap!!.get(id)
            view!!.findViewById<TextView>(R.id.button_1).text = "Stop"
            view!!.findViewById<TextView>(R.id.button_2).text = "Reset"
        }
    }

    private fun startStopwatch(id: Int) {

    }

    private fun removeTimer(id: Int) {
        if (mFloatingWidgetViewMap!!.containsKey(id)) {
            val view = mFloatingWidgetViewMap!!.get(id)
            mWindowManager!!.removeView(view)
            mFloatingWidgetViewMap!!.remove(id)
        }
    }

    private fun removeStopwatch(id: Int) {
        if (mFloatingWidgetViewMap!!.containsKey(id)) {
            val view = mFloatingWidgetViewMap!!.get(id)
            mWindowManager!!.removeView(view)
            mFloatingWidgetViewMap!!.remove(id)
        }
    }

    private fun resetTimer(timer: Timer) {
        if (mFloatingWidgetViewMap!!.containsKey(timer.id)) {
            val view = mFloatingWidgetViewMap!!.get(timer.id)
            view!!.findViewById<TextView>(R.id.timer_value).text = Utils.formatSeconds(timer.initialTime)
            view!!.findViewById<TextView>(R.id.button_1).text = "Start"
        }
    }

    private fun resetStopwatch(stopwatch: Stopwatch) {

    }

    private fun stopTimer(id: Int) {
        if (mFloatingWidgetViewMap!!.containsKey(id)) {
            val view = mFloatingWidgetViewMap!!.get(id)
            view!!.findViewById<TextView>(R.id.button_1).text = "Start"
        }
    }

    private fun stopStopwatch(id: Int) {

    }

    private fun updateTimerValue(timer: Timer) {
        if (mFloatingWidgetViewMap!!.containsKey(timer.id)) {
            val view = mFloatingWidgetViewMap!!.get(timer.id)
            view!!.findViewById<TextView>(R.id.timer_value).text = Utils.formatSeconds(timer.currentTime)
            view!!.findViewById<TextView>(R.id.button_1).text = if (timer.isPlaying) "Stop" else "Start"
        }
    }

    private fun updateStopwatchValue(stopwatch: Stopwatch) {
//        val view = collapsedViewMap!!.get(stopwatch.id)
//        view!!.findViewById<TextView>(R.id.heading).setText(stopwatch.name)
//        view!!.findViewById<TextView>(R.id.timer_value).setText(formatSeconds(stopwatch.currentTime))
    }

    private fun updateTimerName(id: Int, name: String?) {
        if (mFloatingWidgetViewMap!!.containsKey(id) && name != null) {
            val view = mFloatingWidgetViewMap!!.get(id)
            view!!.findViewById<TextView>(R.id.heading).text = name
        }
    }

    private fun updateStopwatchName(id: Int, name: String?) {}


    /*  Add Remove View to Window Manager  */
    private fun addRemoveView() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //Inflate the removing view layout we created
        removeFloatingWidgetView = inflater.inflate(R.layout.remove_floating_widget_layout, null)

        //Add the view to the window.
        val paramRemove = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT)

        //Specify the view position
        paramRemove.gravity = Gravity.TOP or Gravity.LEFT

        //Initially the Removing widget view is not visible, so set visibility to GONE
        removeFloatingWidgetView!!.visibility = View.GONE
        remove_image_view = removeFloatingWidgetView!!.findViewById(R.id.remove_img) as ImageView

        //Add the view to the window
        mWindowManager!!.addView(removeFloatingWidgetView, paramRemove)
//        return remove_image_view
    }

    /*  Add Floating Widget View to Window Manager  */
    private fun addFloatingWidgetView(id: Int, layout: Int): View {

        Log.d(FloatingWidgetService::class.java.name, "addFloatingWidgetView called")
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        //Inflate the floating view layout we created
        val view = inflater.inflate(layout, null)

        //Add the view to the window.
        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

        //Specify the view position
        params.gravity = Gravity.TOP or Gravity.LEFT

        //Initially view will be added to top-left corner, you change x-y coordinates according to your need
        params.x = 5
        params.y = 200

        //Add the view to the window
        mWindowManager!!.addView(view, params)


        mFloatingWidgetViewMap!!.put(id, view)
        return view
    }

    private fun getWindowManagerDefaultDisplay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
            mWindowManager!!.defaultDisplay.getSize(szWindow)
        else {
            val w = mWindowManager!!.defaultDisplay.width
            val h = mWindowManager!!.defaultDisplay.height
            szWindow.set(w, h)
        }
    }

    /*  Implement Touch Listener to Floating Widget Root View  */
    @SuppressLint("ClickableViewAccessibility")
    private fun implementTouchListenerToFloatingWidgetView(id: Int) {
        //Drag and move floating view using user's touch action.
        mFloatingWidgetViewMap!!.get(id)!!.findViewById<RelativeLayout>(R.id.root_container).setOnTouchListener(object : View.OnTouchListener {

            internal var time_start: Long = 0
            internal var time_end: Long = 0

            internal var isLongClick = false//variable to judge if user click long press
            internal var inBounded = false//variable to judge if floating view is bounded to remove view
            internal var remove_img_width = 0
            internal var remove_img_height = 0

            internal var handler_longClick = Handler()
            internal var runnable_longClick: Runnable = Runnable {
                //On Floating Widget Long Click

                //Set isLongClick as true
                isLongClick = true

                //Set remove widget view visibility to VISIBLE
//                removeFloatingWidgetView!!.visibility = View.VISIBLE

                onFloatingWidgetLongClick(id)
            }

            override fun onTouch(v: View, event: MotionEvent): Boolean {

                //Get Floating widget view params
                val layoutParams = mFloatingWidgetViewMap!!.get(id)!!.layoutParams as WindowManager.LayoutParams

                //get the touch location coordinates
                val x_cord = event.rawX.toInt()
                val y_cord = event.rawY.toInt()

                val x_cord_Destination: Int
                var y_cord_Destination: Int

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        Log.d(FloatingWidgetService::class.java.name, "Touch Action Down")
                        time_start = System.currentTimeMillis()

                        handler_longClick.postDelayed(runnable_longClick, 600)

//                        remove_img_width = remove_image_view!!.layoutParams.width
//                        remove_img_height = remove_image_view!!.layoutParams.height

                        x_init_cord = x_cord
                        y_init_cord = y_cord

                        //remember the initial position.
                        x_init_margin = layoutParams.x
                        y_init_margin = layoutParams.y

                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        Log.d(FloatingWidgetService::class.java.name, "Touch Action Up")
                        isLongClick = false
//                        removeFloatingWidgetView!!.visibility = View.GONE
//                        remove_image_view!!.layoutParams.height = remove_img_height
//                        remove_image_view!!.layoutParams.width = remove_img_width
                        handler_longClick.removeCallbacks(runnable_longClick)

                        //If user drag and drop the floating widget view into remove view then stop the service
                        if (inBounded) {
                            stopSelf()
                            inBounded = false
                        }


                        //Get the difference between initial coordinate and current coordinate
                        val x_diff = x_cord - x_init_cord
                        val y_diff = y_cord - y_init_cord

                        //The check for x_diff <5 && y_diff< 5 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                            time_end = System.currentTimeMillis()

                            //Also check the difference between start time and end time should be less than 300ms
//                            if (time_end - time_start < 300)
//                                onFloatingWidgetClick(id)

                        }

                        y_cord_Destination = y_init_margin + y_diff

                        val barHeight = statusBarHeight
                        if (y_cord_Destination < 0) {
                            y_cord_Destination = 0
                        } else if (y_cord_Destination + (mFloatingWidgetViewMap!!.get(id)!!.height + barHeight) > szWindow.y) {
                            y_cord_Destination = szWindow.y - (mFloatingWidgetViewMap!!.get(id)!!.height + barHeight)
                        }

                        layoutParams.y = y_cord_Destination

                        inBounded = false

                        //reset position if user drags the floating view
                        resetPosition(x_cord)

                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        Log.d(FloatingWidgetService::class.java.name, "Touch Action Move")
                        val x_diff_move = x_cord - x_init_cord
                        val y_diff_move = y_cord - y_init_cord

                        x_cord_Destination = x_init_margin + x_diff_move
                        y_cord_Destination = y_init_margin + y_diff_move

                        //If user long click the floating view, update remove view
                        /* if (isLongClick) {
                             val x_bound_left = szWindow.x / 2 - (remove_img_width * 1.5).toInt()
                             val x_bound_right = szWindow.x / 2 + (remove_img_width * 1.5).toInt()
                             val y_bound_top = szWindow.y - (remove_img_height * 1.5).toInt()

                             //If Floating view comes under Remove View update Window Manager
                             if (x_cord >= x_bound_left && x_cord <= x_bound_right && y_cord >= y_bound_top) {
                                 inBounded = true

                                 val x_cord_remove = ((szWindow.x - remove_img_height * 1.5) / 2).toInt()
                                 val y_cord_remove = (szWindow.y - (remove_img_width * 1.5 + statusBarHeight)).toInt()

                                 if (remove_image_view!!.layoutParams.height == remove_img_height) {
                                     remove_image_view!!.layoutParams.height = (remove_img_height * 1.5).toInt()
                                     remove_image_view!!.layoutParams.width = (remove_img_width * 1.5).toInt()

                                     val param_remove = removeFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams
                                     param_remove.x = x_cord_remove
                                     param_remove.y = y_cord_remove

                                     mWindowManager!!.updateViewLayout(removeFloatingWidgetView, param_remove)
                                 }

                                 layoutParams.x = x_cord_remove + Math.abs(removeFloatingWidgetView!!.width - mFloatingWidgetViewMap!!.get(id)!!.width) / 2
                                 layoutParams.y = y_cord_remove + Math.abs(removeFloatingWidgetView!!.height - mFloatingWidgetViewMap!!.get(id)!!.height) / 2

                                 //Update the layout with new X & Y coordinate
                                 mWindowManager!!.updateViewLayout(mFloatingWidgetViewMap!!.get(id)!!, layoutParams)
                             } else {
                                 //If Floating window gets out of the Remove view update Remove view again
                                 inBounded = false
                                 remove_image_view!!.layoutParams.height = remove_img_height
                                 remove_image_view!!.layoutParams.width = remove_img_width
                                 //onFloatingWidgetClick(id)
                             }

                         }*/


                        layoutParams.x = x_cord_Destination
                        layoutParams.y = y_cord_Destination

                        //Update the layout with new X & Y coordinate
                        mWindowManager!!.updateViewLayout(mFloatingWidgetViewMap!!.get(id)!!, layoutParams)
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun implementClickListeners(id: Int) {
//        mFloatingWidgetViewMap!!.get(id)!!.findViewById<ImageView>(R.id.close_floating_view).setOnClickListener(this)
//        mFloatingWidgetViewMap!!.get(id)!!.findViewById<ImageView>(R.id.close_expanded_view).setOnClickListener(this)
//        mFloatingWidgetViewMap!!.get(id)!!.findViewById<ImageView>(R.id.open_activity_button).setOnClickListener(this)
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.close_floating_view ->
                //close the service and remove the from from the window
                stopSelf()

//            R.id.close_expanded_view -> {
////                collapsedView!!.visibility = View.VISIBLE
////                expandedView!!.visibility = View.GONE
//            }
//            R.id.open_activity_button -> {
//                //open the activity and stop service
//                /*val intent = Intent(this@FloatingWidgetService, MainActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)*/
//
//                //close the service and remove view from the view hierarchy
//                stopSelf()
//            }
        }
    }

    /*  on Floating Widget Long Click, increase the size of remove view as it look like taking focus */
    private fun onFloatingWidgetLongClick(id: Int) {
        //Get remove Floating view params
        val removeParams = removeFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams

        //get x and y coordinates of remove view
        val x_cord = (szWindow.x - removeFloatingWidgetView!!.width) / 2
        val y_cord = szWindow.y - (removeFloatingWidgetView!!.height + statusBarHeight)


        removeParams.x = x_cord
        removeParams.y = y_cord

        //Update Remove view params
        mWindowManager!!.updateViewLayout(removeFloatingWidgetView, removeParams)
    }

    /*  Reset position of Floating Widget view on dragging  */
    private fun resetPosition(x_cord_now: Int) {
//        if (x_cord_now <= szWindow.x / 2) {
//            isLeft = true
//            moveToLeft(x_cord_now)
//        } else {
//            isLeft = false
//            moveToRight(x_cord_now)
//        }

    }


    /*  Method to move the Floating widget view to Left  */
    /*private fun moveToLeft(current_x_cord: Int) {
        val x = szWindow.x - current_x_cord

        object : CountDownTimer(500, 5) {
            //get params of Floating Widget view
            internal var mParams = mFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams

            override fun onTick(t: Long) {
                val step = (500 - t) / 5

                mParams.x = 0 - (current_x_cord.toLong() * current_x_cord.toLong() * step).toInt()

                //If you want bounce effect uncomment below line and comment above line
                // mParams.x = 0 - (int) (double) bounceValue(step, x);


                //Update window manager for Floating Widget
                mWindowManager!!.updateViewLayout(mFloatingWidgetView, mParams)
            }

            override fun onFinish() {
                mParams.x = 0

                //Update window manager for Floating Widget
                mWindowManager!!.updateViewLayout(mFloatingWidgetView, mParams)
            }
        }.start()
    }*/

    /*  Method to move the Floating widget view to Right  */
    /* private fun moveToRight(current_x_cord: Int) {

         object : CountDownTimer(500, 5) {
             //get params of Floating Widget view
             internal var mParams = mFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams

             override fun onTick(t: Long) {
                 val step = (500 - t) / 5

                 mParams.x = (szWindow.x + current_x_cord.toLong() * current_x_cord.toLong() * step - mFloatingWidgetView!!.width).toInt()

                 //If you want bounce effect uncomment below line and comment above line
                 //  mParams.x = szWindow.x + (int) (double) bounceValue(step, x_cord_now) - mFloatingWidgetView.getWidth();

                 //Update window manager for Floating Widget
                 mWindowManager!!.updateViewLayout(mFloatingWidgetView, mParams)
             }

             override fun onFinish() {
                 mParams.x = szWindow.x - mFloatingWidgetView!!.width

                 //Update window manager for Floating Widget
                 mWindowManager!!.updateViewLayout(mFloatingWidgetView, mParams)
             }
         }.start()
     }*/

    /*  Get Bounce value if you want to make bounce effect to your Floating Widget */
    private fun bounceValue(step: Long, scale: Long): Double {
        return scale.toDouble() * Math.exp(-0.055 * step) * Math.cos(0.08 * step)
    }


    /*  Update Floating Widget view coordinates on Configuration change  */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        getWindowManagerDefaultDisplay()
        mFloatingWidgetViewMap!!.values.forEach {
            val layoutParams = it.layoutParams as WindowManager.LayoutParams

            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {


                if (layoutParams.y + (it.height + statusBarHeight) > szWindow.y) {
                    layoutParams.y = szWindow.y - (it.height + statusBarHeight)
                    mWindowManager!!.updateViewLayout(it, layoutParams)
                }

                if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                    resetPosition(szWindow.x)
                }

            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

                if (layoutParams.x > szWindow.x) {
                    resetPosition(szWindow.x)
                }

            }
        }

    }

    /*  on Floating widget click show expanded view  */
    private fun onFloatingWidgetClick(id: Int) {
        if (isViewCollapsed(id)) {
            //When user clicks on the image view of the collapsed layout,
            //visibility of the collapsed layout will be changed to "View.GONE"
            //and expanded view will become visible.
//            collapsedViewMap!!.get(id)!!.visibility = View.GONE
//            expandedViewMap!!.get(id)!!.visibility = View.VISIBLE

        }
    }


    override fun onListen(p0: Any?, p1: EventChannel.EventSink?) {
        Log.d("FloatingWidgetService", "EventBus onListen Called")
        eventSink = p1
    }

    override fun onCancel(p0: Any?) {
        eventSink = null
        Log.d("FloatingWidgetService", "EventBus onCancel Called")
    }

    override fun onDestroy() {
        super.onDestroy()

        /*  on destroy remove both view from window manager */
        mFloatingWidgetViewMap!!.values.forEach {
            mWindowManager!!.removeView(it)
        }


        if (removeFloatingWidgetView != null)
            mWindowManager!!.removeView(removeFloatingWidgetView)

        EventBus.getDefault().unregister(this)
        Log.d("FloatingWidgetService", "Service destroyed")
    }


}