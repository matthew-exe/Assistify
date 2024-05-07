package com.example.final_login

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class ProfileAdapter(private val context: Context): PagerAdapter() {
    private val layouts = mutableListOf(R.layout.activity_profile_link)

    fun addLinkedProfile(userDetails: ProfileData) {
        layouts.add(R.layout.activity_profile_linked)
        notifyDataSetChanged()
    }
    
    override fun getCount(): Int {
        return layouts.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(layouts[position], container, false)
        container.addView(layout)
        return layout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}