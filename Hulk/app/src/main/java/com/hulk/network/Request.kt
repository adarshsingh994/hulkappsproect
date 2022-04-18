package com.hulk.network

import okhttp3.*
import okhttp3.Request
import okio.IOException

/**
 * A helper class which uses [OkHttpClient] to make network calls
 */
class Request {
    private val client = OkHttpClient()

    /**
     * Make request to a url
     *
     * @param url the api endpoint to make request
     * @param listener listen to the response
     */
    fun run(url : String, listener: (response: String?, error : String?) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                listener(null, e.localizedMessage)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) listener(null, "Unexpected code $response")
                    else listener(response.body?.string(), null)
                }
            }
        })
    }
}