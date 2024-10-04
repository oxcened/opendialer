package dev.alenajam.opendialer.feature.calls

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import dev.alenajam.opendialer.core.common.CircleTransform
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.data.calls.CallOption
import dev.alenajam.opendialer.data.calls.CallType
import dev.alenajam.opendialer.data.calls.ContactInfo
import dev.alenajam.opendialer.data.calls.DialerCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ocpsoft.prettytime.PrettyTime

private val circleTransform: Transformation = CircleTransform()
private const val itemHeight = 75f
private const val optionHeight = 50
private const val expandAnimDuration = 200L

class RecentsAdapter(
  context: Context,
  private val recyclerView: RecyclerView,
  private val coroutineScope: CoroutineScope,
  private val onCallClick: (call: DialerCall) -> Unit,
  private val onContactClick: (call: DialerCall) -> Unit,
  private val onOptionClick: (call: DialerCall, option: CallOption) -> Unit,
  private val updateContactInfo: (number: String?, countryIso: String?, callLogInfo: ContactInfo) -> Unit
) : RecyclerView.Adapter<RecentsAdapter.ViewHolder>() {
  private val incoming: Drawable? = ContextCompat.getDrawable(context, R.drawable.icon_21)
  private val outgoing: Drawable? = ContextCompat.getDrawable(context, R.drawable.icon_16)
  private val missed: Drawable? = ContextCompat.getDrawable(context, R.drawable.icon_22)
  private val voicemail: Drawable? = ContextCompat.getDrawable(context, R.drawable.icon_09)
  private val blocked: Drawable? = ContextCompat.getDrawable(context, R.drawable.icon_18)
  val calls = mutableListOf<DialerCall>()
  private var expandedItem: Int = -1

  inner class ViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_call_log_new, parent, false)) {
    private val card = itemView.findViewById<CardView>(R.id.cardView)
    private val textView = itemView.findViewById<TextView>(R.id.item_call_title)
    private val date = itemView.findViewById<TextView>(R.id.item_call_subtitle)
    private val buttonCall = itemView.findViewById<ImageView>(R.id.icon)
    private val callsIcons = itemView.findViewById<ImageView>(R.id.calls_icons)
    private val contactIcon = itemView.findViewById<ImageView>(R.id.icon_contact)
    private val recyclerViewOptions =
      itemView.findViewById<RecyclerView>(R.id.recyclerViewOptions)
    private var animator: ValueAnimator? = null
    var job: Job? = null

    private fun expand(
      call: DialerCall,
      holder: RecentsAdapter.ViewHolder,
      expand: Boolean,
      animate: Boolean? = false
    ) {
      val height = convertDpToPixels(itemHeight, parent.context)
      val expandedHeight = convertDpToPixels(
        itemHeight + (call.options.size * optionHeight),
        parent.context
      )
      val elevation = convertDpToPixels(0f, parent.context)
      val elevationExpanded = convertDpToPixels(4f, parent.context)
      if (animate == true) {
        animator?.cancel()
        animator = getValueAnimator(
          expand,
          expandAnimDuration,
          AccelerateDecelerateInterpolator()
        ) {
          holder.card.layoutParams.height =
            (height + (expandedHeight - height) * it).toInt()
          holder.card.cardElevation = (elevation + (elevationExpanded - elevation) * it)
          holder.card.requestLayout()
        }
        if (expand) animator?.doOnStart {
          holder.recyclerViewOptions.visibility = View.VISIBLE
        }
        animator?.doOnEnd {
          animator = null
          if (!expand) holder.recyclerViewOptions.visibility = View.GONE
        }
        animator?.start()
      } else {
        holder.card.layoutParams.height = (if (expand) expandedHeight else height).toInt()
        holder.card.cardElevation = if (expand) elevationExpanded else elevation
        holder.recyclerViewOptions.visibility = if (expand) View.VISIBLE else View.GONE
      }
    }

    fun bind(currentCall: DialerCall, position: Int) {
      val context = parent.context
      val contact = currentCall.contactInfo

      /** Update call log only if number is dialable */
      if (!currentCall.isAnonymous() && PermissionUtils.hasContactsPermission(context)) {
        job = coroutineScope.launch(Dispatchers.IO) {
          updateContactInfo(
            currentCall.number,
            currentCall.countryIso,
            currentCall.contactInfo
          )
        }
      }

      var name = contact.name
      val number = contact.number

      if (currentCall.isAnonymous()) {
        name = context.getString(R.string.anonymous)
      } else if (name.isNullOrBlank()) {
        name = number
      }

      textView.text = name

      val isExpanded = position == expandedItem
      expand(currentCall, this, isExpanded)

      Picasso.get()
        .load(contact.photoUri)
        .transform(circleTransform)
        .into(contactIcon)

      contactIcon.setOnClickListener { onContactClick(currentCall) }

      card.setOnClickListener {
        val isExpandedNow = position == expandedItem
        if (!isExpandedNow) {
          getExpandedItemViewHolder()?.let {
            expand(
              currentCall,
              it,
              expand = false,
              animate = true
            )
          }
          notifyItemChanged(expandedItem)
        }

        expandedItem = if (isExpandedNow) -1 else position
        expand(currentCall, this, !isExpandedNow, true)
      }

      callsIcons.setImageDrawable(
        when (currentCall.type) {
          CallType.OUTGOING -> outgoing
          CallType.INCOMING, CallType.ANSWERED_EXTERNALLY -> incoming
          CallType.MISSED, CallType.REJECTED -> missed
          CallType.BLOCKED -> blocked
          CallType.VOICEMAIL -> voicemail
        }
      )

      val prettyDate = PrettyTime().format(currentCall.date)

      date.text =
        if (currentCall.childCalls.size > 1) {
          context.getString(
            R.string.call_log_item_subtitle_number,
            currentCall.childCalls.size,
            prettyDate
          )
        } else {
          context.getString(R.string.call_log_item_subtitle, prettyDate)
        }

      if (currentCall.isAnonymous()) {
        buttonCall.visibility = View.INVISIBLE
      } else {
        buttonCall.setOnClickListener { onCallClick(currentCall) }
        buttonCall.visibility = View.VISIBLE
      }

      recyclerViewOptions.layoutManager = LinearLayoutManager(context)
      recyclerViewOptions.adapter = CallOptionsAdapter(currentCall.options) {
        onOptionClick(currentCall, it)
      }
    }
  }

  private fun getExpandedItemViewHolder(): RecentsAdapter.ViewHolder? {
    return recyclerView.findViewHolderForAdapterPosition(expandedItem) as? RecentsAdapter.ViewHolder?
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
    ViewHolder(LayoutInflater.from(parent.context), parent)

  override fun getItemCount(): Int = calls.size

  override fun onBindViewHolder(holder: ViewHolder, position: Int) =
    holder.bind(calls[position], position)

  override fun onViewRecycled(holder: ViewHolder) {
    holder.job?.cancel()
  }

  fun setData(calls: List<DialerCall>) {
    val oldCalls = this.calls.toList()
    this.calls.apply {
      clear()
      addAll(calls)
    }
    if (oldCalls.isEmpty()) {
      notifyDataSetChanged()
    } else {
      coroutineScope.launch(Dispatchers.IO) {
        val diffUtil = RecentsDiffUtil(oldCalls, calls)
        val result = DiffUtil.calculateDiff(diffUtil)
        withContext(Dispatchers.Main) {
          result.dispatchUpdatesTo(this@RecentsAdapter)
        }
      }
    }
  }
}

inline fun getValueAnimator(
  forward: Boolean = true,
  duration: Long? = null,
  interpolator: TimeInterpolator? = null,
  crossinline updateListener: (progress: Float) -> Unit
): ValueAnimator {
  val a = if (forward) ValueAnimator.ofFloat(0f, 1f) else ValueAnimator.ofFloat(1f, 0f)
  a.addUpdateListener { updateListener(it.animatedValue as Float) }
  duration?.let { a.duration = it }
  interpolator?.let { a.interpolator = it }
  return a
}

fun convertDpToPixels(dp: Float, context: Context): Float {
  return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}