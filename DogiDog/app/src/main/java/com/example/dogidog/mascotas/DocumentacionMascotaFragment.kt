package com.example.dogidog.mascotas

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogidog.R
import com.example.dogidog.adapters.DocumentosAdapter
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Documentacion
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.databinding.FragmentDocumentacionMascotaBinding
import com.example.dogidog.principal.ConfiguracionFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


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


        setHasOptionsMenu(true)
        mascota = arguments?.getParcelable("mascota")!!
        configurarToolbar(mascota)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Cambia esta URL por la de tu servidor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(ApiService::class.java)

        service.obtenerDocumentacionPorMascota(mascota.id).enqueue(object :
            Callback<List<Documentacion>> {
            override fun onResponse(call: Call<List<Documentacion>>, response: Response<List<Documentacion>>) {
                if (response.isSuccessful && response.body() != null) {
                    documentosOriginal = response.body()!!

                    adapter = DocumentosAdapter(requireContext(), documentosOriginal.toMutableList())
                    binding.recyclerViewDocumentos.layoutManager = LinearLayoutManager(requireContext())
                    binding.recyclerViewDocumentos.adapter = adapter

                    setupSearch()
                    setupFiltroTipo()
                } else {
                    Toast.makeText(requireContext(), "No hay documentación disponible.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Documentacion>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error al obtener los documentos", Toast.LENGTH_SHORT).show()
            }
        })
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

    private fun configurarToolbar(mascota: Mascota) {
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar ?: return

        // Limpiar cualquier customView previa
        actionBar.customView?.let {
            (it.parent as? ViewGroup)?.removeView(it)
        }

        // Crear el TextView personalizado
        val titleTextView = TextView(requireContext()).apply {
            text = mascota.nombre
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            val actionBarHeight = resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material)
            val textSize = (actionBarHeight * 0.5f).toFloat()
            this.textSize = textSize / resources.displayMetrics.density
        }

        actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar.customView = titleTextView

        actionBar.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.primario)))
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)

        setHasOptionsMenu(true)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_nuevo_documento -> { // El ítem de configuración
                // Aquí navegas a tu fragmento de configuración
                (activity as AppCompatActivity).supportFragmentManager.beginTransaction().apply {
                    replaceFragment(CrearDocumentoFragment(), mascota)
                    addToBackStack(null) // Si quieres que el fragmento de configuración se agregue a la pila de retroceso
                    commit()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu, menu) // Inflar el menú
        val menuItem = menu.findItem(R.id.action_delete)
        val menuItemOptions = menu.findItem(R.id.action_options)
        val menuItemAnadir = menu.findItem(R.id.action_nuevo_documento)
        menuItem.isVisible = false
        menuItemOptions.isVisible = false
        menuItemAnadir.isVisible = true;
        setHasOptionsMenu(true) // Permitir que el fragmento maneje los ítems del menú
    }

    private fun replaceFragment(fragment: Fragment, mascota: Mascota) {
        val bundle = Bundle()
        bundle.putParcelable("mascota", mascota)  // Pasar los datos de la mascota
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
