package com.example.dogidog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.dogidog.databinding.ActivityPantallaPrincipalBinding

class PantallaPrincipalActivity : AppCompatActivity() {
    lateinit var binding: ActivityPantallaPrincipalBinding
    lateinit var mapsFragment: MapsFragment
    lateinit var mascotasFragment: MascotasFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPantallaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Define el índice del elemento que deseas seleccionar por defecto (en este ejemplo, el segundo elemento)
        val defaultItemIndex = 1
        // Selecciona el elemento por defecto
        binding.navegacion.menu.getItem(defaultItemIndex).isChecked = true
        initComponents()
        seleccionarToolbar()
    }

    private fun initComponents() {
        mapsFragment = MapsFragment()
        mascotasFragment = MascotasFragment()
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.containerView, fragment)
            commit()
        }
    }

    private fun seleccionarToolbar() {
        binding.navegacion.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_mascotas -> {

                    setCurrentFragment(mascotasFragment)
                    true
                }

                R.id.navigation_mapa -> {

                    setCurrentFragment(mapsFragment)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }
}