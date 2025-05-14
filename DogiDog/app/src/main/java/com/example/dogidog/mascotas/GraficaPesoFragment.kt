package com.example.dogidog.mascotas

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.dataModels.PesoMascota
import com.example.dogidog.databinding.FragmentGraficaPesoBinding
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GraficaPesoFragment : Fragment() {
    private lateinit var binding: FragmentGraficaPesoBinding
    private var listaPesos: List<PesoMascota> = emptyList()
    private var mascota: Mascota? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGraficaPesoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mascota = arguments?.getParcelable("mascota")

        // Spinner para seleccionar el periodo (Anual/Mensual)
        binding.spinnerPeriodo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val periodo = parent?.getItemAtPosition(position).toString()
                actualizarSpinnerAniosOMeses(periodo)
                actualizarGrafica()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Spinner para seleccionar el año o mes
        binding.spinnerAnio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                actualizarGrafica()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        obtenerPesosMascota(mascota!!.id)
    }

    private fun obtenerPesosMascota(mascotaId: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Reemplaza por tu IP real
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        api.obtenerPesosMascota(mascotaId).enqueue(object : Callback<List<PesoMascota>> {
            override fun onResponse(call: Call<List<PesoMascota>>, response: Response<List<PesoMascota>>) {
                if (response.isSuccessful) {
                    val pesos = response.body()
                    Log.d("GraficaPeso", "Pesos recibidos: $pesos")

                    if (!pesos.isNullOrEmpty()) {
                        listaPesos = pesos

                        val añosDisponibles = obtenerAniosDisponibles()

                        if (añosDisponibles.isNotEmpty()) {
                            val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, añosDisponibles)
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            binding.spinnerAnio.adapter = adapter
                        } else {
                            binding.spinnerAnio.adapter = null
                        }

                        binding.tvMensajeSinDatos.visibility = View.GONE
                        binding.graficoPeso.visibility = View.VISIBLE
                        binding.cardDetalles.visibility = View.VISIBLE

                        actualizarGrafica()
                    } else {
                        Log.d("GraficaPeso", "La lista de pesos está vacía.")
                        mostrarMensajeSinPesos()
                    }
                } else {
                    Log.e("GraficaPeso", "Error en respuesta: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<PesoMascota>>, t: Throwable) {
                Toast.makeText(context, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Función para actualizar los spinners según el periodo seleccionado (Anual o Mensual)
    private fun actualizarSpinnerAniosOMeses(periodo: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        when (periodo.lowercase()) {
            "anual" -> {
                val años = listaPesos.mapNotNull {
                    try {
                        val fecha = sdf.parse(it.fecha)
                        Calendar.getInstance().apply { time = fecha!! }.get(Calendar.YEAR)
                    } catch (e: Exception) {
                        null
                    }
                }.distinct().sorted()

                val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, años)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerAnio.adapter = adapter
            }

            "mensual" -> {
                val meses = listaPesos.mapNotNull {
                    try {
                        val fecha = sdf.parse(it.fecha)
                        Calendar.getInstance().apply { time = fecha!! }.get(Calendar.MONTH)
                    } catch (e: Exception) {
                        null
                    }
                }.distinct().sorted()

                val nombresMeses = meses.map {
                    SimpleDateFormat("MMMM", Locale("es", "ES")).format(Calendar.getInstance().apply { set(Calendar.MONTH, it) }.time).replaceFirstChar(Char::titlecase)
                }

                val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, nombresMeses)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerAnio.adapter = adapter
            }
        }
    }

    private fun actualizarGrafica() {
        if (listaPesos.isEmpty()) {
            mostrarMensajeSinPesos()
            return
        }

        val periodo = binding.spinnerPeriodo.selectedItem.toString().lowercase()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val pesosFiltrados = when (periodo) {
            "anual" -> {
                val anioSeleccionado = binding.spinnerAnio.selectedItem.toString().toIntOrNull() ?: return
                listaPesos.filter {
                    try {
                        val fecha = sdf.parse(it.fecha)
                        val calendar = Calendar.getInstance().apply { time = fecha }
                        calendar.get(Calendar.YEAR) == anioSeleccionado
                    } catch (e: Exception) {
                        false
                    }
                }
            }

            "mensual" -> {
                val mesNombre = binding.spinnerAnio.selectedItem?.toString()?.lowercase() ?: return
                val mesIndex = try {
                    SimpleDateFormat("MMMM", Locale("es", "ES")).parse(mesNombre)?.let {
                        Calendar.getInstance().apply { set(Calendar.MONTH, it.month) }.get(Calendar.MONTH)
                    }
                } catch (e: Exception) {
                    null
                } ?: return

                listaPesos.filter {
                    try {
                        val fecha = sdf.parse(it.fecha)
                        val calendar = Calendar.getInstance().apply { time = fecha }
                        calendar.get(Calendar.MONTH) == mesIndex
                    } catch (e: Exception) {
                        false
                    }
                }
            }

            else -> emptyList()
        }

        if (pesosFiltrados.isEmpty()) {
            mostrarMensajeSinPesos()
            return
        }

        // Caso de agrupación por **mes** para el modo **mensual**
        if (periodo == "mensual") {
            val pesosPorDia = mutableMapOf<Int, MutableList<Double>>()
            pesosFiltrados.forEach {
                val fecha = sdf.parse(it.fecha)
                val calendar = Calendar.getInstance().apply { time = fecha!! }
                val dia = calendar.get(Calendar.DAY_OF_MONTH)
                pesosPorDia.getOrPut(dia) { mutableListOf() }.add(it.peso.toDouble())
            }

            val puntos = pesosPorDia.entries.sortedBy { it.key }.map { entry ->
                val dia = entry.key
                val promedio = entry.value.average()
                DataPoint(dia.toDouble(), promedio)
            }

            // Etiquetas del eje X (días del mes)
            val dias = Array(31) { (it + 1).toString() }

            binding.graficoPeso.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
                override fun formatLabel(value: Double, isValueX: Boolean): String {
                    return if (isValueX && value.toInt() in 1..31) dias[value.toInt() - 1] else super.formatLabel(value, isValueX)
                }
            }

            binding.graficoPeso.gridLabelRenderer.numHorizontalLabels = 31
            binding.graficoPeso.gridLabelRenderer.horizontalAxisTitle = "Día"
            binding.graficoPeso.gridLabelRenderer.verticalAxisTitle = "Peso (kg)"

            // Ejes fijos para el modo mensual
            binding.graficoPeso.viewport.setYAxisBoundsManual(true)
            binding.graficoPeso.viewport.setMinY(0.0)
            binding.graficoPeso.viewport.setMaxY(30.0)

            binding.graficoPeso.viewport.setXAxisBoundsManual(true)
            binding.graficoPeso.viewport.setMinX(0.0)
            binding.graficoPeso.viewport.setMaxX(31.0)

            val series = LineGraphSeries(puntos.toTypedArray())
            binding.graficoPeso.removeAllSeries()
            binding.graficoPeso.addSeries(series)

            series.isDrawDataPoints = true
            series.dataPointsRadius = 10f
            series.thickness = 8

            // Datos inferiores
            val ultimoPeso = pesosFiltrados.last().peso.toDouble()
            val primeroPeso = pesosFiltrados.first().peso.toDouble()
            val promedio = pesosFiltrados.map { it.peso.toDouble() }.average()
            val diferencia = ultimoPeso - primeroPeso
            val diferenciaTexto = if (diferencia >= 0) "+%.2f".format(diferencia) else "%.2f".format(diferencia)

            binding.tvUltimoPeso.text = "Último Peso: %.2f kg".format(ultimoPeso)
            binding.tvDiferenciaPeso.text = "Diferencia: $diferenciaTexto kg"
            binding.tvPromedioPeso.text = "Peso Promedio: %.2f kg".format(promedio)

            binding.graficoPeso.visibility = View.VISIBLE
            binding.tvMensajeSinDatos.visibility = View.GONE

        } else if (periodo == "anual") { // Caso de agrupación por **mes** para el modo **anual**
            val pesosPorMes = mutableMapOf<Int, MutableList<Double>>()
            pesosFiltrados.forEach {
                val fecha = sdf.parse(it.fecha)
                val calendar = Calendar.getInstance().apply { time = fecha!! }
                val mes = calendar.get(Calendar.MONTH) // Obtenemos el mes (0 - 11)
                pesosPorMes.getOrPut(mes) { mutableListOf() }.add(it.peso.toDouble())
            }

            val puntos = pesosPorMes.entries.sortedBy { it.key }.map { entry ->
                val mes = entry.key + 1  // Ajustamos porque Calendar.get(Calendar.MONTH) devuelve un valor entre 0 y 11
                val promedio = entry.value.average()
                DataPoint(mes.toDouble(), promedio)
            }

            // Etiquetas del eje X (meses del año)
            val meses = arrayOf(
                "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
            )

            binding.graficoPeso.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
                override fun formatLabel(value: Double, isValueX: Boolean): String {
                    return if (isValueX && value.toInt() in 1..12) meses[value.toInt() - 1] else super.formatLabel(value, isValueX)
                }
            }

            binding.graficoPeso.gridLabelRenderer.numHorizontalLabels = 12
            binding.graficoPeso.gridLabelRenderer.horizontalAxisTitle = "Mes"
            binding.graficoPeso.gridLabelRenderer.verticalAxisTitle = "Peso (kg)"

            // Ejes fijos para el modo anual
            binding.graficoPeso.viewport.setYAxisBoundsManual(true)
            binding.graficoPeso.viewport.setMinY(0.0)
            binding.graficoPeso.viewport.setMaxY(30.0)

            binding.graficoPeso.viewport.setXAxisBoundsManual(true)
            binding.graficoPeso.viewport.setMinX(0.0)
            binding.graficoPeso.viewport.setMaxX(12.0)

            val series = LineGraphSeries(puntos.toTypedArray())
            binding.graficoPeso.removeAllSeries()
            binding.graficoPeso.addSeries(series)

            series.isDrawDataPoints = true
            series.dataPointsRadius = 10f
            series.thickness = 8

            // Datos inferiores
            val ultimoPeso = pesosFiltrados.last().peso.toDouble()
            val primeroPeso = pesosFiltrados.first().peso.toDouble()
            val promedio = pesosFiltrados.map { it.peso.toDouble() }.average()
            val diferencia = ultimoPeso - primeroPeso
            val diferenciaTexto = if (diferencia >= 0) "+%.2f".format(diferencia) else "%.2f".format(diferencia)

            binding.tvUltimoPeso.text = "Último Peso: %.2f kg".format(ultimoPeso)
            binding.tvDiferenciaPeso.text = "Diferencia: $diferenciaTexto kg"
            binding.tvPromedioPeso.text = "Peso Promedio: %.2f kg".format(promedio)

            binding.graficoPeso.visibility = View.VISIBLE
            binding.tvMensajeSinDatos.visibility = View.GONE
        }
    }


    private fun mostrarMensajeSinPesos() {
        binding.graficoPeso.visibility = View.GONE
        binding.tvMensajeSinDatos.visibility = View.VISIBLE
        binding.cardDetalles.visibility = View.GONE
    }

    private fun obtenerAniosDisponibles(): List<Int> {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val años = listaPesos.mapNotNull {
            try {
                val fecha = sdf.parse(it.fecha)
                val calendar = Calendar.getInstance().apply { time = fecha!! }
                calendar.get(Calendar.YEAR)
            } catch (e: Exception) {
                null
            }
        }.distinct().sorted()

        return años
    }
}

