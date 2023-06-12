package com.game.beautyLink

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.game.beautyLink.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTransparentClipStatusBar(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        val sharedPreferences: SharedPreferences = getSharedPreferences("ACCOUNT", MODE_PRIVATE)
        ActivityMainBinding.inflate(layoutInflater).apply mainBinding@{
            setContentView(root)
            val account = sharedPreferences.getString(ACCOUNT, "")
            root.post {
                if (account == null) {
                    showLogin()
                    return@post
                }
                if (account.isEmpty()) showLogin()
                else scoreLogin(
                    account,
                    sharedPreferences.getString(PASSWORD, "") ?: ""
                ) { accountResponse ->
                    when (accountResponse.code) {
                        0 -> init()
                        else -> showLogin()
                    }
                }
            }
        }
    }

    private fun ActivityMainBinding.init() {
        start.setOnClickListener {
            startActivity(Intent(this@MainActivity, LinkGameActivity::class.java))
        }
    }

    private fun showLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


}