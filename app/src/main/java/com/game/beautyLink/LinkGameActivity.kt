package com.game.beautyLink

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.game.beautyLink.databinding.AddScoreLayoutBinding
import com.game.beautyLink.databinding.FailedLayoutBinding
import com.game.beautyLink.databinding.LinkGameGameBinding
import com.game.beautyLink.databinding.SettingsLayoutBinding
import com.game.beautyLink.databinding.SuccessfulLayoutBinding
import com.game.beautyLink.databinding.WaitingLayoutBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import java.util.TimerTask


class LinkGameActivity : AppCompatActivity() {
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences(
            "ACCOUNT",
            MODE_PRIVATE
        )
    }
    private var timer = Timer()
    private val gson by lazy { Gson() }
    private var isPause = false
    private lateinit var handler: Handler
    private var score = 300L
        set(value) {
            sharedPreferences.edit().putLong(SCORE, value).apply()
            field = value
        }
    private var checkDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTransparentClipStatusBar(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        LinkGameGameBinding.inflate(layoutInflater).apply {
            setContentView(root)
            this@LinkGameActivity.score = sharedPreferences.getLong(SCORE, 300L)
            fun refreshScore() {
                score.text = this@LinkGameActivity.score.toString()
            }
            refreshScore()
            handler = Handler(Looper.getMainLooper()) {
                if (it.what == 1) {
                    checkDialog?.dismiss()
                    refreshScore()
                }
                false
            }
            val items = mutableListOf<Int>().apply {
                add(R.mipmap.link_item1)
                add(R.mipmap.link_item2)
                add(R.mipmap.link_item3)
                add(R.mipmap.link_item4)
                add(R.mipmap.link_item5)
                add(R.mipmap.link_item6)
                add(R.mipmap.link_item7)
                add(R.mipmap.link_item8)
            }
            var remainingTime = 12
            var priceScore = 0f

            linkView.onGameFinish {
                isPause = true
                AlertDialog.Builder(this@LinkGameActivity, R.style.TransparentDialog).apply {
                    val inflate = SuccessfulLayoutBinding.inflate(layoutInflater)
                    setView(inflate.root)
                    create().also { dialog ->
                        this@LinkGameActivity.score += (150L + priceScore.toLong())
                        priceScore = 0f
                        refreshScore()
                        inflate.continueBtn.setOnClickListener {
                            dialog.dismiss()
                        }
                    }.show()
                }
            }

            fun gameOver() {
                priceScore = 0f
                runOnUiThread {
                    AlertDialog.Builder(this@LinkGameActivity, R.style.TransparentDialog).apply {
                        val inflate = FailedLayoutBinding.inflate(layoutInflater)
                        setView(inflate.root)
                        create().also { dialog ->
                            this@LinkGameActivity.score -= 50L
                            refreshScore()
                            inflate.continueBtn.setOnClickListener {
                                dialog.dismiss()
                            }
                        }.show()
                    }
                }
            }

            fun startTimer() {
                timer.cancel()
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    @SuppressLint("SetTextI18n")
                    override fun run() {
                        if (isPause) return
                        remainingTime--
                        runOnUiThread {
                            time.text = "Time: ${remainingTime / 60}:${remainingTime % 60}"
                        }
                        if (remainingTime <= 0) {
                            timer.cancel()
                            gameOver()
                        }
                    }
                }, 0, 1000)
            }

            fun startGame(time: Int, hardCore: Boolean = false) {
                linkView.reset(items, hardCore)
                remainingTime = time
                pause.setImageResource(R.mipmap.link_stop_icon)
                isPause = false
                startTimer()
            }

            fun addScore() {
                showAddScoreDialog { score ->
                    isPause = true
                    val fScore = Integer.parseInt(score).toFloat()
                    if (fScore >= 10000f) {
                        startGame(8, true)
                    } else if (fScore >= 1000) {
                        startGame(8)
                    } else {
                        startGame(10)
                    }
                    priceScore = fScore
                    return@showAddScoreDialog

                }
            }
            addScore.setOnClickListener {
                addScore()
            }
            settings.setOnClickListener {
                showDialog()
            }
            pause.setOnClickListener {
                isPause = !isPause
                pause.setImageResource(if (isPause) R.mipmap.link_start_icon else R.mipmap.link_stop_icon)
            }
            reset.setOnClickListener {
                this@LinkGameActivity.score -= 100L
                if (this@LinkGameActivity.score < 0) {
                    addScore()
                    return@setOnClickListener
                } else refreshScore()
                startGame(12)
            }
        }
    }

    private fun showDialog() {
        isPause = true
        AlertDialog.Builder(this, R.style.TransparentDialog).apply {
            val layoutBinding = SettingsLayoutBinding.inflate(layoutInflater)
            setView(layoutBinding.root)

            create().also { dialog ->
                layoutBinding.close.setOnClickListener {
                    dialog.dismiss()
                    isPause = false
                }
                layoutBinding.privacyLink.setOnClickListener {
                    showPrivacy("https://www.baidu.com")
                }
                layoutBinding.homepage.setOnClickListener {
                    startActivity(Intent(this@LinkGameActivity, MainActivity::class.java))
                    finish()
                }
            }.show()
        }
    }

    private fun showAddScoreDialog(onStart: (String) -> Unit) {
        AlertDialog.Builder(this, R.style.TransparentDialog).apply {
            val layoutBinding = AddScoreLayoutBinding.inflate(layoutInflater)
            setView(layoutBinding.root)

            create().also { dialog ->
                layoutBinding.close.setOnClickListener {
                    dialog.dismiss()
                    isPause = false
                }
                fun Button.setClick() {
                    setOnClickListener {
                        layoutBinding.input.setText(text)
                    }
                }
                layoutBinding.apply {
                    button100.setClick()
                    button200.setClick()
                    button500.setClick()
                    button1000.setClick()
                    button3000.setClick()
                    button5000.setClick()
                    button10000.setClick()
                    button20000.setClick()
                }
                layoutBinding.recharge.setOnClickListener {
                    onStart(layoutBinding.input.text.toString())
                    dialog.dismiss()
                }
            }.show()
        }
    }

    private fun showPrivacy(link: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(link)
            startActivity(intent)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

}