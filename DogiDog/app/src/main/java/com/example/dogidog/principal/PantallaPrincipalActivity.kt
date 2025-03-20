package com.example.dogidog.principal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.dogidog.R
import com.example.dogidog.databinding.ActivityPantallaPrincipalBinding

class PantallaPrincipalActivity : AppCompatActivity() {
    lateinit var binding: ActivityPantallaPrincipalBinding
    lateinit var mapsFragment: MapsFragment
    lateinit var mascotasFragment: MascotasFragment
    lateinit var notificacionesFragment: NotificacionesFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPantallaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Define el índice del elemento que deseas seleccionar por defecto (en este ejemplo, el segundo elemento)
        val defaultItemIndex = 2
        // Selecciona el elemento por defecto
        binding.navegacion.menu.getItem(defaultItemIndex).isChecked = true
        initComponents()
        seleccionarToolbar()
    }

    private fun initComponents() {
        mapsFragment = MapsFragment()
        mascotasFragment = MascotasFragment()
        notificacionesFragment = NotificacionesFragment()
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

                R.id.navigation_notificaciones -> {

                    setCurrentFragment(notificacionesFragment)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }
}