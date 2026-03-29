package com.sessiontimer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AppItem(val name: String, val packageName: String, var isChecked: Boolean)

class AppAdapter(private val apps: List<AppItem>) :
    RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.checkbox_app)
        val label: TextView = view.findViewById(R.id.tv_app_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.label.text = app.name
        holder.checkbox.isChecked = app.isChecked
        holder.checkbox.setOnCheckedChangeListener { _, checked -> app.isChecked = checked }
    }

    override fun getItemCount() = apps.size

    fun getSelectedPackages(): Set<String> =
        apps.filter { it.isChecked }.map { it.packageName }.toSet()
}
