package com.game.beautyLink

import android.app.Activity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

val scoreClient by lazy { Score().getScoreService() }

fun Activity.scoreLogin(account: String, password: String, onResponse: (AccountResponse) -> Unit) {
    scoreClient.login(account, password)
        .enqueue(object : Callback<AccountResponse> {
            override fun onResponse(
                call: Call<AccountResponse>,
                response: Response<AccountResponse>
            ) {
                response.body()?.let { accountResponse ->
                    onResponse(accountResponse)
                } ?: run {
                    msg(getString(R.string.network_error))
                }
            }

            override fun onFailure(
                call: Call<AccountResponse>,
                t: Throwable
            ) {
                msg(getString(R.string.login_failed))
            }

        })
}

class Score {

    interface ScoreRequest {
        @POST("/")
        @FormUrlEncoded
        fun login(
            @Field("account") account: String,
            @Field("password") password: String
        ): Call<AccountResponse>

        @POST("/")
        @FormUrlEncoded
        fun register(
            @Field("account") account: String,
            @Field("password") password: String
        ): Call<AccountResponse>

    }

    private val retrofitClient = Retrofit.Builder().apply {

        addConverterFactory(GsonConverterFactory.create())
    }.build()

    fun getScoreService(): ScoreRequest = retrofitClient.create(ScoreRequest::class.java)

}