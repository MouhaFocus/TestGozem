package com.testgozem.test


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.testgozem.R
import com.testgozem.test.models.Content
import com.testgozem.test.models.Section
import com.testgozem.test.models.SectionItem
import de.hdodenhof.circleimageview.CircleImageView
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException


class HomeFragment : Fragment() {
    private lateinit var latLng: LatLng
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var title:String
    private lateinit var pin:String
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var latLngLiveData:MutableLiveData<LatLng> = MutableLiveData()
    private lateinit var  socket: Socket
    private lateinit var map:GoogleMap
    //

    lateinit var  rootView :View
    lateinit var mapView:MapView

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(title)
        Glide.with(this)
            .asBitmap()
            .load(pin)
            .into(object : SimpleTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resource));
                    //googleMap.addMarker(markerOptions)
                    //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f))
                }
            })
        map.addMarker(markerOptions)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f))

        latLngLiveData.observe(this as LifecycleOwner){
            val marker = MarkerOptions()
                .position(it)
                .title(title)
            map.clear()
            map.addMarker(marker)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(it,15f))
        }
        getLocation()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_home, container, false)

        return rootView
    }
    fun getLocation(){
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnCompleteListener {
            val location = it.result
            if(location !=  null){
                latLngLiveData.postValue(LatLng(location.latitude,location.longitude))
            }
            else{
                Log.e("sssss", "No location found")
            }
        }
        locationListener = object : LocationListener {
            override fun onLocationChanged(p0: Location) {
                latLngLiveData.postValue(LatLng(p0.latitude,p0.longitude))
            }
        }
// Request location updates every 10 seconds
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0F, locationListener)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val container = rootView.findViewById<LinearLayout>(R.id.container)
        val data = Section()
        val auth = Firebase.auth
        val user = auth.currentUser
        data.add(
            SectionItem(
                content = Content(email = "${user?.email}",image = "https://mir-s3-cdn-cf.behance.net/project_modules/disp/ce54bf11889067.562541ef7cde4.png",lat = null,lng = null,name = "${user?.displayName}",pin = null, source = null,title = null,value = null),
                type = "profile")
        )
        data.add(
            SectionItem(
                content = Content(email = null,image = null,lat = 14.693425,lng = -17.447938,name = null,pin = "", source = null,title = "Location",value = null),
                type = "map")
        )
        data.add(
            SectionItem(
                content = Content(email = null,image = null,lat = null,lng = null,name = null,pin = null, source = "wss://demo.piesocket.com/v3/channel_123?api_key=VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV&notify_self",title = "Information",value = "Loading..."),
                type = "data")
        )


        for (section in data) {
            val view = inflateLayout(section.type!!, container)
            populateLayout(view, section)
            container.addView(view)
        }

        mapView = rootView.findViewById<MapView>(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(callback)
    }

    private fun inflateLayout(type: String, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        return when (type) {
            "profile" -> inflater.inflate(R.layout.profile, parent, false)
            "map" -> inflater.inflate(R.layout.map, parent, false)
            "data" -> inflater.inflate(R.layout.data, parent, false)
            else -> throw IllegalArgumentException("Unknown section type: $type")
        }
    }

    private fun populateLayout(view: View, data: SectionItem) {

        when (data.type) {
            "profile" -> {
                data.content?.let {
                    val name = view.findViewById<TextView>(R.id.name)
                    name.text = it.name
                    val mail = view.findViewById<TextView>(R.id.mail)
                    mail.text = it.email
                    val profilImage = view.findViewById<CircleImageView>(R.id.profile_image)
                    Glide.with(requireContext())
                        .load(it.image)
                        .into(profilImage);
                }


            }
            "map" -> {
                data.content?.let {
                    try {
                        latLng = LatLng(it.lat!!, it.lng!!)
                        title = it.title!!
                        pin = it.pin!!
                    } catch (e: NullPointerException) {
                    }

                }

            }
            "data" -> {
                data.content?.let {
                    val title = view.findViewById<TextView>(R.id.tilte)
                    title.text = it.title
                    val information = view.findViewById<TextView>(R.id.information)
                    information.text = it.value
                    try {

                        socket = IO.socket(it.source)
                        socket.connect()
                        socket
                            .on(Socket.EVENT_CONNECT, Emitter.Listener {
                            socket.send("Hello, World!")
                            information.text = "socket connexion etablished"
                            Log.v("socket connexion ","etablished")
                        })
                            .on(Socket.EVENT_CONNECT_ERROR, Emitter.Listener {
                            information.text = "socket connexion failed"
                            Log.v("socket connexion ","fails")
                        })
                            .on("message") { args ->
                                val message = args[0] as String
                            information.text = "socket connexion received\n $message"
                            Log.v("socket connexion ","received")
                        }

                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                        information.text = "socket connexion fails2"
                        println("vladmir"+e.message)
                        Log.v("socket connexion ","fails2")
                    }
                }
            }
            else -> throw IllegalArgumentException("Unknown section type: $data.type")
        }

    }

     override fun onResume() {
         super.onResume()
         mapView.onResume()
         try {
             socket.open()
         }
         catch (e:Exception){
         }
    }

     override fun onPause() {
         super.onPause()
         mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        socket.close()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}

