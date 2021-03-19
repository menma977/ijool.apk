package net.ijool.controller

import okhttp3.Response
import java.io.BufferedReader
import java.io.InputStreamReader

class Render(response: Response) {
  private var result: String = ""

  init {
    val input = BufferedReader(InputStreamReader(response.body!!.byteStream()))
    var readInput = input.readLine()
    while (readInput != null) {
      result += readInput
      readInput = input.readLine()
    }
  }

  fun result(): String {
    return result
  }
}