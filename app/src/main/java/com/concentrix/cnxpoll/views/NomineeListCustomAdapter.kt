package com.concentrix.cnxpoll.views

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.concentrix.cnxpoll.R

/**
 * Created by Kusuma 23/05/19
 */

class NomineeListCustomAdapter(internal var context: Activity, internal var nomineeNames: ArrayList<String>, val nomineeList: ListView, val searchEditText: EditText, var btnNominate: Button, var tvSelectedNomenee: TextView) : BaseAdapter() {
    var nominatedName: String = ""

    override fun getCount(): Int {
        return nomineeNames.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    private inner class ViewHolder {
        internal var txtViewTitle: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        val inflater = context.layoutInflater

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.nominee_listview_adapter, null)
            holder = ViewHolder()
            holder.txtViewTitle = convertView!!.findViewById<View>(R.id.textViewItem) as TextView

            convertView.tag = holder

        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.txtViewTitle!!.text = nomineeNames[position]

        nomineeList.onItemClickListener = object : AdapterView.OnItemClickListener {

            override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long){
                nominatedName = nomineeNames[position]
                tvSelectedNomenee.text = "Selected Name:" + " " + nominatedName
                btnNominate.isEnabled = true
                btnNominate.setBackgroundColor(ContextCompat.getColor(context, R.color.colorpink))
            }
        }

        return convertView
    }

    fun filterList(filterdNames: ArrayList<String>) {
        this.nomineeNames = filterdNames
        notifyDataSetChanged()
    }
}

