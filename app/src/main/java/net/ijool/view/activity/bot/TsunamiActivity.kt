package net.ijool.view.activity.bot

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class TsunamiActivity : AppCompatActivity() {
  private lateinit var loading: Loading
  private lateinit var chart: LineChart
  private lateinit var jobLoadBalance: CompletableJob
  private lateinit var jobBot: CompletableJob
  private lateinit var balanceRaw: BigDecimal
  private lateinit var balance: TextView
  private lateinit var start: Button
  private lateinit var stop: Button
  private lateinit var data: ArrayList<Entry>
  private lateinit var lineDataSet: LineDataSet
  private lateinit var arrayLineDataSet: ArrayList<ILineDataSet>
  private lateinit var lineData: LineData
  private var indexChart: Float = 0F
  private var seed: String = (0..99999).random().toString()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_tsunami)

    loading = Loading(this)
    loading.openDialog()

    chart = findViewById(R.id.chart)
    balance = findViewById(R.id.textViewBalanceBot)
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
      GlobalScope.launch(Dispatchers.Main) {
        setupChart()
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
      while (true) {
        if (jobBot.isCancelled) {
          break
        }
        val delta = System.currentTimeMillis() - time
        if (delta >= 2000) {
          time = System.currentTimeMillis()
          try {
            val post = BotController().tsunami(applicationContext, balanceRaw, seed)
            Log.i("bet", post.toString())
            when {
              post.getInt("code") < 400 -> {
                runOnUiThread {
                  val responseBalance = post.getString("balance").toBigDecimal()
                  addChartData(Coin.decimalToCoin(responseBalance).toFloat())
                  if (responseBalance < BigDecimal(0)) {
                    balance.text = "LOSE"
                    balance.setTextColor(getColor(R.color.Danger))
                  } else {
                    balance.text = "${Coin.decimalToCoin(responseBalance).toPlainString()} DOGE"
                  }
                }
              }
              post.getInt("code") == 408 -> {
                jobBot.cancel(CancellationException("Insufficient Funds"))
                break
              }
              else -> {
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
    if (indexChart > 0.0015F) {
      lineData.removeEntry(indexChart - 0.0016F, 0)
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
    lineDataSet = LineDataSet(data, "Tsunami")

    arrayLineDataSet = ArrayList()
    arrayLineDataSet.add(lineDataSet)

    lineData = LineData(lineDataSet)

    chart.data = lineData
  }

  override fun onBackPressed() {
    super.onBackPressed()
    jobBot.cancel(CancellationException("Tsunami has been close"))
    val move = Intent(this, NavigationActivity::class.java)
    startActivity(move)
    finish()
  }
}