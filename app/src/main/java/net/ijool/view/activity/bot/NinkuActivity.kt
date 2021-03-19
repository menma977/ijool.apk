package net.ijool.view.activity.bot

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import net.ijool.R
import net.ijool.config.Coin
import net.ijool.config.Loading
import net.ijool.controller.BotController
import net.ijool.controller.NavigationController
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

class NinkuActivity : AppCompatActivity() {
  private lateinit var loading: Loading
  private lateinit var chart: LineChart
  private lateinit var payInRawDefault: BigDecimal
  private lateinit var payInRaw: BigDecimal
  private lateinit var balanceRaw: BigDecimal
  private lateinit var balanceRawTarget: BigDecimal
  private lateinit var balanceRawLose: BigDecimal
  private lateinit var balance: TextView
  private lateinit var payIn: TextView
  private lateinit var start: Button
  private lateinit var stop: Button
  private lateinit var data: ArrayList<Entry>
  private lateinit var lineDataSet: LineDataSet
  private lateinit var arrayLineDataSet: ArrayList<ILineDataSet>
  private lateinit var lineData: LineData
  private var indexChart: Float = 0F
  private var isRunning: Boolean = false
  private var formula: Int = 1
  private var target = BigDecimal(0.06)
  private var lose = BigDecimal(5)
  private var seed: String = (0..99999).random().toString()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_ninku)

    loading = Loading(this)
    loading.openDialog()

    chart = findViewById(R.id.chart)
    balance = findViewById(R.id.textViewBalanceBot)
    payIn = findViewById(R.id.textViewPayIn)
    start = findViewById(R.id.buttonStart)
    stop = findViewById(R.id.buttonStop)

    start.setOnClickListener {
      loading.openDialog()
      Timer().schedule(100) {
        balanceRaw = NavigationController().getBalance(applicationContext, true).toBigDecimal()
        payInRaw = Coin.coinToDecimal(BigDecimal(0.1))
        payInRawDefault = payInRaw
        balanceRawTarget = Coin.coinToDecimal(Coin.decimalToCoin(balanceRaw + (balanceRaw * target)))
        balanceRawLose = Coin.coinToDecimal(Coin.decimalToCoin(balanceRaw - (balanceRaw * lose)))
        formula = 1
        runOnUiThread {
          payIn.text = "${Coin.decimalToCoin(payInRaw).toPlainString()} DOGE"
          loading.closeDialog()
          onBot()
        }
      }
    }

    stop.setOnClickListener {
      isRunning = false
      start.isEnabled = true
      stop.isEnabled = false
    }

    Timer().schedule(100) {
      balanceRaw = NavigationController().getBalance(applicationContext, true).toBigDecimal()
      payInRaw = Coin.coinToDecimal(BigDecimal(0.1))
      payInRawDefault = payInRaw
      balanceRawTarget = Coin.coinToDecimal(Coin.decimalToCoin(balanceRaw + (balanceRaw * target)))
      balanceRawLose = Coin.coinToDecimal(Coin.decimalToCoin(balanceRaw - (balanceRaw * lose)))
      runOnUiThread {
        setupChart()
        payIn.text = "${Coin.decimalToCoin(payInRaw).toPlainString()} DOGE"
        balance.text = "${Coin.decimalToCoin(balanceRaw).toPlainString()} DOGE"
        loading.closeDialog()
      }
    }
  }

  private fun onBot() {
    start.isEnabled = false
    stop.isEnabled = true
    isRunning = true
    var time = System.currentTimeMillis()
    val trigger = Object()
    Timer().schedule(100) {
      while (balanceRaw in balanceRawLose..balanceRawTarget) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 2000) {
          if (!isRunning) {
            break
          }
          time = System.currentTimeMillis()
          synchronized(trigger) {
            try {
              payInRaw *= formula.toBigDecimal()
              val post = BotController().ninku(applicationContext, payInRaw, seed)
              if (post.getInt("code") < 400) {
                runOnUiThread {
                  seed = post.getString("seed")
                  val responseBalance = post.getString("balance").toBigDecimal()

                  addChartData(Coin.decimalToCoin(responseBalance).toFloat())
                  if (responseBalance < BigDecimal(0)) {
                    balance.text = "LOSE"
                    balance.setTextColor(getColor(R.color.Danger))
                  } else {
                    balance.text = "${Coin.decimalToCoin(responseBalance).toPlainString()} DOGE"
                    payIn.text = "${Coin.decimalToCoin(payInRaw).toPlainString()} DOGE"
                  }

                  if (responseBalance < balanceRaw) {
                    formula *= 2
                  } else {
                    formula = 1
                  }

                  balanceRaw = responseBalance
                  payInRaw = Coin.coinToDecimal(BigDecimal(0.1))
                }
              } else if (post.getInt("code") == 408) {
                isRunning = false
              } else {
                runOnUiThread {
                  payInRaw = Coin.coinToDecimal(BigDecimal(0.1))
                  balance.text = post.getString("message")
                }
              }
            } catch (e: Exception) {
              trigger.wait(60000)
            }
          }
        }
      }
      runOnUiThread {
        start.isEnabled = true
        stop.isEnabled = false
        isRunning = false
      }
    }
  }

  private fun addChartData(balance: Float) {
    lineDataSet = chart.data.getDataSetByIndex(0) as LineDataSet
    if (indexChart > 0.0020F) {
      lineData.removeEntry(indexChart - 0.0021F, 0)
    }
    indexChart += 0.0001F
    lineData.addEntry(Entry(indexChart, balance), 0)
    chart.data.notifyDataChanged()
    chart.notifyDataSetChanged()
    chart.invalidate()
  }

  private fun setupChart() {
    chart.setBackgroundColor(getColor(R.color.background))
    chart.description.isEnabled = false
    chart.setTouchEnabled(false)
    chart.setDrawGridBackground(false)

    data = ArrayList()
    data.add(Entry(0F, Coin.decimalToCoin(balanceRaw).toFloat()))
    lineDataSet = LineDataSet(data, "Ninku")

    arrayLineDataSet = ArrayList()
    arrayLineDataSet.add(lineDataSet)

    lineData = LineData(lineDataSet)

    chart.data = lineData
  }

  override fun onBackPressed() {
    super.onBackPressed()
    isRunning = false
    finish()
  }
}