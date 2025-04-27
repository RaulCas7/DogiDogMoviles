package com.example.dogidog.principal

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class PetSelectionDialogFragment(
    private val onPetSelected: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Elige tu mascota 🐾")

        val pets = arrayOf("Border Collie", "Shiba Inu")
        builder.setItems(pets) { _, which ->
            val selectedPet = when (which) {
                0 -> "bordercollie"
                1 -> "shibainu"
                else -> "bordercollie"
            }
            onPetSelected(selectedPet)
        }

        builder.setCancelable(false)
        return builder.create()
    }
}
