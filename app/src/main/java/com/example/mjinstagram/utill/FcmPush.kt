package com.example.mjinstagram.utill

import com.example.mjinstagram.data.PushDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.squareup.okhttp.*

import java.io.IOException


class FcmPush() {
    val JSON = MediaType.parse("application/json; charset=utf-8")
    val url = "https://fcm.googleapis.com/fcm/send"
    val serverKey = "AAAAwiX7fiM:APA91bF6iZLUPXgD5oTZ0Nc4RJd7kr-amKEOMvyCkvhthHgoFVLRHwIs_YGewCYg-92C1tNzZaIUtyL0xpx0APshPEKbuPgjbg3EqsecXfAIdIJz8yAaOy-iojR22QQwTN8x8rKXpmZX"

    var okHttpClient: OkHttpClient? = null
    var gson: Gson? = null
    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    fun sendMessage(destinationUid: String, title: String, message: String) {
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var token = task.result?.get("pushtoken")?.toString()
                println(token)
                var pushDTO = PushDTO()
                pushDTO.to = token
                pushDTO.notification?.title = title
                pushDTO.notification?.body = message

                var body = RequestBody.create(JSON, gson?.toJson(pushDTO))
                var request = Request
                        .Builder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "key=" + serverKey)
                        .url(url)
                        .post(body)
                        .build()
                okHttpClient?.newCall(request)?.enqueue(object : Callback {
                    override fun onFailure(request: Request?, e: IOException?) {
                    }
                    override fun onResponse(response: Response?) {
                        println(response?.body()?.string())
                    }
                })
            }
        }
    }
}