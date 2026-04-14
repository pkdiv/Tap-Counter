package com.pkdiv.tapcounter

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.pkdiv.tapcounter.databinding.ActivityMainBinding
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var count = 0
    private var isCountingUp = true
    private val gravity = FloatArray(3)
    private var lastImpulseTimestamp = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        count = 0
        renderCount()
        renderSensorStatus()

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isCountingUp = checkedId == R.id.countUpButton
            }
        }

        binding.applyButton.setOnClickListener {
            val inputText = binding.countInput.text?.toString()
            if (!inputText.isNullOrEmpty()) {
                count = inputText.toIntOrNull() ?: 0
                renderCount()
                binding.countInput.text?.clear()
                binding.countInput.clearFocus()
                hideKeyboard()
            }
        }

        binding.resetButton.setOnClickListener {
            count = 0
            renderCount()
        }

        binding.addButton.setOnClickListener {
            updateCount(1)
        }

        binding.subtractButton.setOnClickListener {
            updateCount(-1)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.countInput.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        renderSensorStatus()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val alpha = 0.8f
        for (index in 0..2) {
            gravity[index] = alpha * gravity[index] + (1 - alpha) * event.values[index]
        }

        val linearX = event.values[0] - gravity[0]
        val linearY = event.values[1] - gravity[1]
        val linearZ = event.values[2] - gravity[2]
        val magnitude = sqrt(
            linearX * linearX + linearY * linearY + linearZ * linearZ
        )

        val now = System.currentTimeMillis()
        val tapThreshold = 5f
        val zThreshold = 4f
        val doubleTapWindow = 500L
        val debounceTime = 150L

        // Detect a sharp impulse (likely a tap)
        if (magnitude > tapThreshold && abs(linearZ) > zThreshold) {
            val timeDiff = now - lastImpulseTimestamp
            
            if (timeDiff in debounceTime..doubleTapWindow) {
                // Successful double tap!
                if (isCountingUp) {
                    updateCount(1)
                } else {
                    updateCount(-1)
                }
                lastImpulseTimestamp = 0L // Reset to avoid triple-tap triggering two counts
            } else if (timeDiff > doubleTapWindow) {
                // Either the first tap of a new sequence or too much time has passed
                lastImpulseTimestamp = now
            }
            // If timeDiff < debounceTime, we ignore it as noise from the same physical impact
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun updateCount(delta: Int) {
        val newCount = count + delta
        count = if (newCount < 0) 0 else newCount
        renderCount()
        if (count == 0) {
            vibrate()
        }
    }

    private fun renderCount() {
        binding.countValue.text = count.toString()
    }

    private fun renderSensorStatus() {
        binding.sensorStatus.text = if (accelerometer == null) {
            getString(R.string.sensor_missing)
        } else {
            getString(R.string.sensor_ready)
        }
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
