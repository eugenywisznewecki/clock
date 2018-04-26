package ink.mnk.com.clock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit


/*Реализовать аналоговые часы. Должно быть:
- секундная стрелка
- минутная стрелка
- часовая стрелка
- круглый циферблат с пятиминутными отсечками
Все стрелки должны менять своё положение синхронно с системным временем.
View не должна занимать лишнего места, при выставлении
размеров wrap_content view должна занимать 3/4 части
минимального размера (например, в портрете 3/4 ширины, в ландшафте 3/4 высоты)
Предусмотреть возможность изменения внешнего вида view.
Как минимум нужно добавить возможность менять цвет и толщину
стрелок и цвет циферблата через аттрибуты и методы класса.*/

class ClockView(context: Context, attr: AttributeSet) : View(context, attr) {

	//simply to use RX :)
	private val timeObservable by lazy {
		Observable.interval(100L, TimeUnit.MILLISECONDS)
				.timeInterval()
				.observeOn(AndroidSchedulers.mainThread())
	}

	//TODO
	// Mutable  PUBLIC Properties, to set from code
	var radius = 300
	var hourColor = Color.BLACK
	var minuteColor = Color.BLACK
	var secondColor = Color.BLACK
	var hourThick = 15
	var minuteThick = 10
	var secondThick = 5
	var minuteLength = 295
	var secondLength = 310
	var hourLength = 150
	var clockColor = Color.BLACK
	var clockThick = 10
	//remove this
	var innerRadius = radius - 15


	private var isSet: Boolean = false


	//Paint objects
	//to eliminate any new() in OnDraw (GC calling)
	private val oval = Paint()
	private val minutes = RectF()
	private val minutesPaint = Paint()
	private val hours = RectF()
	private val hourPaint = Paint()
	private val seconds = RectF()
	private val secondsPaint = Paint()
	private val watchLine = RectF()
	private val watchLine2 = RectF()
	private val lines = Paint()
	private val zasechka = Paint()


	private val hour
		get() = if (Calendar.getInstance().get(Calendar.HOUR) < 12) Calendar.getInstance().get(Calendar.HOUR) else
			Calendar.getInstance().get(Calendar.HOUR) - 12
	private val minute
		get() = Calendar.getInstance().get(Calendar.MINUTE)
	private val second
		get() = Calendar.getInstance().get(Calendar.SECOND)

	private val ArcDEGREE: Float = -180f

	private val secondDegree
		get() = ArcDEGREE + second.toFloat() / 5 * 30
	private val minuteDegree
		get() = ArcDEGREE + minute.toFloat() / 5 * 30
	private val hourDegree
		get() = ArcDEGREE + (hour * 30) + (minute.toFloat() / 60 * 30)


	init {

		attr.run {
			val typedArray = context.obtainStyledAttributes(attr, R.styleable.ClockView)
			radius = typedArray.getInt(R.styleable.ClockView_radius, 300)
			hourColor = typedArray.getInt(R.styleable.ClockView_arrow_hour_color, Color.BLACK)
			minuteColor = typedArray.getInt(R.styleable.ClockView_arrow_minute_color, Color.BLACK)
			secondColor = typedArray.getInt(R.styleable.ClockView_arrow_second_color, Color.BLACK)
			hourThick = typedArray.getInt(R.styleable.ClockView_hour_thick, 15)
			minuteThick = typedArray.getInt(R.styleable.ClockView_min_thick, 10)
			secondThick = typedArray.getInt(R.styleable.ClockView_sec_thick, 5)
			minuteLength = typedArray.getInt(R.styleable.ClockView_min_length, 295)
			secondLength = typedArray.getInt(R.styleable.ClockView_sec_length, 310)
			hourLength = typedArray.getInt(R.styleable.ClockView_hour_length, 150)
			clockColor = typedArray.getInt(R.styleable.ClockView_hour_watch_color, Color.BLACK)
			clockThick = typedArray.getInt(R.styleable.ClockView_clockThickness, 10)
			typedArray.recycle()

		}
		timeObservable.subscribe { postInvalidate() }
	}


	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

		//TODO with 3 state of width and heigh
		// TODO: EXACTLY, AT_MOST, UNSPECIFIED
		val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
		val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
		val minimal_size = Math.min(width, height)
		radius = (minimal_size) / 2 * 3 / 4
		innerRadius = radius - clockThick
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
	}


	//настраиваем пэйнты в первый запуск
	fun setPaint(xCenter: Float, yCenter: Float): Boolean {

		watchLine.apply { set(xCenter - radius, yCenter - radius, xCenter + radius, yCenter + radius) }
		watchLine2.apply {
			set(xCenter - 3, top, xCenter + 3, top + 30)
		}

		oval.apply {
			color = clockColor
			strokeWidth = clockThick.toFloat()
			style = Paint.Style.STROKE
		}

		lines.apply {
			color = Color.BLACK
			/*	isAntiAlias = true*/
			strokeWidth = 3F
		}


		zasechka.apply {
			color = Color.GRAY
			style = Paint.Style.FILL
		}

		hours.apply { set(xCenter - hourThick, yCenter - hourThick, xCenter + hourThick, yCenter + hourLength) }

		minutes.apply {
			set(xCenter - minuteThick, yCenter - minuteThick, xCenter + minuteThick, yCenter + minuteLength)
		}

		hourPaint.apply {
			color = hourColor
			style = Paint.Style.FILL
		}

		minutesPaint.apply {
			color = minuteColor
			style = Paint.Style.FILL
		}

		seconds.apply {
			set(xCenter - secondThick, yCenter - secondThick, xCenter + secondThick, yCenter + secondLength)
		}

		secondsPaint.apply {
			color = secondColor
			style = Paint.Style.FILL
		}

		return true

	}

	//TODO do smth with save-restore
	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		//not ref pointer - can be used here
		val xCenter = canvas.width / 2f
		val yCenter = canvas.height / 2f

		if (!isSet) isSet = setPaint(xCenter, yCenter)

		canvas.run {
			//круг
			drawArc(watchLine, 0f, 360f, false, oval)

			val initArcSave = save()

			//делаем засечки на 5 минут
			translate(width / 2F, height / 2F)
			for (i in 1..12) {
				rotate(360F / 12, 0f, 0f)
				if (i % 15 != 0) {
					drawLine(0f, radius - 40F, 0F, radius.toFloat(), lines)
				}
			}
			restoreToCount(initArcSave)

			//cтрелки
			val hourSave = save()
			rotate(hourDegree, xCenter, yCenter)
			drawRect(hours, hourPaint)
			restoreToCount(hourSave)

			rotate(minuteDegree, xCenter, yCenter)
			drawRect(minutes, minutesPaint)

			rotate(secondDegree, xCenter, yCenter)
			drawRect(seconds, secondsPaint)

		}
	}


}