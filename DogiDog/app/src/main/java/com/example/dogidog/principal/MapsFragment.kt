package com.example.dogidog.principal

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.dogidog.R
import com.example.dogidog.adapters.ValoracionAdapter
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.dataModels.Recorrido
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.dataModels.Valoracion
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime

class MapsFragment : Fragment() {
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var googleMapGlobal: GoogleMap? = null
    private var myLocationMarker: Marker? = null
    private var totalDistance = 0.0
    private var lastLocation: Location? = null
    private var selectedPet: String? = null
    private lateinit var fabEndWalk: FloatingActionButton
    var ubicacionInicial: Location? = null
    var distanciaRecorrida = 0.0f // Variable para almacenar la distancia recorrida en metros
    private lateinit var usuario: Usuario
    private lateinit var apiService: ApiService
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var idRecorridoActual: Int? = null
    private var mascotaMapa: Mascota? = null
    private var tiempoInicioPaseo : Long = 0
    private var valoracionUsuario : Int = 0




    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 3000
        fastestInterval = 2000
        priority = Priority.PRIORITY_HIGH_ACCURACY
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        googleMapGlobal = googleMap

        // Actualizar el TextView con la distancia
        // Obtener la referencia del TextView
        val distanciaTextView = view?.findViewById<TextView>(R.id.distanceText)



        // Estilo del mapa
        val styleJson = """[{"elementType":"geometry","stylers":[{}]},{"elementType":"labels.icon","stylers":[{"visibility":"off"}]},{"elementType":"labels.text.fill","stylers":[{"color":"#616161"}]},{"elementType":"labels.text.stroke","stylers":[{"color":"#ffffff"}]},{"featureType":"poi","elementType":"all","stylers":[{"visibility":"off"}]},{"featureType":"road","elementType":"geometry","stylers":[{}]},{"featureType":"transit","elementType":"all","stylers":[{"visibility":"off"}]},{"featureType":"water","elementType":"geometry","stylers":[{"color":"#b2d4ff"}]}]"""
        googleMap.setMapStyle(MapStyleOptions(styleJson))

        googleMap.uiSettings.apply {
            isZoomControlsEnabled = false
            isMyLocationButtonEnabled = false
            isTiltGesturesEnabled = true
            isRotateGesturesEnabled = true
        }

        val iconResId = when (selectedPet) {
            "shibainu" -> R.drawable.bordercollie
            else -> R.drawable.bordercollie
        }

        // Obtener la localización y actualizar el mapa
        obtenerUsuariosYRecorridosYMostrarEnMapa()
        val originalBitmap = BitmapFactory.decodeResource(resources, iconResId)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 80, 80, false)
        val customIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return@OnMapReadyCallback
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val latLng = LatLng(location.latitude, location.longitude)

                if (myLocationMarker == null) {
                    myLocationMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .icon(customIcon)
                            .anchor(0.5f, 0.5f)
                            .flat(true)
                            .rotation(location.bearing)
                            .title(null) // Sin título, evita InfoWindow
                    )
                } else {
                    myLocationMarker!!.position = latLng
                    myLocationMarker!!.rotation = location.bearing
                }

                val cameraPosition = CameraPosition.Builder()
                    .target(latLng)
                    .zoom(18f)
                    .tilt(30f)
                    .bearing(location.bearing)
                    .build()

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                // Actualizar coordenadas en el servidor
                actualizarCoordenadasUsuario(location.latitude, location.longitude)

                // Calcular la distancia
                lastLocation?.let {
                    val distance = it.distanceTo(location)
                    totalDistance += distance
                }

                lastLocation = location

                distanciaTextView?.text = "Distancia recorrida: ${"%.2f".format(totalDistance)} m"
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        googleMap.setOnMarkerClickListener { marker ->
                if (marker == myLocationMarker) {
                    // No hacer nada ni mostrar ventana
                    true // ← esto evita que se abra InfoWindow
                } else {

                    val recorrido : Recorrido = marker.tag as Recorrido

                    view?.findViewById<LinearLayout>(R.id.floatActionsContainer)?.visibility = View.VISIBLE

                    val fabAddReview = view?.findViewById<FloatingActionButton>(R.id.fabAddReview)
                    fabAddReview?.setOnClickListener {
                        val dialogView = layoutInflater.inflate(R.layout.dialog_add_review, null)
                        val etDescripcion = dialogView.findViewById<EditText>(R.id.etDescripcionValoracion)
                        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)

                        AlertDialog.Builder(requireContext())
                            .setTitle("Añadir valoración")
                            .setView(dialogView)
                            .setPositiveButton("Enviar") { _, _ ->
                                val descripcion = etDescripcion.text.toString()
                                val estrellas = ratingBar.rating.toInt()

                                // Aquí deberías hacer la llamada a tu API para enviar la valoración
                                enviarValoracion(obtenerUsuarioLocal()!!, recorrido.mascota.usuario, estrellas, descripcion)
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }

                    view?.findViewById<FloatingActionButton>(R.id.fabSeeReviews)?.setOnClickListener {
                        val usuarioId = recorrido.mascota.usuario?.id ?: return@setOnClickListener

                        obtenerValoracionesDelUsuario(usuarioId) { valoraciones ->
                            if (valoraciones.isEmpty()) {
                                Toast.makeText(requireContext(), "Este usuario no tiene valoraciones aún.", Toast.LENGTH_SHORT).show()
                                return@obtenerValoracionesDelUsuario
                            }

                            // Inflar el layout del dialog que contiene una ListView
                            val dialogView = LayoutInflater.from(requireContext()).inflate(android.R.layout.list_content, null)
                            val listView = ListView(requireContext())
                            listView.adapter = ValoracionAdapter(requireContext(), valoraciones)

                            val builder = AlertDialog.Builder(requireContext())
                            builder.setTitle("Opiniones sobre ${recorrido.mascota.usuario.usuario}")
                            builder.setView(listView)
                            builder.setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
                            builder.show()
                        }
                    }
                    false
                }
        }

        googleMap.setOnMapClickListener {
            view?.findViewById<LinearLayout>(R.id.floatActionsContainer)?.visibility = View.GONE
        }
    }


    private fun enviarValoracion(usuarioActual: Usuario, usuarioValorado: Usuario, estrellas: Int, descripcion: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        val valoracion = Valoracion(
            usuario = usuarioActual,
            valorado = usuarioValorado,
            puntuacion = estrellas,
            comentario = descripcion,
            fechaValoracion = LocalDateTime.now().toString()
        )

        val call = service.enviarValoracion(valoracion)
        call.enqueue(object : Callback<Valoracion> {
            override fun onResponse(call: Call<Valoracion>, response: Response<Valoracion>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "¡Valoración enviada!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("API", "Error al enviar valoración: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Valoracion>, t: Throwable) {
                Log.e("API", "Fallo en la conexión: ${t.message}")
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                enableUserLocation()
            }
        }
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMapGlobal?.isMyLocationEnabled = true
            moveCameraToUserLocation()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usuario = obtenerUsuarioLocal()!!;
        fabEndWalk = view.findViewById(R.id.fabEndWalk)
        fabEndWalk.setOnClickListener {
            finalizarPaseo()
        }

        // El runnable que se ejecutará cada 5 minutos
        runnable = object : Runnable {
            override fun run() {
                obtenerUsuariosYRecorridosYMostrarEnMapa() // Llamamos a la función para obtener los usuarios y sus ubicaciones
                handler.postDelayed(this, 300000) // 300000ms = 5 minutos
            }
        }

        // Iniciar el primer ciclo de actualizaciones
        handler.post(runnable)

        // Configuración de Retrofit y la API
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // URL base
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        if (selectedPet == null) {
            // Hacer la llamada a obtenerMascotas antes de mostrar el diálogo
            val call = apiService.obtenerMascotas(obtenerUsuarioLocal()!!.id) // Asegúrate que tienes el usuarioId disponible

            call.enqueue(object : Callback<List<Mascota>> {
                override fun onResponse(call: Call<List<Mascota>>, response: Response<List<Mascota>>) {
                    if (response.isSuccessful) {
                        val mascotas = response.body() ?: emptyList()

                        if (mascotas.isNotEmpty()) {
                            // Mostrar el diálogo con las mascotas
                            PetSelectionDialogFragment(mascotas) { mascotaSeleccionada ->
                                mascotaMapa = mascotaSeleccionada
                                selectedPet = mascotaSeleccionada.nombre // o el campo que quieras usar
                                initMap(view)

                                // Obtener fecha actual en formato ISO-8601 (yyyy-MM-dd)
                                val fechaActual = LocalDate.now().toString()

// Crear objeto Recorrido con mascota seleccionada y fecha
                                val nuevoRecorrido = Recorrido(
                                    mascota = mascotaSeleccionada,
                                    fecha = fechaActual,
                                    distancia = 0,
                                    duracion = 0
                                )

// Hacer la llamada a la API para guardar el recorrido
                                apiService.guardarRecorrido(nuevoRecorrido).enqueue(object : Callback<Int> {
                                    override fun onResponse(call: Call<Int>, response: Response<Int>) {
                                        if (response.isSuccessful) {
                                            idRecorridoActual = response.body()
                                            Toast.makeText(context, "Recorrido iniciado (ID: $idRecorridoActual)", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Error al iniciar recorrido", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onFailure(call: Call<Int>, t: Throwable) {
                                        Toast.makeText(context, "Fallo en la conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                                iniciarPaseo()
                                tiempoInicioPaseo = System.currentTimeMillis()
                                iniciarActualizacionPeriodica()
                            }.show(childFragmentManager, "PetSelectionDialog")
                        } else {
                            Toast.makeText(context, "No tienes mascotas aún", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Error al obtener mascotas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Mascota>>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            initMap(view)
            iniciarPaseo()
            // Iniciar la actualización periódica
            iniciarActualizacionPeriodica()
        }
    }

    private fun initMap(view: View) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun moveCameraToUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    val cameraPosition = CameraPosition.Builder()
                        .target(userLatLng)
                        .zoom(17f)
                        .tilt(45f)
                        .build()
                    googleMapGlobal?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        handler.removeCallbacks(runnable)
    }

    override fun onPause() {
        super.onPause()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        handler.removeCallbacks(runnable)
    }


    // Método que inicia el paseo y guarda la ubicación inicial
    @SuppressLint("MissingPermission")
    fun iniciarPaseo() {
        // Hacer visible el botón de finalizar paseo
        fabEndWalk.visibility = View.VISIBLE
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        ubicacionInicial = location
        distanciaRecorrida = 0.0f

        // Asegúrate de que la ubicación inicial sea válida
        if (location != null) {
            lastLocation = location // Guarda la última ubicación si está disponible
        }
    }

    // Método que se ejecuta cuando se pulsa el botón de "finalizar paseo"
    @SuppressLint("MissingPermission")
    fun finalizarPaseo() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        // Verifica si tenemos la ubicación inicial y la final
        if (ubicacionInicial != null && location != null) {
            // Calcula la distancia entre la ubicación inicial y la final
            val result = FloatArray(1)
            Location.distanceBetween(
                ubicacionInicial!!.latitude,
                ubicacionInicial!!.longitude,
                location.latitude,
                location.longitude,
                result
            )

            distanciaRecorrida = result[0] // Almacena la distancia recorrida

            // Calcular la duración del paseo
            val duracionRecorrido = (System.currentTimeMillis() - tiempoInicioPaseo) / 1000 // Duración en segundos

            // Mostrar el diálogo con la distancia recorrida y duración
            mostrarDialogoFinalizacion(distanciaRecorrida)

            // Crear un objeto Recorrido para actualizar
            val recorridoActualizado = Recorrido(
                mascota = mascotaMapa!!, // Asegúrate de usar el objeto completo de la mascota
                fecha = LocalDate.now().toString(),
                distancia = distanciaRecorrida.toInt(),
                duracion = duracionRecorrido.toInt()
            )

            // Llamar a la API para actualizar el recorrido
            if (idRecorridoActual != null) {
                apiService.actualizarRecorrido(idRecorridoActual!!, recorridoActualizado).enqueue(object : Callback<Recorrido> {
                    override fun onResponse(call: Call<Recorrido>, response: Response<Recorrido>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Paseo finalizado y recorrido actualizado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Error al actualizar recorrido", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Recorrido>, t: Throwable) {
                        Toast.makeText(requireContext(), "Fallo en la conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(requireContext(), "No se encontró el ID del recorrido", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Si no se ha podido obtener la ubicación final o inicial
            Toast.makeText(requireContext(), "No se pudo calcular la distancia", Toast.LENGTH_SHORT).show()
        }
    }


    fun mostrarDialogoFinalizacion(distancia: Float) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Paseo Finalizado")
            .setMessage("Has recorrido ${distancia.toInt()} metros.")
            .setPositiveButton("Aceptar") { dialog, _ ->
                // Actualizar las coordenadas a null
                ubicacionInicial = null
                lastLocation = null
                distanciaRecorrida = 0.0f

                // Limpiar cualquier coordenada almacenada si es necesario
                limpiarCoordenadasUsuario()



                dialog.dismiss() // Cierra el diálogo

                // Recargar el fragmento para reiniciar todo como si no hubiera empezado el paseo
                recargarFragmento()
            }
            .create()

        dialog.show()
    }

    // Método para mostrar el AlertDialog con la distancia
    fun recargarFragmento() {
        // Obtener el fragment manager y la transacción
        val fragmentManager = parentFragmentManager

        // Comenzar una transacción de fragmentos
        val transaction = fragmentManager.beginTransaction()

        // Eliminar el fragmento actual
        transaction.remove(this)

        // Commit para realizar el cambio
        transaction.commit()

        // Ahora volvemos a agregar el fragmento desde cero
        fragmentManager.beginTransaction()
            .replace(R.id.containerView, MapsFragment())
            .commit()
    }
    private fun actualizarCoordenadasUsuario(latitud: Double, longitud: Double) {
        val usuarioId = obtenerUsuarioLocal()?.id ?: return

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Asegúrate de que esta URL es la correcta
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        service.actualizarCoordenadas(usuarioId, latitud, longitud).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (isAdded && context != null) {
                    if (response.isSuccessful) {
                        //Toast.makeText(requireContext(), "Coordenadas actualizadas", Toast.LENGTH_SHORT).show()
                    } else {
                       // Toast.makeText(requireContext(), "Error al actualizar coordenadas", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun limpiarCoordenadasUsuario() {
        val usuarioId = obtenerUsuarioLocal()?.id ?: return

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        service.limpiarCoordenadas(usuarioId).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful && isAdded) {
                    Toast.makeText(requireContext(), "Coordenadas limpiadas", Toast.LENGTH_SHORT).show()
                    reiniciarFragmento() // <- Aquí vuelves a cargar el estado inicial del fragmento
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun reiniciarFragmento() {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.detach(this)
        fragmentTransaction.attach(this)
        fragmentTransaction.commit()
    }


    private fun obtenerUsuarioLocal(): Usuario? {
        val prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val id = prefs.getInt("usuario_id", -1)
        val usuario = prefs.getString("usuario", null)
        val email = prefs.getString("usuario_email", null)
        val password = prefs.getString("usuario_password", null)
        val contadorPreguntas = prefs.getInt("usuario_preguntas", 0)
        val latitud = prefs.getFloat("usuario_latitud", Float.MIN_VALUE)
        val longitud = prefs.getFloat("usuario_longitud", Float.MIN_VALUE)
        val valoracion = prefs.getInt("usuario_valoracion", 0)  // Nuevo campo de valoración

        Log.d("SharedPreferences", "Recuperando usuario: ID=$id, Usuario=$usuario, Email=$email, Preguntas=$contadorPreguntas, Valoración=$valoracion")

        return if (id != -1 && usuario != null && email != null && password != null) {
            val latitudDouble = if (latitud != Float.MIN_VALUE) latitud.toDouble() else null
            val longitudDouble = if (longitud != Float.MIN_VALUE) longitud.toDouble() else null

            Usuario(
                id = id,
                usuario = usuario,
                email = email,
                password = password,
                contadorPreguntas = contadorPreguntas,
                latitud = latitudDouble,
                longitud = longitudDouble,
                valoracion = valoracion  // Agregar la valoración al objeto Usuario
            )
        } else {
            Log.w("SharedPreferences", "No se encontró un usuario válido en SharedPreferences")
            null
        }
    }
    fun obtenerValoracionesDelUsuario(usuarioId: Int, callback: (List<Valoracion>) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/")  // URL de tu API
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.obtenerValoracionesDeUsuario(usuarioId).enqueue(object : Callback<List<Valoracion>> {
            override fun onResponse(call: Call<List<Valoracion>>, response: Response<List<Valoracion>>) {
                if (response.isSuccessful) {
                    val valoraciones = response.body() ?: emptyList()
                    Log.d("ValoracionesAPI", "Valoraciones obtenidas: $valoraciones")

                    if (valoraciones.isEmpty()) {
                        Log.d("ValoracionesAPI", "El usuario no tiene valoraciones.")
                    }

                    callback(valoraciones) // Llamamos al callback con las valoraciones obtenidas
                } else {
                    // Mostrar detalles sobre la respuesta si falla
                    Log.e("ValoracionesAPI", "Error en la respuesta: ${response.code()} - ${response.message()}")
                    Toast.makeText(requireContext(), "Error al obtener las valoraciones", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Valoracion>>, t: Throwable) {
                Log.e("ValoracionesAPI", "Fallo en la conexión: ${t.message}")
                Toast.makeText(requireContext(), "Error en la conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }




    private fun actualizarNumValoraciones(usuarioId: Int, numValoraciones: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("UsuarioPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Guardamos el número de valoraciones del usuario
        editor.putInt("numValoraciones_$usuarioId", numValoraciones)
        editor.apply() // No olvides llamar a apply() para guardar los cambios de forma asincrónica
    }

    private fun obtenerYActualizarUsuarios() {
        apiService.obtenerTodosLosUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful) {
                    val usuarios = response.body()
                    if (usuarios != null) {
                        actualizarPosicionesUsuarios(usuarios)
                    }
                } else {
                    Log.e("MapsFragment", "Error al obtener usuarios: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                Log.e("MapsFragment", "Error en la llamada a la API: ${t.message}")
            }
        })
    }

    private fun actualizarPosicionesUsuarios(usuarios: List<Usuario>) {
        // Limpiar los marcadores antes de agregar nuevos
        googleMapGlobal?.clear()

        for (usuario in usuarios) {
            if (usuario.latitud != null && usuario.longitud != null) {
                val latLng = LatLng(usuario.latitud, usuario.longitud)

                // Agregar un marcador para cada usuario
                googleMapGlobal?.addMarker(MarkerOptions()
                    .position(latLng)
                    .title(usuario.usuario)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )
            }
        }
    }

    private fun iniciarActualizacionPeriodica() {
        handler.post(runnable) // Empieza a actualizar usuarios cada 5 minutos
    }

    fun obtenerUsuariosYRecorridosYMostrarEnMapa() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        // Limpiar marcadores existentes
        googleMapGlobal?.clear()

        // Obtener recorridos activos (ya incluyen usuario y mascota)
        service.obtenerRecorridosActivos().enqueue(object : Callback<List<Recorrido>> {
            override fun onResponse(call: Call<List<Recorrido>>, response: Response<List<Recorrido>>) {
                if (response.isSuccessful) {
                    val recorridos = response.body() ?: return
                    Log.d("Recorridos", "Recorridos obtenidos: $recorridos")

                    for (recorrido in recorridos) {
                        val usuario = recorrido.mascota?.usuario
                        val mascota = recorrido.mascota

                        if (usuario?.latitud != null && usuario.longitud != null && usuario.id != obtenerUsuarioLocal()!!.id) {
                            val latLng = LatLng(usuario.latitud, usuario.longitud)
                            val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.bordercollie)
                            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 80, 80, false)
                            val customIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

                            val marker = googleMapGlobal?.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(usuario.usuario)
                                    .icon(customIcon)
                            )

                            marker?.tag = recorrido
                        }
                    }

                    googleMapGlobal?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                        override fun getInfoWindow(marker: Marker): View? = null

                        override fun getInfoContents(marker: Marker): View {
                            val view = layoutInflater.inflate(R.layout.info_window, null)
                            val recorrido = marker.tag as? Recorrido ?: return view
                            val mascota = recorrido.mascota
                            val usuario = mascota?.usuario

                            view.findViewById<TextView>(R.id.tvUsername).text = "${usuario?.usuario} 🐾"
                            view.findViewById<TextView>(R.id.tvWalkingWith).text = "Paseando con ${mascota?.nombre}"
                            view.findViewById<ImageView>(R.id.ivPet).setImageResource(R.drawable.bordercollie)
                            view.findViewById<ImageView>(R.id.ivUserProfile).setImageResource(R.drawable.bordercollie)
                            view.findViewById<TextView>(R.id.tvPetName).text = mascota.nombre
                            view.findViewById<TextView>(R.id.tvPetBreed).text = mascota.raza.nombre
                            // Estrellas (puedes adaptar según valoraciones reales)



                            var valoracion: Int = usuario?.valoracion ?: 0


                            val stars = listOf(
                                view.findViewById<ImageView>(R.id.star1),
                                view.findViewById<ImageView>(R.id.star2),
                                view.findViewById<ImageView>(R.id.star3),
                                view.findViewById<ImageView>(R.id.star4),
                                view.findViewById<ImageView>(R.id.star5)
                            )

                            for (i in stars.indices) {
                                stars[i].setImageResource(
                                    if (i < valoracion) R.drawable.star_filled else R.drawable.star_empty
                                )
                            }

                            return view
                        }
                    })

                } else {
                    Toast.makeText(requireContext(), "Error al obtener recorridos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Recorrido>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



}

