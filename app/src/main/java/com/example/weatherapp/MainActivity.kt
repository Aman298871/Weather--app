package com.example.weatherapp

import WeatherApp
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

const val API_KEY = "037b2c34e48820b64bfdc2f628bb85d7"
const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
const val TAG = "WeatherApp"

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fetchWeatherData("Raipur")
        setupSearchView()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName, API_KEY, "metric")
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                if (response.isSuccessful) {
                    response.body()?.let { updateUI(it, cityName) }
                } else {
                    Log.e(TAG, "Response failed with code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Log.e(TAG, "API call failed: ${t.message}", t)
            }
        })
    }

    private fun updateUI(data: WeatherApp, cityName: String) {
        val temperature = data.main.temp.toString()
        val humidity = data.main.humidity
        val windSpeed = data.wind.speed
        val sunrise = data.sys.sunrise.toLong()
        val sunset = data.sys.sunset.toLong()
        val pressure = data.main.pressure
        val condition = data.weather.firstOrNull()?.main ?: "Unknown"
        val maxTemp = data.main.temp_max
        val minTemp = data.main.temp_min

        // Log temperatures for debugging
        Log.d(TAG, "City: $cityName, Temp Min: $minTemp, Temp Max: $maxTemp")
        Log.d(TAG, "Sunrise: $sunrise, Sunset: $sunset")

        // Update UI
        binding.temp.text = "$temperature °C"
        binding.weather.text = condition
        binding.maxtemp.text = "Max Temp: $maxTemp °C"
        binding.mintemp.text = "Min Temp: $minTemp °C"
        binding.humidity.text = "$humidity %"
        binding.windspeed.text = "$windSpeed m/s"
        binding.sunrise.text = time(sunrise)
        binding.sunset.text = time(sunset)
        binding.sea.text = "$pressure hPa"
        binding.condition.text = condition
        binding.day.text = dayName(System.currentTimeMillis())
        binding.date.text = date()
        binding.cityname.text = cityName

        changeImageAccordingToWeatherCondition(condition)
    }

    private fun changeImageAccordingToWeatherCondition(condition: String) {
        when (condition) {
            "Haze" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy", "Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault() // Ensure correct timezone
        return sdf.format(Date(timestamp * 1000))
    }

    private fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
