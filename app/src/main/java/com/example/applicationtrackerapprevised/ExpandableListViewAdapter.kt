package com.example.applicationtrackerapprevised

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class ExpandableListViewAdapter : BaseExpandableListAdapter {

    private lateinit var context : Context
    private lateinit var expandableTitleList : MutableList<String>
    private lateinit var expandableDetailList : HashMap<String, MutableList<String>>

    constructor(context : Context, titles : MutableList<String>, details : HashMap<String, MutableList<String>>) {
        this.context = context
        expandableTitleList = titles
        expandableDetailList = details
    }

    override fun getGroupCount(): Int {
        return this.expandableTitleList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return this.expandableDetailList.get(this.expandableTitleList.get(groupPosition))!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return this.expandableTitleList.get(groupPosition)
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return this.expandableDetailList.get(this.expandableTitleList.get(groupPosition))!!.get(childPosition)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var company : String = getGroup(groupPosition).toString()
        var convert : View? = convertView

        if (convert == null){
            val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convert = inflater.inflate(R.layout.companies_list, null)

        }

        var companyTV : TextView = convert!!.findViewById(R.id.companies_tv)
        companyTV.text = company

        return convert
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var detail : String = getChild(groupPosition, childPosition).toString()
        var convert : View? = convertView

        if (convert == null){
            val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convert = inflater.inflate(R.layout.details_list, null)
        }

        var detailTV : TextView = convert!!.findViewById(R.id.details_tv)
        detailTV.text = detail

        return convert
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}