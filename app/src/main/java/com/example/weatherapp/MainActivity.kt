package com.example.weatherapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import org.json.JSONObject
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE = 1
    private var permissionsList: ArrayList<String> = arrayListOf()

    private var fusedLocation: FusedLocationProviderClient? = null
    lateinit var locationCallback: LocationCallback
    private var locationManager: LocationManager? = null
    private var gpsStatus: Boolean = false
    private val API_KEY = "4fbb1c651315dee8ecccfffa2c38a1a3"
    private var weather_url = ""

    private var iv_icon: ImageView? = null
    private var tv_temp: TextView? = null
    private var tv_feels_like: TextView? = null
    private var tv_cityName: TextView? = null
    private var tv_latitude: TextView? = null
    private var tv_lontitude: TextView? = null
    private var tv_altitude: TextView? = null
    private var tv_velocity: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
        checkGPS()

        iv_icon = findViewById(R.id.iv_icon)
        tv_temp = findViewById(R.id.tv_temp)
        tv_feels_like = findViewById(R.id.tv_feels_like)
        tv_cityName = findViewById(R.id.tv_city)
        tv_latitude = findViewById(R.id.tv_latitude)
        tv_lontitude = findViewById(R.id.tv_longtitude)
        tv_altitude = findViewById(R.id.tv_altitude)
        tv_velocity = findViewById(R.id.tv_velocity)

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        createLocationRequest()
        getCurrentLocation()

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                for(loc in p0.locations) {
                    //Update UI
                    tv_latitude?.text = "Latitude: " + loc.latitude.toString()
                    tv_lontitude?.text = "Longtitude: " + loc.longitude.toString()
                    tv_altitude?.text = "Altitude: " + loc.altitude.toString()
                    tv_velocity?.text = "Speed: " + loc.speed.toString()
                }
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE) {
            if(grantResults.isNotEmpty()) {
                for(element in grantResults) {
                    if(element == PackageManager.PERMISSION_GRANTED) {
                        Log.i("Perm", "Permissions granted!")
                    }
                }
            }
        }
    }

    fun checkPermissions() {
        val coarseLoc = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fineLoc = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        if(coarseLoc != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if(fineLoc != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if(!permissionsList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsList.toArray(arrayOf(permissionsList.size.toString())), REQUEST_CODE)
        }
    }

    fun checkGPS() {
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        Log.i("alertGPS","${gpsStatus}")
        if(!gpsStatus) {
            val alertGPS = AlertDialog.Builder(this)
            alertGPS.apply {
                setTitle("Please enable the GPS. Otherwise the app won't work.")
                setPositiveButton("Agree", { dialog, id ->
                    Log.i("alertGPS", "User have promised turn GPS on.")
                })
                setNegativeButton("Disagree", { dialog, id ->
                    Log.i("alertGPS", "User cancelled dialog")
                })
            }
            alertGPS.create()
            alertGPS.show()
        }
    }

    fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        return locationRequest
    }

    fun getCurrentLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions()
        } else {
            fusedLocation?.lastLocation?.addOnSuccessListener { location: Location? ->
                weather_url = "https://api.openweathermap.org/data/2.5/weather?lat=${location?.latitude}&lon=${location?.longitude}&appid=${API_KEY}"
                startRequest(weather_url)
                Log.i("LastLocation" ,"Latitude = ${location?.latitude}, Longtitude = ${location?.longitude}")
            }
        }
    }

    fun startRequest(url: String) {
        val request = Volley.newRequestQueue(this)

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                val json = JSONObject(response)
                val jmain = json.getJSONObject("main")
                val jsys = json.getJSONObject("sys")
                tv_temp?.text = jmain.getDouble("temp").toString()
                tv_feels_like?.text = "Feels like " + jmain.getDouble("feels_like").toString()
                tv_cityName?.text = jsys.getString("country") + ", " + json.getString("name")
                Log.i("json", "json = ${json}")
            },
            Response.ErrorListener { Log.i("Response = ", "That don't work!") },
        )
        request.add(stringRequest)
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions()
        } else {
            fusedLocation?.requestLocationUpdates(createLocationRequest(), locationCallback, Looper.getMainLooper())
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocation?.removeLocationUpdates(locationCallback)
    }
}