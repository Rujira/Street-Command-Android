package com.codinghub.apps.streetcommand.app

import com.codinghub.apps.streetcommand.BuildConfig
import com.codinghub.apps.streetcommand.models.preferences.AppPreferences
import com.codinghub.apps.streetcommand.models.repository.RemoteRepository
import com.codinghub.apps.streetcommand.models.repository.Repository
import com.codinghub.apps.streetcommand.models.repository.StreetCommandAPI
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Injection{

    fun provideRepository(): Repository =
        RemoteRepository

    private fun provideRetrofitLogin(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppPreferences.getServiceURL().toString())
            .addConverterFactory(GsonConverterFactory.create())
            .client(provideOkHttpClientForLogin())
            .build()
    }

    private fun provideLoggingInterceptor(): HttpLoggingInterceptor {

        val logging = HttpLoggingInterceptor()
        logging.level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        return logging
    }

    private fun provideOkHttpClientForLogin(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(provideLoggingInterceptor())

        httpClient.addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build()

            chain.proceed(request)
        }
        return httpClient.build()
    }

    fun provideStreetCommandLoginAPI(): StreetCommandAPI {
        return provideRetrofitLogin().create(StreetCommandAPI::class.java)
    }

    private fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppPreferences.getServiceURL().toString())
            .addConverterFactory(GsonConverterFactory.create())
            .client(provideOkHttpClient())
            .build()
    }

    private fun provideOkHttpClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(provideLoggingInterceptor())

        httpClient.addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer ${AppPreferences.getAccessToken()}")
                .build()

            chain.proceed(request)
        }
        return httpClient.build()
    }

    fun provideStreetCommandAPI(): StreetCommandAPI {
        return provideRetrofit().create(StreetCommandAPI::class.java)
    }

}