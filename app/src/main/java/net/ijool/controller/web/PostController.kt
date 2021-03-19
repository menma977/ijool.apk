package net.ijool.controller.web

import android.util.Log
import net.ijool.controller.Render
import net.ijool.model.Url
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class PostController(private var targetUrl: String, private var token: String? = null, private var body: FormBody.Builder) : Callable<JSONObject> {
  constructor(targetUrl: String, body: FormBody.Builder) : this(targetUrl, null, body)

  override fun call(): JSONObject {
    return try {
      val client = OkHttpClient.Builder()
      client.connectTimeout(30, TimeUnit.SECONDS)
      client.writeTimeout(30, TimeUnit.SECONDS)
      client.readTimeout(30, TimeUnit.SECONDS)
      val request = Request.Builder()
      request.url(Url.web(targetUrl))
      request.post(body.build())
      if (!token.isNullOrEmpty()) {
        request.addHeader("Authorization", "Bearer ${token!!}")
      }
      request.addHeader("X-Requested-With", "XMLHttpRequest")
      val response = client.build().newCall(request.build()).execute()
      val raw = Render(response).result()
      val convertJSON = JSONObject(raw)

      return Handler(response, convertJSON).result()
    } catch (e: Exception) {
      Log.e("json", e.stackTraceToString())
      JSONObject().put("code", 500).put("data", e.message).put("logout", false)
    }
  }
}