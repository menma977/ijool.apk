package net.ijool.view.activity.bot

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.*
import net.ijool.R
import net.ijool.config.Coin
import net.ijool.config.Loading
import net.ijool.controller.BotController
import net.ijool.controller.NavigationController
import net.ijool.view.activity.NavigationActivity
import java.math.BigDecimal
import java.util.*

class MoseeActivity : AppCompatActivity() {
  private lateinit var loading: Loading
  private lateinit var chart: LineChart
  private lateinit var jobLoadBalance: CompletableJob
  private lateinit var jobBot: CompletableJob
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
      initBot()
    }

    stop.setOnClickListener {
      jobBot.cancel(CancellationException("Stop"))
      start.isEnabled = true
      stop.isEnabled = false
    }

    initBalance()
  }

  private fun initBalance() {
    loading.openDialog()
    if (!::jobLoadBalance.isInitialized || jobLoadBalance.isCompleted) {
      jobLoadBalance = Job()
    }
    CoroutineScope(Dispatchers.IO + jobLoadBalance).launch {
      balanceRaw = NavigationController().getBalance(applicationContext, true).toBigDecimal()
      payInRaw = Coin.coinToDecimal(BigDecimal(0.01))
      payInRawDefault = payInRaw
      balanceRawTarget = Coin.coinToDecimal(Coin.decimalToCoin(balanceRaw + (balanceRaw * target)))
      balanceRawLose = Coin.coinToDecimal(Coin.decimalToCoin(balanceRaw - (balanceRaw * lose)))
      GlobalScope.launch(Dispatchers.Main) {
        setupChart()
        payIn.text = "${Coin.decimalToCoin(payInRaw).toPlainString()} DOGE"
        balance.text = "${Coin.decimalToCoin(balanceRaw).toPlainString()} DOGE"
        loading.closeDialog()
      }
    }
  }

  private fun initBot() {
    if (!::jobBot.isInitialized || jobBot.isCompleted) {
      jobBot = Job()
      jobBot.invokeOnCompletion { throwable ->
        throwable?.message.let {
          var message = it
          if (message.isNullOrBlank()) {
            message = "null error"
          }
          GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
          }
        }
      }
    }
    runBot()
  }

  private fun runBot() {
    start.isEnabled = false
    stop.isEnabled = true
    var time = System.currentTimeMillis()

    CoroutineScope(Dispatchers.IO + jobBot).launch {
      while (balanceRaw in balanceRawLose..balanceRawTarget) {
        if (jobBot.isCancelled) {
          break
        }
        val delta = System.currentTimeMillis() - time
        if (delta >= 2000) {
          time = System.currentTimeMillis()
          try {
            payInRaw *= formula.toBigDecimal()
            val post = BotController().ninku(applicationContext, payInRaw, seed)
            when {
              post.getInt("code") < 400 -> {
                runOnUiThread {
                  seed = post.getString("seed")
                  val responseBalance = post.getString("balance").toBigDecimal()
                  val payInSend = payInRaw

                  GlobalScope.launch(Dispatchers.Main) {
                    addChartData(Coin.decimalToCoin(responseBalance).toFloat())
                    if (responseBalance < BigDecimal(0)) {
                      balance.text = "LOSE"
                      balance.setTextColor(getColor(R.color.Danger))
                    } else {
                      balance.text = "${Coin.decimalToCoin(responseBalance).toPlainString()} DOGE"
                      payIn.text = "${Coin.decimalToCoin(payInSend).toPlainString()} DOGE"
                    }
                  }

                  if (responseBalance < balanceRaw) {
                    formula += 19
                  } else {
                    if (formula == 1) {
                      formula = 1
                    } else {
                      formula -= 1
                    }
                  }

                  balanceRaw = responseBalance
                  payInRaw = Coin.coinToDecimal(BigDecimal(0.01))
                }
              }
              post.getInt("code") == 408 -> {
                jobBot.cancel(CancellationException("Insufficient Funds"))
                break
              }
              else -> {
                payInRaw = Coin.coinToDecimal(BigDecimal(0.01))
                GlobalScope.launch(Dispatchers.Main) {
                  balance.text = post.getString("message")
                }
                delay(60000)
              }
            }
          } catch (e: Exception) {
            delay(60000)
          }
        }
      }

      GlobalScope.launch(Dispatchers.Main) {
        start.isEnabled = true
        stop.isEnabled = false
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
    lineDataSet = LineDataSet(data, "Mosee")

    arrayLineDataSet = ArrayList()
    arrayLineDataSet.add(lineDataSet)

    lineData = LineData(lineDataSet)

    chart.data = lineData
  }

  override fun onBackPressed() {
    super.onBackPressed()
    if (::jobBot.isInitialized) {
      jobBot.cancel(CancellationException("Mosee has been close"))
    }
    val move = Intent(this, NavigationActivity::class.java)
    startActivity(move)
    finish()
  }
}