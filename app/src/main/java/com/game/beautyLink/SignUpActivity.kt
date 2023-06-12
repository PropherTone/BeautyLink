package com.game.beautyLink

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import com.game.beautyLink.databinding.SignUpLayoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {
    val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences(
            "ACCOUNT",
            MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTransparentClipStatusBar(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        setContentView(SignUpLayoutBinding.inflate(layoutInflater).apply {
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
            passwordInputAgain.addTextChangedListener {
                passwordWrong.text = ""
                passwordWrong.isGone = true
            }
            close.setOnClickListener {
                finish()
            }
            signUp.setOnClickListener {
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
                val inputPasswordAgain = passwordInputAgain.text.toString()
                if (inputPasswordAgain.isEmpty()) {
                    passwordWrong(getString(R.string.password_must_not_be_null))
                    return@setOnClickListener
                }
                if (inputPassword != inputPasswordAgain) {
                    passwordWrong(getString(R.string.entered_passwords_differ))
                    return@setOnClickListener
                }
                scoreClient.register(inputAccount, inputPassword)
                    .enqueue(object : Callback<AccountResponse> {
                        @SuppressLint("ApplySharedPref")
                        override fun onResponse(
                            call: Call<AccountResponse>,
                            response: Response<AccountResponse>
                        ) {
                            response.body()?.let { accountResponse ->
                                when (accountResponse.code) {
                                    0 -> {
                                        sharedPreferences.edit()
                                            .putString(ACCOUNT, inputAccount)
                                            .putString(PASSWORD, inputPassword)
                                            .commit()
                                        startActivity(
                                            Intent(
                                                this@SignUpActivity,
                                                MainActivity::class.java
                                            )
                                        )
                                        finish()
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

                        override fun onFailure(
                            call: Call<AccountResponse>,
                            t: Throwable
                        ) {
                            msg(getString(R.string.network_error))
                        }

                    })
            }
        }.root)
    }
}