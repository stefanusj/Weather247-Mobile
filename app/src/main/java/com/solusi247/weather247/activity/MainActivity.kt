package com.solusi247.weather247.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.solusi247.weather247.R
import com.solusi247.weather247.adapter.MainAdapter
import com.solusi247.weather247.listener.AttrWeatherListener
import com.solusi247.weather247.listener.LastWeatherListener
import com.solusi247.weather247.module.model.ResponseModel
import com.solusi247.weather247.module.presenter.MainPresenter
import com.solusi247.weather247.module.view.MainView
import com.solusi247.weather247.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.no_connection.*
import kotlinx.android.synthetic.main.progress_loading.*


class MainActivity : AppCompatActivity(), MainView, LastWeatherListener, AttrWeatherListener {

    /************************************************************************************/
    /*********************   Override Function AppCompatActivity   **********************/
    /************************************************************************************/

    lateinit var presenter: MainPresenter
    lateinit var mqttHelper: MqttHelper

    lateinit var fadeIn: Animation
    lateinit var moveUp: Animation
    lateinit var rotate360: Animation


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize activity presenter
        presenter = MainPresenter(this)

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        moveUp = AnimationUtils.loadAnimation(this, R.anim.move_up_in)
        rotate360 = AnimationUtils.loadAnimation(this, R.anim.rotate_360)

        /****************Set layout******************/
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val params = clWeatherNow.layoutParams
        params.height = displayMetrics.heightPixels - resources.getInteger(R.integer.arch_height).convertToPixel()
        clWeatherNow.layoutParams = params

        val linearLayoutManager = LinearLayoutManager(this)

        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        rvLastWeather.layoutManager = linearLayoutManager
        rvLastWeather.addItemDecoration(DividerItemDecoration(this, linearLayoutManager.orientation))

        chartMQTT.apply {
            legend.isEnabled = false
            isScaleYEnabled = false
            animateXY(1000, 1000)
            description.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            setVisibleXRangeMaximum(10f)
            data = LineData()
        }
        /**************End of set layout***************/

        ivRefresh.setOnClickListener { presenter.refreshWeather() }

        noConnection.setOnClickListener { _ ->
            presenter.loadWeather()
            noConnection.visibility = View.GONE
        }

        // Presenter load weather
        presenter.loadWeather()

        presenter.initGraph(chartMQTT)

    }

    override fun onDestroy() {
        presenter.interruptService()
        super.onDestroy()
    }
    /**********************End of Override Function AppCompatActivity*********************/


    /*************************************************************************************/
    /**************************************   View   *************************************/
    /*************************************************************************************/

    override fun showLoading() = ivLoading.startCustomLoading()

    override fun hideLoading() = ivLoading.endCustomLoading()

    override fun showError() {
        noConnection.visibility = View.VISIBLE
    }

    override fun startRefresh() = ivRefresh.startAnimation(rotate360)

    override fun stopRefresh() = ivRefresh.clearAnimation()

    override fun playAnimationWeatherToday() {
        flRefresh.startAnimation(fadeIn)
        ivIconWeather.startAnimation(fadeIn)
        tvDescription.startAnimation(fadeIn)
        tvLocation.startAnimation(fadeIn)
        tvDate.startAnimation(fadeIn)
        tvTemperature.startAnimation(fadeIn)
        tvTemperature.visibility = View.VISIBLE
        tvPressure.startAnimation(fadeIn)
        tvPressure.visibility = View.VISIBLE
        tvHumidity.startAnimation(fadeIn)
        tvHumidity.visibility = View.VISIBLE
        divider.visibility = View.VISIBLE
        tvRealtime.startAnimation(fadeIn)
        tvRealtime.visibility = View.VISIBLE
        chartMQTT.startAnimation(fadeIn)
        chartMQTT.visibility = View.VISIBLE
        llWeatherInfo.startAnimation(moveUp)
        llWeatherInfo.visibility = View.VISIBLE
    }

    override fun onWeatherToday(dataWeather: ResponseModel.DataWeather) {
        ivIconWeather.setImageResource(dataWeather.weather.convertToLargeWeatherIcon())
        tvDescription.text = dataWeather.weather
        tvLocation.text = getString(R.string.default_location)
        tvDate.text = String.format(getString(R.string.last_update), dataWeather.date.changeFormatDate(), dataWeather.time)
        tvTemperature.textAnimationIncrement(dataWeather.temperature, 1000, "\u2103")
        tvPressure.textAnimationIncrement(dataWeather.pressure, 1000, "hPa")
        tvHumidity.textAnimationIncrement(dataWeather.humidity, 1000, "%")
        tvTemperature.setOnLongClickListener { onTemperatureClicked(); true }
        tvPressure.setOnLongClickListener { onPressureClicked(); true }
        tvHumidity.setOnLongClickListener { onHumidityClicked(); true }
    }

    override fun onLastWeather(dataWeathers: List<ResponseModel.DataWeather>) {
        lastWeather.text = getString(R.string.past_weather)
        rvLastWeather.adapter = MainAdapter(dataWeathers, this)
    }

    override fun onMQTTUpdated(entry: Entry) {
        val data = chartMQTT.data
        data.addEntry(entry, 0)
        data.notifyDataChanged()
        chartMQTT.notifyDataSetChanged()
        chartMQTT.moveViewTo((data.getDataSetByIndex(0).entryCount - 1).toFloat(), data.yMax, YAxis.AxisDependency.LEFT)
    }

    /***************************************End of View**********************************/


    /*************************************************************************************/
    /***************************   LastWeatherFragment Listener   ************************/
    /*************************************************************************************/

    override fun goToDetail(day: String, date: String) {
        val intentToDetail = Intent(this, DetailActivity::class.java)
        intentToDetail.putExtra(Constant.SHARED_DAY, day)
        intentToDetail.putExtra(Constant.SHARED_DATE, date)
        startActivity(intentToDetail)
    }
    /******************************End of LastWeatherFragment Listener*********************/


    /***************************************************************************************/
    /**************************   Attr WeatherFragment Listener   **************************/
    /***************************************************************************************/

    override fun onTemperatureClicked() {
        val message = String.format(getString(R.string.temperature_now), tvTemperature.text)
        Message.showToast(this, message, Message.Type.INFORMATION)
    }

    override fun onPressureClicked() {
        val message = String.format(getString(R.string.pressure_now), tvPressure.text)
        Message.showToast(this, message, Message.Type.INFORMATION)
    }

    override fun onHumidityClicked() {
        val message = String.format(getString(R.string.humidity_now), tvHumidity.text)
        Message.showToast(this, message, Message.Type.INFORMATION)
    }
    /***************************End of Attr WeatherFragment Listener*************************/
}
