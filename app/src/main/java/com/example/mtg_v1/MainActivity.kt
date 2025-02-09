package com.example.mtg_v1

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Declare references to the player life TextViews and buttons
    private lateinit var player1LifeText: TextView
    private lateinit var player2LifeText: TextView
    private lateinit var player3LifeText: TextView
    private lateinit var player4LifeText: TextView

    private lateinit var player1ChangeText: TextView
    private lateinit var player2ChangeText: TextView
    private lateinit var player3ChangeText: TextView
    private lateinit var player4ChangeText: TextView

    private lateinit var player1Minus1Button: Button
    private lateinit var player1Plus1Button: Button
    private lateinit var player2Minus1Button: Button
    private lateinit var player2Plus1Button: Button
    private lateinit var player3Minus1Button: Button
    private lateinit var player3Plus1Button: Button
    private lateinit var player4Minus1Button: Button
    private lateinit var player4Plus1Button: Button

    private lateinit var resetButton: Button

    private var player1Life = 40
    private var player2Life = 40
    private var player3Life = 40
    private var player4Life = 40

    private var player1Change = 0
    private var player2Change = 0
    private var player3Change = 0
    private var player4Change = 0

    private val LONG_PRESS_INCREMENT = 10   // Life change for long press
    private val SHORT_PRESS_INCREMENT = 1   // Life change for short press

    private val handler = Handler(Looper.getMainLooper())
    private val hideChangeRunnable = mutableMapOf<Int, Runnable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)     // Keep the screen on

        // Initialize references from the XML layout
        player1LifeText = findViewById(R.id.player1Life)
        player2LifeText = findViewById(R.id.player2Life)
        player3LifeText = findViewById(R.id.player3Life)
        player4LifeText = findViewById(R.id.player4Life)

        player1ChangeText = findViewById(R.id.player1Change)
        player2ChangeText = findViewById(R.id.player2Change)
        player3ChangeText = findViewById(R.id.player3Change)
        player4ChangeText = findViewById(R.id.player4Change)

        player1Minus1Button = findViewById(R.id.player1Minus1)
        player1Plus1Button = findViewById(R.id.player1Plus1)
        player2Minus1Button = findViewById(R.id.player2Minus1)
        player2Plus1Button = findViewById(R.id.player2Plus1)
        player3Minus1Button = findViewById(R.id.player3Minus1)
        player3Plus1Button = findViewById(R.id.player3Plus1)
        player4Minus1Button = findViewById(R.id.player4Minus1)
        player4Plus1Button = findViewById(R.id.player4Plus1)

        resetButton = findViewById(R.id.resetButton)

        // Set up button click listeners to update life totals


        setupButtonListeners(player1Plus1Button, 1, SHORT_PRESS_INCREMENT)
        setupButtonListeners(player1Minus1Button, 1, -SHORT_PRESS_INCREMENT)
        setupButtonListeners(player2Plus1Button, 2, SHORT_PRESS_INCREMENT)
        setupButtonListeners(player2Minus1Button, 2, -SHORT_PRESS_INCREMENT)
        setupButtonListeners(player3Plus1Button, 3, SHORT_PRESS_INCREMENT)
        setupButtonListeners(player3Minus1Button, 3, -SHORT_PRESS_INCREMENT)
        setupButtonListeners(player4Plus1Button, 4, SHORT_PRESS_INCREMENT)
        setupButtonListeners(player4Minus1Button, 4, -SHORT_PRESS_INCREMENT)

        // Set up reset button to reset all life totals to 40
        resetButton.setOnClickListener {resetLife()}

        // Display initial life values
        updateLifeDisplay()
    }

    private fun setupButtonListeners(button: Button, player: Int, delta: Int) {
        button.setOnClickListener {
            updateLife(player, delta)
        }

        button.setOnLongClickListener {
            performLongClickIncrement(button, player, delta)
            true    // Indicate long press was handled
        }
    }

    private fun performLongClickIncrement(button: Button, player: Int, delta: Int) {
        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                updateLife(player, delta*LONG_PRESS_INCREMENT)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)

        button.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
                handler.removeCallbacks(runnable)  // Stop when button is released
            }
            false
        }
    }

    // Function to update the life of a player
    private fun updateLife(player: Int, delta: Int) {
        when (player) {
            1 -> {
                player1Life += delta
                player1Change += delta
                showCumulativeChange(player1ChangeText, 1, player1Change)
            }
            2 -> {
                player2Life += delta
                player2Change += delta
                showCumulativeChange(player2ChangeText, 2, player2Change)
            }
            3 -> {
                player3Life += delta
                player3Change += delta
                showCumulativeChange(player3ChangeText, 3, player3Change)
            }
            4 -> {
                player4Life += delta
                player4Change += delta
                showCumulativeChange(player4ChangeText, 4, player4Change)
            }
        }
        updateLifeDisplay()
    }
    // Function to show cumulative life change above health counter
    private fun showCumulativeChange(changeTextView: TextView, player: Int, change: Int) {
        changeTextView.text = if ( change > 0) "+$change" else "$change"
        changeTextView.visibility = TextView.VISIBLE

        hideChangeRunnable[player]?.let {handler.removeCallbacks(it)}
        val runnable = Runnable{
            changeTextView.visibility = TextView.GONE
            resetCumulativeChange(player)
        }
        hideChangeRunnable[player] = runnable
        handler.postDelayed(runnable, 2000)     // shows 2 seconds cumulative life change
    }

    private fun resetCumulativeChange(player: Int){
        when (player) {
            1 -> player1Change = 0
            2 -> player2Change = 0
            3 -> player3Change = 0
            4 -> player4Change = 0
        }
    }

    // Function to reset all player life totals to 40
    private fun resetLife() {
        player1Life = 40
        player2Life = 40
        player3Life = 40
        player4Life = 40
        resetCumulativeChange(1)
        resetCumulativeChange(2)
        resetCumulativeChange(3)
        resetCumulativeChange(4)
        updateLifeDisplay()
    }

    // Function to update the life display TextViews with the current life totals
    private fun updateLifeDisplay() {
        player1LifeText.text = player1Life.toString()
        player2LifeText.text = player2Life.toString()
        player3LifeText.text = player3Life.toString()
        player4LifeText.text = player4Life.toString()
    }
}
