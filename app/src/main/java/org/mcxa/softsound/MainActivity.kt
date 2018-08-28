package org.mcxa.softsound

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.view.View
import android.widget.SeekBar


class MainActivity : AppCompatActivity() {
    // handle binding to the player service
    private var playerService: PlayerService? = null

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            playerService = (service as PlayerService.PlayerBinder).getService()
            // update the FAB
            fab.visibility = if (playerService?.isPlaying() == true) View.VISIBLE else View.INVISIBLE
            playerService?.playerChangeListener = playerChangeListener
        }

    }

    private val playerChangeListener = {
        fab.visibility = if (playerService?.isPlaying() == true) View.VISIBLE else View.INVISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        play_rain.setOnClickListener { playerService?.toggleSound(PlayerService.Sound.RAIN) }
        play_water.setOnClickListener { playerService?.toggleSound(PlayerService.Sound.WATER) }
        play_storm.setOnClickListener { playerService?.toggleSound(PlayerService.Sound.THUNDER) }
        play_fire.setOnClickListener { playerService?.toggleSound(PlayerService.Sound.FIRE) }
        play_wind.setOnClickListener { playerService?.toggleSound(PlayerService.Sound.WIND) }

        rain_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.RAIN))
        water_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.WATER))
        storm_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.THUNDER))
        fire_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.FIRE))
        wind_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.WIND))

        fab.setOnClickListener {
            playerService?.stopPlaying()
            fab.visibility = View.INVISIBLE
        }
    }

    inner class VolumeChangeListener(val sound: PlayerService.Sound): SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            playerService?.setVolume(sound, (progress + 1) / 20f)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    override fun onStart() {
        super.onStart()
        val playerIntent = Intent(this, PlayerService::class.java)
        startService(playerIntent)
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        playerService?.stopForeground()
    }

    override fun onPause() {
        playerService?.startForeground()
        super.onPause()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel("softsound", name, importance)

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
