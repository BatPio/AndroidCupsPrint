package io.github.benoitduffez.cupsprint.app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import io.github.benoitduffez.cupsprint.R
import io.github.benoitduffez.cupsprint.databinding.ActivityManageManualPrintersBinding
import io.github.benoitduffez.cupsprint.databinding.ManagePrintersListItemBinding
import java.util.ArrayList

class ManageManualPrintersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageManualPrintersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageManualPrintersBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Build adapter
        val prefs = getSharedPreferences(AddPrintersActivity.SHARED_PREFS_MANUAL_PRINTERS, Context.MODE_PRIVATE)
        val numPrinters = prefs.getInt(AddPrintersActivity.PREF_NUM_PRINTERS, 0)
        val printers = getPrinters(prefs, numPrinters)
        val adapter = ManualPrintersAdapter(this, R.layout.manage_printers_list_item, printers)

        // Setup adapter with click to remove
        binding.managePrintersList.adapter = adapter
        binding.managePrintersList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val editor = prefs.edit()
            val actualNumPrinters = prefs.getInt(AddPrintersActivity.PREF_NUM_PRINTERS, 0)
            editor.putInt(AddPrintersActivity.PREF_NUM_PRINTERS, actualNumPrinters - 1)
            editor.remove(AddPrintersActivity.PREF_NAME + position)
            editor.remove(AddPrintersActivity.PREF_URL + position)
            editor.apply()
            adapter.removeItem(position)
        }

        binding.managePrintersEmpty.visibility = if (numPrinters <= 0) View.VISIBLE else View.GONE
    }

    private fun getPrinters(prefs: SharedPreferences, numPrinters: Int): List<ManualPrinterInfo> {
        if (numPrinters <= 0) {
            return ArrayList()
        }
        val printers = ArrayList<ManualPrinterInfo>(numPrinters)
        var url: String?
        var name: String?
        for (i in 0 until numPrinters) {
            name = prefs.getString(AddPrintersActivity.PREF_NAME + i, null)
            url = prefs.getString(AddPrintersActivity.PREF_URL + i, null)
            if (name != null && url != null) {
                printers.add(ManualPrinterInfo(name, url))
            }
        }
        return printers
    }

    private class ManualPrinterInfo(internal var name: String, internal var url: String)
    private class ManualPrinterInfoViews internal constructor(internal var name: TextView, internal var url: TextView)

    private class ManualPrintersAdapter(context: Context, @LayoutRes resource: Int, objects: List<ManualPrinterInfo>) : ArrayAdapter<ManualPrinterInfo>(context, resource, objects) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = when (convertView) {
                null -> {
                    var itemBinding = ManagePrintersListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    val inflate = LayoutInflater.from(parent.context).inflate(R.layout.manage_printers_list_item, parent, false)
                    inflate.tag = ManualPrinterInfoViews(itemBinding.manualPrinterName, itemBinding.manualPrinterUrl)
                    inflate
                }
                else -> convertView
            }

            val views = view.tag as ManualPrinterInfoViews

            val info = getItem(position)
            if (info != null) {
                views.name.text = info.name
                views.url.text = info.url
            } else {
                throw IllegalStateException("Manual printers list can't have invalid items")
            }

            return view
        }

        fun removeItem(position: Int) = remove(getItem(position))
    }
}
