package dev.alenajam.opendialer.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import dev.alenajam.opendialer.R
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigationBadged(context: Context, attributeSet: AttributeSet) :
  BottomNavigationView(context, attributeSet) {

  private val badges = mutableMapOf<Int, View>()

  /*fun showItemBadge(position: Int, show: Boolean) {
    val item = getItem(position)

    if (show) {
      if (badges.contains(position)) return

      val badgeView = inflate(context, R.layout.bottom_nav_badge, null)
      item.addView(badgeView)

      badges[position] = badgeView
    } else {
      if (badges.contains(position)) {
        val badgeView = badges[position]
        item.removeView(badgeView)
        badges.remove(position)
      }
    }
  }*/

  /*private fun getItem(position: Int): BottomNavigationItemView {
    val menuView = getChildAt(0) as BottomNavigationMenuView
    return menuView.getChildAt(position) as BottomNavigationItemView
  }*/
}