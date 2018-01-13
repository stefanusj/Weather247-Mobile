package com.solusi247.weather247.module.model

object ResponseModel {

    data class Weather(
            val error: Boolean,
            val message: String,
            val data: List<DataWeather>
    )

    data class DetailWeather(
            val error: Boolean,
            val message: String,
            val data: List<DataDetailWeather>
    )

    data class DataWeather(
            val id: Int,
            val weather: String,
            val day: String,
            val date: String,
            var temperature: String,
            var pressure: String,
            var humidity: String
    )

    data class DataDetailWeather(
            val id: Int,
            val weather: String,
            val day: String,
            val date: String,
            val time: String,
            var temperature: String,
            var pressure: String,
            var humidity: String
    )
}