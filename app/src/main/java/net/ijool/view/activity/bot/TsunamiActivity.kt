package net.ijool.view.activity.bot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
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
import java.lang.Exception
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

class TsunamiActivity : AppCompatActivity() {
  private lateinit var loading: Loading
  private lateinit var chart: LineChart
  private lateinit var balanceRaw: BigDecimal
  private lateinit var balance: TextView
  private lateinit var start: Button
  private lateinit var stop: Button
  private lateinit var data: ArrayList<Entry>
  private lateinit var lineDataSet: LineDataSet
  private lateinit var arrayLineDataSet: ArrayList<ILineDataSet>
  private lateinit var lineData: LineData
  private var indexChart: Float = 0F
  private var isRunning: Boolean = false
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
      onBot()
    }

    stop.setOnClickListener {
      isRunning = false
      start.isEnabled = true
      stop.isEnabled = false
    }

    Timer().schedule(100) {
      balanceRaw = NavigationController().getBalance(applicationContext, true).toBigDecimal()
      runOnUiThread {
        setupChart()
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
      while (true) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 2000) {
          if (!isRunning) {
            break
          }
          time = System.currentTimeMillis()
          synchronized(trigger) {
            try {
              val post = BotController().tsunami(applicationContext, balanceRaw, seed)
              Log.i("bet", post.toString())
              if (post.getInt("code") < 400) {
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
              } else if (post.getInt("code") == 408) {
                isRunning = false
              } else {
                runOnUiThread {
                  balance.text = post.getString("message")
                }
              }
            } catch (e: Exception) {
              trigger.wait(60000)
            }
          }
        }
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
    lineDataSet = LineDataSet(data, "Tsunami")

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