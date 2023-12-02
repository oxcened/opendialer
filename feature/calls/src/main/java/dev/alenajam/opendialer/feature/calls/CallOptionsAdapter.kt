package dev.alenajam.opendialer.feature.calls

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.alenajam.opendialer.data.calls.CallOption

class CallOptionsAdapter(
  private var options: List<CallOption> = emptyList(),
  private val onClick: (option: CallOption) -> Unit
) : RecyclerView.Adapter<CallOptionsAdapter.ViewHolder>() {
  inner class ViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_call_log_child, parent, false)) {
    private val layout = itemView.findViewById<LinearLayout>(R.id.layout)
    private val icon = itemView.findViewById<ImageView>(R.id.icon)
    private val text = itemView.findViewById<TextView>(R.id.text)

    fun bind(option: CallOption) {
      //icon.setImageDrawable(ContextCompat.getDrawable(parent.context, option.icon))

      val resId = when (option.id) {
        CallOption.ID_CREATE_CONTACT -> R.string.create_new_contact
        CallOption.ID_ADD_EXISTING -> R.string.add_to_a_contact
        CallOption.ID_SEND_MESSAGE -> R.string.send_message
        CallOption.ID_CALL_DETAILS -> R.string.call_details
        CallOption.ID_COPY_NUMBER -> R.string.copy_number
        CallOption.ID_EDIT_BEFORE_CALL -> R.string.edit_number_before_call
        CallOption.ID_BLOCK_CALLER -> R.string.blockThisCaller
        CallOption.ID_UNBLOCK_CALLER -> R.string.unblockThisCaller
        CallOption.ID_DELETE -> R.string.delete
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
}
