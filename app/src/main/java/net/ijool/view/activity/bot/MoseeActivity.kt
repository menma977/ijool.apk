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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import net.ijool.R
import net.ijool.config.Coin
import net.ijool.config.Loading
import net.ijool.controller.DogeController
import net.ijool.controller.volley.doge.HandleError
import net.ijool.controller.volley.doge.HandleResponse
import net.ijool.model.User
import net.ijool.view.activity.NavigationActivity
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*

class MoseeActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var chart: LineChart
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
  private var target = BigDecimal(0.02)
  private var lose = BigDecimal(0.5)
  private var seed: String = (0..99999).random().toString()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_ninku)

    user = User(this)
    loading = Loading(this)
    loading.openDialog()

    chart = findViewById(R.id.chart)
    balance = findViewById(R.id.textViewBalanceBot)
    payIn = findViewById(R.id.textViewPayIn)
    start = findViewById(R.id.buttonStart)
    stop = findViewById(R.id.buttonStop)

    start.setOnClickListener {
      payInRaw = Coin.coinToDecimal(Coin.decimalToCoin(balanceRaw) * BigDecimal(0.001))
      payInRawDefault = payInRaw
      balanceRawTarget = Coin.coinToDecimal(Coin.decimalToCoin(balanceRaw + (balanceRaw * target)))
      balanceRawLose = Coin.coinToDecimal(Coin.decimalToCoin(balanceRaw - (balanceRaw * lose)))

      initBot()
    }

    stop.setOnClickListener {
      jobBot.cancel(CancellationException("Mosee has been stop"))
      start.isEnabled = true
      stop.isEnabled = false
    }

    initBalance()
  }

  private fun initBalance() {
    loading.openDialog()
    DogeController(this).balance(user.getString("cookie_bot")).cll({
      val response = JSONObject(it)
      balanceRaw = response.getString("Balance").toBigDecimal()
      payInRaw = Coin.coinToDecimal(Coin.decimalToCoin(balanceRaw) * BigDecimal(0.001))
      payInRawDefault = payInRaw
      balanceRawTarget = Coin.coinToDecimal(Coin.decimalToCoin(balanceRaw + (balanceRaw * target)))
      balanceRawLose = Coin.coinToDecimal(Coin.decimalToCoin(balanceRaw - (balanceRaw * lose)))
      setupChart()
      payIn.text = "${Coin.decimalToCoin(payInRaw).toPlainString()} DOGE"
      balance.text = "${Coin.decimalToCoin(balanceRaw).toPlainString()} DOGE"
      loading.closeDialog()
    }, {
      balance.text = "0 DOGE"
      loading.closeParent()
    })
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
          GlobalScope.launch(Main) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
          }
        }
      }
    }
    runBot()
  }

  private fun runBot() {
    val cookie = user.getString("cookie_bot")
    start.isEnabled = false
    stop.isEnabled = true
    var time = System.currentTimeMillis()
    CoroutineScope(IO + jobBot).launch {
      while (balanceRaw in balanceRawLose..balanceRawTarget) {
        if (jobBot.isCancelled) {
          break
        }
        val delta = System.currentTimeMillis() - time
        if (delta >= 2000) {
          time = System.currentTimeMillis()
          try {
            val payInToSend = payInRaw * formula.toBigDecimal()
            DogeController(applicationContext).mosee(cookie, payInToSend, seed).cll({
              val response = JSONObject(it)
              val handler = HandleResponse(response).result()
              when {
                handler.getInt("code") == 200 -> {
                  seed = response.getString("Next")
                  val payOut = response.getString("PayOut").toBigDecimal()
                  val profit = payOut - payInToSend
                  val balanceRawResult = response.getString("StartingBalance").toBigDecimal() + profit

                  if (balanceRawResult < balanceRaw) {
                    formula += 20
                  } else {
                    if (formula == 1) {
                      formula = 1
                    } else {
                      formula -= 1
                    }
                  }

                  GlobalScope.launch(Main) {
                    addChartData(Coin.decimalToCoin(balanceRawResult).toFloat())
                    if (profit < BigDecimal(0)) {
                      balance.setTextColor(getColor(R.color.Danger))
                      payIn.setTextColor(getColor(R.color.Danger))
                    } else {
                      balance.setTextColor(getColor(R.color.Success))
                      payIn.setTextColor(getColor(R.color.Success))
                    }

                    balanceRaw = balanceRawResult
                    balance.text = "${Coin.decimalToCoin(balanceRawResult).toPlainString()} DOGE"
                    payIn.text = "${Coin.decimalToCoin(payInToSend).toPlainString()} DOGE"
                  }
                }
                handler.getInt("code") == 408 -> {
                  GlobalScope.launch(Main) {
                    jobBot.cancel(CancellationException("Insufficient Funds"))
                  }
                }
                else -> {
                  GlobalScope.launch(Main) {
                    Toast.makeText(applicationContext, handler.getString("data"), Toast.LENGTH_SHORT).show()
                    delay(30000)
                  }
                }
              }
            }, {
              val error = HandleError(it).result()
              when {
                error.getInt("code") == 408 -> {
                  GlobalScope.launch(Main) {
                    jobBot.cancel(CancellationException("Insufficient Funds"))
                  }
                }
                else -> {
                  GlobalScope.launch(Main) {
                    Toast.makeText(applicationContext, error.getString("message"), Toast.LENGTH_SHORT).show()
                    delay(30000)
                  }
                }
              }
            })
          } catch (e: Exception) {
            delay(60000)
          }
        }
      }

      GlobalScope.launch(Main) {
        start.isEnabled = true
        stop.isEnabled = false
      }
    }
  }

  private fun addChartData(balance: Float) {
    lineDataSet = chart.data.getDataSetByIndex(0) as LineDataSet
    if (indexChart > 0.0010F) {
      lineData.removeEntry(indexChart - 0.0011F, 0)
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