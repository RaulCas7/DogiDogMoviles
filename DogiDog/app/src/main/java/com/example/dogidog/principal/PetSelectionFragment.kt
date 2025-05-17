package com.example.dogidog.principal

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.dogidog.dataModels.Mascota

class PetSelectionDialogFragment (
    private val mascotas: List<Mascota>,
    private val onPetSelected: (Mascota) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Elige tu mascota 🐾")

        // Extraemos solo los nombres
        val nombresMascotas = mascotas.map { it.nombre }.toTypedArray()

        builder.setItems(nombresMascotas) { _, which ->
            val selectedMascota = mascotas[which] // Elegimos la mascota directamente
            onPetSelected(selectedMascota)
        }

        builder.setCancelable(false)
        return builder.create()
    }
}
