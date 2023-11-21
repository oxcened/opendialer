package dev.alenajam.opendialer.features.dialer.calls

import androidx.recyclerview.widget.DiffUtil

class RecentsDiffUtil(
  private val oldList: List<DialerCall>,
  private val newList: List<DialerCall>
) : DiffUtil.Callback() {
  override fun getOldListSize(): Int {
    return oldList.size
  }

  override fun getNewListSize(): Int {
    return newList.size
  }

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    val old = oldList[oldItemPosition]
    val new = newList[newItemPosition]

    return old.id == new.id
  }

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    val old = oldList[oldItemPosition]
    val new = newList[newItemPosition]

    return old.contactInfo == new.contactInfo
  }
}
