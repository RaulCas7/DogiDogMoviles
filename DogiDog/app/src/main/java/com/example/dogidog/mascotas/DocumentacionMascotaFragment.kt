package com.example.dogidog.mascotas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogidog.R
import com.example.dogidog.adapters.DocumentosAdapter
import com.example.dogidog.dataModels.Documentacion
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.databinding.FragmentDocumentacionMascotaBinding


class DocumentacionFragment : Fragment() {

    private lateinit var binding: FragmentDocumentacionMascotaBinding
    private lateinit var mascota: Mascota
    private lateinit var adapter: DocumentosAdapter
    private lateinit var documentosOriginal: List<Documentacion>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDocumentacionMascotaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mascota = arguments?.getParcelable("mascota")!!

        // Datos de ejemplo (reemplázalo por datos de tu API)
        documentosOriginal = listOf(
            Documentacion(
                id = 1,
                mascota = mascota,
                tipo = "Consulta",
                fecha = "2024-03-10",
                descripcion = "Chequeo general",
                archivo = null,
                creadoEn = "2024-03-10T10:00:00Z"
            ),
            Documentacion(
                id = 2,
                mascota = mascota,
                tipo = "Vacuna Realizada",
                fecha = "2024-02-15",
                descripcion = "Vacuna antirrábica",
                archivo = "vacuna.pdf",
                creadoEn = "2024-02-15T09:30:00Z"
            ),
            Documentacion(
                id = 3,
                mascota = mascota,
                tipo = "Cartilla",
                fecha = "2023-12-01",
                descripcion = "Cartilla sanitaria",
                archivo = "cartilla.jpeg",
                creadoEn = "2023-12-01T08:45:00Z"
            )
        )

        adapter = DocumentosAdapter(requireContext(), documentosOriginal.toMutableList())
        binding.recyclerViewDocumentos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewDocumentos.adapter = adapter

        setupSearch()
        setupFiltroTipo()
    }

    private fun setupSearch() {
        binding.searchViewDocumento.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarDocumentos(newText ?: "", binding.spinnerTituloFiltro.selectedItem.toString())
                return true
            }
        })
    }

    private fun setupFiltroTipo() {

        val adapterSpinner = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.opciones,
            R.layout.spinner_item
        )
        binding.spinnerTituloFiltro.adapter = adapterSpinner

        binding.spinnerTituloFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val filtro = parent?.getItemAtPosition(position).toString()
                val texto = binding.searchViewDocumento.query?.toString() ?: ""
                filtrarDocumentos(texto, filtro)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun filtrarDocumentos(query: String, filtro: String) {
        val filtrados = documentosOriginal.filter {
            when (filtro.lowercase()) {
                "título" -> it.tipo.contains(query, ignoreCase = true)
                "descripción" -> it.descripcion?.contains(query, ignoreCase = true) == true
                "tipo" -> it.tipo.contains(query, ignoreCase = true)
                "fecha" -> it.fecha.contains(query, ignoreCase = true)
                else -> true
            }
        }
        adapter.actualizarLista(filtrados)
    }
}
