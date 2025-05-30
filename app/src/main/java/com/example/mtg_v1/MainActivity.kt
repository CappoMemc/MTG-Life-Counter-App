package com.example.mtg_v1


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.view.Gravity
import android.view.MenuItem
import android.widget.GridLayout
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {
    // Player Class Setup Try1
    private val handler = Handler(Looper.getMainLooper())
    private val players = mutableListOf<Player>()

    private lateinit var playersLayout: GridLayout

  //  private val handler = Handler(Looper.getMainLooper())
  //  private val hideChangeRunnable = mutableMapOf<Int, Runnable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)



        playersLayout = findViewById(R.id.playersLayout)
        setupPlayers(4)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.player_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_2_players -> setupPlayers(2)
            R.id.menu_3_players -> setupPlayers(3)
            R.id.menu_4_players -> setupPlayers(4)
            R.id.resetButton -> {
                players.forEach { it.resetLife() }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupPlayers(playerCount: Int) {
        players.clear()
        playersLayout.removeAllViews()
        playersLayout.columnCount = 2
        playersLayout.rowCount = (playerCount + 1) / 2

        for (i in 0 until playerCount) {
            val playerGrid = GridLayout(this).apply {
                columnCount = 3
                rowCount = 3
                setPadding(16, 16, 16, 16)
                setBackgroundColor(Color.LTGRAY)
            }

            val layoutParams = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(i % 2, 1f)
                rowSpec = GridLayout.spec(i / 2, 1f)
                setMargins(8, 8, 8, 8)
            }

            playerGrid.layoutParams = layoutParams
            addPlayerViews(playerGrid, i)
            playersLayout.addView(playerGrid)
        }
    }

    private fun addPlayerViews(playerGrid: GridLayout, index: Int) {
        val playerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.DKGRAY)
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(index % 2, 1f)
                rowSpec = GridLayout.spec(index / 2, 1f)
                setMargins(8, 8, 8, 8)
            }
        }

        // Rotate based on player index
        // For example, rotate players in top-left (index 0) by 90 degrees
        if (index % 2 == 0) {
            playerContainer.rotation = 90f
        } else {
            // you can add rotation for other players similarly
            playerContainer.rotation = -90f
        }
        // Add views inside playerContainer
        val lifeText = TextView(this).apply {
            text = "40"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val buttonsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val minusButton = Button(this).apply { text = "-" }
        val plusButton = Button(this).apply { text = "+" }

        buttonsLayout.addView(minusButton)
        buttonsLayout.addView(plusButton)

        playerContainer.addView(lifeText)
        playerContainer.addView(buttonsLayout)

        playerGrid.addView(playerContainer)

        // Create Player object and add to list
        val player = Player(
            lifeDisplay = lifeText,
            changeLifeView = TextView(this).apply { visibility = View.GONE }, // if used elsewhere
            minusButton = minusButton,
            plusButton = plusButton
        )
        players.add(player)
    }






    inner class Player(
        private val lifeDisplay: TextView,
        private val changeLifeView: TextView,
        private val minusButton: Button,
        private val plusButton: Button
    ) {
        private var life: Int = 40
        private var change_life: Int = 0
        private val LONG_PRESS_INCREMENT = 10
        private val SHORT_PRESS_INCREMENT = 1
        private var isLongPressing = false
        private var hideChangeRunnable: Runnable? = null
        private var longPressRunnable: Runnable? = null

        init {
            setupButtonListeners()
        }

        private fun setupButtonListeners() {
            // Regular short-press
            plusButton.setOnClickListener {
                updateLife(SHORT_PRESS_INCREMENT)
            }

            minusButton.setOnClickListener {
                updateLife(-SHORT_PRESS_INCREMENT)
            }

            // Touch listener with proper long press + accessibility support
            plusButton.setOnTouchListener { view, event ->
                handleTouch(event, LONG_PRESS_INCREMENT, view)
                false
            }

            minusButton.setOnTouchListener { view, event ->
                handleTouch(event, -LONG_PRESS_INCREMENT, view)
                false
            }
        }

        private fun handleTouch(event: MotionEvent, increment: Int, view: View) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isLongPressing = true
                    longPressRunnable = object : Runnable {
                        override fun run() {
                            if (isLongPressing) {
                                updateLife(increment)
                                handler.postDelayed(this, 1000)
                            }
                        }
                    }
                    handler.postDelayed(longPressRunnable!!, 500)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!isLongPressing) {
                        view.performClick() // Required for accessibility
                    }
                    isLongPressing = false
                    longPressRunnable?.let { handler.removeCallbacks(it) }
                }
            }
        }

        private fun updateLife(delta: Int) {
            life += delta
            change_life += delta
            lifeDisplay.text = life.toString()
            changeLifeView.text = change_life.toString()

            showCumulativeChange(changeLifeView, change_life)
        }

        private fun showCumulativeChange(changeTextView: TextView, change: Int) {
            changeTextView.text = if (change > 0) "+$change" else "$change"
            changeTextView.visibility = View.VISIBLE

            hideChangeRunnable?.let { handler.removeCallbacks(it) }

            hideChangeRunnable = Runnable {
                changeTextView.visibility = View.GONE
                change_life = 0
            }
            handler.postDelayed(hideChangeRunnable!!, 2000)
        }

        fun resetLife() {
            life = 40
            change_life = 0
            lifeDisplay.text = life.toString()
            changeLifeView.text = ""
            changeLifeView.visibility = View.GONE
        }
    }


}

