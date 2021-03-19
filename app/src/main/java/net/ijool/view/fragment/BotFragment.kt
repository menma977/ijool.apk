package net.ijool.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import net.ijool.R
import net.ijool.view.activity.NavigationActivity
import net.ijool.view.activity.bot.MiningActivity
import net.ijool.view.activity.bot.MoseeActivity
import net.ijool.view.activity.bot.NinkuActivity
import net.ijool.view.activity.bot.TsunamiActivity

class BotFragment : Fragment() {
  private lateinit var move: Intent
  private lateinit var parentActivity: NavigationActivity
  private lateinit var miningButton: Button
  private lateinit var ninkuButton: Button
  private lateinit var moseeButton: Button
  private lateinit var tsunamiButton: Button

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_bot, container, false)

    parentActivity = activity as NavigationActivity

    miningButton = view.findViewById(R.id.buttonMining)
    moseeButton = view.findViewById(R.id.buttonMosee)
    ninkuButton = view.findViewById(R.id.buttonNinku)
    tsunamiButton = view.findViewById(R.id.buttonTsunami)

    miningButton.setOnClickListener {
      move = Intent(parentActivity, MiningActivity::class.java)
      startActivity(move)
    }

    moseeButton.setOnClickListener {
      move = Intent(parentActivity, MoseeActivity::class.java)
      startActivity(move)
    }

    ninkuButton.setOnClickListener {
      move = Intent(parentActivity, NinkuActivity::class.java)
      startActivity(move)
    }

    tsunamiButton.setOnClickListener {
      move = Intent(parentActivity, TsunamiActivity::class.java)
      startActivity(move)
    }

    return view
  }
}