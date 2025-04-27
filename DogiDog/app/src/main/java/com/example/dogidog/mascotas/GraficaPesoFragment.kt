package com.example.dogidog.mascotas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.databinding.FragmentGraficaPesoBinding
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class GraficaPesoFragment : Fragment() {
    private lateinit var binding: FragmentGraficaPesoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGraficaPesoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mascota: Mascota? = arguments?.getParcelable("mascota")

        // Configurar el Spinner de Período
        binding.spinnerPeriodo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                actualizarGrafica()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Configurar el Spinner de Año
        binding.spinnerAnio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                actualizarGrafica()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        // Si hay una mascota, mostrar sus datos
        mascota?.let {
            binding.tvUltimoPeso.text = "Último Peso: ${it.peso} kg"
            actualizarGrafica()
        }
    }

    // Función para actualizar la gráfica según la selección
    private fun actualizarGrafica() {
        val periodo = binding.spinnerPeriodo.selectedItem.toString()
        val año = binding.spinnerAnio.selectedItem.toString().toInt()

        val datosPeso = obtenerDatosPeso(periodo, año)

        val series = LineGraphSeries(datosPeso.toTypedArray())
        binding.graficoPeso.removeAllSeries()
        binding.graficoPeso.addSeries(series)

        series.isDrawDataPoints = true
        series.dataPointsRadius = 10f
        series.thickness = 8
    }

    // Simula datos de peso según el período y año seleccionados
    private fun obtenerDatosPeso(periodo: String, año: Int): List<DataPoint> {
        return if (periodo == "Mensual") {
            listOf(
                DataPoint(1.0, 12.1), DataPoint(2.0, 12.3),
                DataPoint(3.0, 12.5), DataPoint(4.0, 12.8)
            )
        } else {
            listOf(
                DataPoint(2023.0, 11.5),
                DataPoint(2024.0, 12.5)
            )
        }
    }
}
