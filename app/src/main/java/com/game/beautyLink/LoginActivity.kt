package com.game.beautyLink

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import com.game.beautyLink.databinding.LoginLayoutBinding

class LoginActivity : AppCompatActivity() {

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences(
            "ACCOUNT",
            MODE_PRIVATE
        )
    }

    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTransparentClipStatusBar(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        setContentView(LoginLayoutBinding.inflate(layoutInflater).apply {
            fun accountWrong(msg: String) {
                accountWrong.text = msg
                accountWrong.isGone = false
            }

            fun passwordWrong(msg: String) {
                passwordWrong.text = msg
                passwordWrong.isGone = false
            }
            accountInput.addTextChangedListener {
                accountWrong.text = ""
                accountWrong.isGone = true
            }
            passwordInput.addTextChangedListener {
                passwordWrong.text = ""
                passwordWrong.isGone = true
            }
            close.setOnClickListener {
                finish()
            }
            login.setOnClickListener {
                val inputAccount = accountInput.text.toString()
                if (inputAccount.isEmpty()) {
                    accountWrong(getString(R.string.account_must_not_be_null))
                    return@setOnClickListener
                }
                val inputPassword = passwordInput.text.toString()
                if (inputPassword.isEmpty()) {
                    passwordWrong(getString(R.string.password_must_not_be_null))
                    return@setOnClickListener
                }
                if (inputPassword.length < 6) {
                    passwordWrong(getString(R.string.the_password_must_be_six_digits_or_more))
                    return@setOnClickListener
                }
                scoreLogin(inputAccount, inputPassword) { accountResponse ->
                    when (accountResponse.code) {
                        0 -> {
                            sharedPreferences.edit()
                                .putString(ACCOUNT, inputAccount)
                                .putString(PASSWORD, inputPassword)
                                .commit()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        }

                        -1 -> {
                            passwordWrong(accountResponse.msg)
                        }

                        else -> {
                            msg(getString(R.string.network_error))
                        }
                    }
                }
            }
            signUp.setOnClickListener {
                startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
            }
        }.root)
    }
}