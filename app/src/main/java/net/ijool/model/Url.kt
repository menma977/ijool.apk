package net.ijool.model

object Url {
  /**
   * @param target String
   * @return String
   */
  fun web(target: String): String {
    return "https://ijool.net/api/${target.replace(".", "/")}"
  }

  /**
   * @return String
   */
  fun doge(): String {
    return "https://www.999doge.com/api/web.aspx"
  }

  fun key(): String {
    return "d73de03e987b4c2e9ba61676de3bf27c"
  }
}