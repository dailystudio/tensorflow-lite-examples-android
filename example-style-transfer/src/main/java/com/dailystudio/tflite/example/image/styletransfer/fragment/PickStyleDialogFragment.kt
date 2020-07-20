package com.dailystudio.tflite.example.image.styletransfer.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.dailystudio.tflite.example.image.styletransfer.R


class PickStyleDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setView(R.layout.dialog_pick_styles)
        builder.setNegativeButton(R.string.label_back) { dialog, _ ->
            dialog.dismiss()
        }

        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        /* avoid crash issue of recreating dialog fragment */
        val fragment = parentFragmentManager.findFragmentById(R.id.fragment_styles)
        fragment?.let {
            parentFragmentManager.beginTransaction().remove(fragment).commit();
        }

    }

}