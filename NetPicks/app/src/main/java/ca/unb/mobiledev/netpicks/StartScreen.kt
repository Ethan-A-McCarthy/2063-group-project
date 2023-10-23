package ca.unb.mobiledev.netpicks

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StartScreen : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_screen)

        val startButton = findViewById<Button>(R.id.start)
        startButton.setOnClickListener{
            val intent = Intent(this@StartScreen, MainActivity::class.java)
            startActivity(intent)
        }
    }
}