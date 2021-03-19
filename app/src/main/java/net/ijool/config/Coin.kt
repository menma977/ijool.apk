package net.ijool.config

import java.math.BigDecimal
import java.text.DecimalFormat

object Coin {
  private var longFormat = BigDecimal(100000000)
  private var bigDecimalFormat = BigDecimal(0.00000001)
  private val decimalFormat = DecimalFormat("#.########")

  fun coinToDecimal(value: BigDecimal): BigDecimal {
    return value.multiply(longFormat).setScale(0, BigDecimal.ROUND_HALF_DOWN)
  }

  fun decimalToCoin(value: BigDecimal): BigDecimal {
    return decimalFormat.format(value.multiply(bigDecimalFormat).setScale(8, BigDecimal.ROUND_HALF_DOWN)).replace(",", ".").toBigDecimal()
  }
}