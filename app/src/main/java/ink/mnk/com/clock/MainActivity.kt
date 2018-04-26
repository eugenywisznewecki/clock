package ink.mnk.com.clock

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)


		clockView.minuteColor = Color.RED
		clockView.hourColor = Color.GREEN
		clockView.secondThick = 15
		clockView.secondColor = Color.BLUE

	}
}
