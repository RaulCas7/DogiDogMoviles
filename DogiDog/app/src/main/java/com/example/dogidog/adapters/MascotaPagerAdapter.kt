package com.example.dogidog.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.dogidog.mascotas.MascotaInfoFragment

class MascotaPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4 // Número de pestañas

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MascotaInfoFragment()
            1 -> MascotaInfoFragment()
            2 -> MascotaInfoFragment()
            3 -> MascotaInfoFragment()
            else -> MascotaInfoFragment()
        }
    }
}