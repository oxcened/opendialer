package dev.alenajam.opendialer.features.dialer.calls

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.alenajam.opendialer.R

class CallOptionsAdapter(
  private var options: List<dev.alenajam.opendialer.model.CallOption> = emptyList(),
  private val onClick: (option: dev.alenajam.opendialer.model.CallOption) -> Unit
) : RecyclerView.Adapter<CallOptionsAdapter.ViewHolder>() {
  inner class ViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_call_log_child, parent, false)) {
    private val layout = itemView.findViewById<LinearLayout>(R.id.layout)
    private val icon = itemView.findViewById<ImageView>(R.id.icon)
    private val text = itemView.findViewById<TextView>(R.id.text)

    fun bind(option: dev.alenajam.opendialer.model.CallOption) {
      icon.setImageDrawable(ContextCompat.getDrawable(parent.context, option.icon))

      val resId = when (option.id) {
        dev.alenajam.opendialer.model.CallOption.ID_CREATE_CONTACT -> R.string.create_new_contact
        dev.alenajam.opendialer.model.CallOption.ID_ADD_EXISTING -> R.string.add_to_a_contact
        dev.alenajam.opendialer.model.CallOption.ID_SEND_MESSAGE -> R.string.send_message
        dev.alenajam.opendialer.model.CallOption.ID_CALL_DETAILS -> R.string.call_details
        dev.alenajam.opendialer.model.CallOption.ID_COPY_NUMBER -> R.string.copy_number
        dev.alenajam.opendialer.model.CallOption.ID_EDIT_BEFORE_CALL -> R.string.edit_number_before_call
        dev.alenajam.opendialer.model.CallOption.ID_BLOCK_CALLER -> R.string.blockThisCaller
        dev.alenajam.opendialer.model.CallOption.ID_UNBLOCK_CALLER -> R.string.unblockThisCaller
        dev.alenajam.opendialer.model.CallOption.ID_DELETE -> R.string.delete
        else -> null
      }

      resId?.let {
        text.text = parent.context.getString(resId)
      }

      layout.setOnClickListener { onClick(option) }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
    ViewHolder(LayoutInflater.from(parent.context), parent)

  override fun getItemCount(): Int = options.size

  override fun onBindViewHolder(holder: ViewHolder, position: Int) =
    holder.bind(options[position])

  fun setData(options: List<dev.alenajam.opendialer.model.CallOption>) {
    this.options = options
    notifyDataSetChanged()
  }
}
