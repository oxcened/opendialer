package dev.alenajam.opendialer.feature.contactsSearch

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import dev.alenajam.opendialer.core.aosp.QueryBoldingUtil
import dev.alenajam.opendialer.core.common.CircleTransform
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.ContactsHelper
import dev.alenajam.opendialer.data.contactsSearch.DialerSearchContact

private val circleTransform: Transformation = CircleTransform()
private val colorList = listOf(
  Color.parseColor("#4FAF44"),
  Color.parseColor("#F6D145"),
  Color.parseColor("#FF9526"),
  Color.parseColor("#EF4423"),
  Color.parseColor("#328AF0")
)
private val generator = ColorGenerator.create(colorList)

class SearchContactsAdapter(private val onClick: (item: Item) -> Unit) :
  RecyclerView.Adapter<SearchContactsAdapter.ViewHolder>() {
  private var list = listOf<Item>()
  var query: String = ""

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      Item.Index.CONTACT.ordinal -> ViewHolder.Contact(inflater, parent)
      Item.Index.HEADER.ordinal -> ViewHolder.Header(inflater, parent)
      else -> ViewHolder.Action(inflater, parent)
    }
  }

  override fun getItemCount(): Int = list.size

  override fun getItemViewType(position: Int): Int {
    return list[position].index.ordinal
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) = when (holder) {
    is ViewHolder.Contact -> holder.bind((list[position] as Item.Contact), query, onClick)
    is ViewHolder.Header -> holder.bind((list[position] as Item.Header))
    is ViewHolder.Action -> holder.bind((list[position] as Item.Action), query, onClick)
  }

  fun setData(data: List<Item>) {
    this.list = data
    notifyDataSetChanged()
  }

  sealed class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    class Contact(inflater: LayoutInflater, parent: ViewGroup) :
      ViewHolder(inflater.inflate(R.layout.item_search_contact, parent, false)) {
      val context: Context = parent.context
      private val title = itemView.findViewById<TextView>(R.id.title)
      private val subtitle = itemView.findViewById<TextView>(R.id.subtitle)
      private val contactIcon = itemView.findViewById<ImageView>(R.id.contactIcon)

      fun bind(item: Item.Contact, query: String, onClick: (item: Item) -> Unit) {
        val current = item.value

        title.text = QueryBoldingUtil.getNameWithQueryBolded(query, current.name, context)
        subtitle.text = QueryBoldingUtil.getNumberWithQueryBolded(query, current.number)

        Picasso.get()
          .load(current.image)
          .placeholder(context.getContactImagePlaceholder(current, generator))
          .transform(circleTransform)
          .into(contactIcon)

        contactIcon.setOnClickListener {
          val contact = ContactsHelper.getContactByPhoneNumber(context, current.number)
          if (contact != null) CommonUtils.showContactDetail(context, contact.id)
        }

        view.setOnClickListener { onClick(item) }
      }
    }

    class Header(inflater: LayoutInflater, parent: ViewGroup) :
      ViewHolder(inflater.inflate(R.layout.item_market_header, parent, false)) {
      val context: Context = parent.context
      private val title = itemView.findViewById<TextView>(R.id.title)

      fun bind(current: Item.Header) {
        title.text = current.value
      }
    }

    class Action(inflater: LayoutInflater, parent: ViewGroup) :
      ViewHolder(inflater.inflate(R.layout.item_search_contacts_action, parent, false)) {
      val context: Context = parent.context
      private val title = itemView.findViewById<TextView>(R.id.text)
      private val icon = itemView.findViewById<ImageView>(R.id.icon)

      fun bind(current: Item.Action, query: String, onClick: (item: Item) -> Unit) {
        title.text = getTitle(current.type, query)
        icon.setImageDrawable(getIcon(current.type))
        view.setOnClickListener { onClick(current) }
      }

      private fun getTitle(action: Item.Action.ActionType, query: String) = when (action) {
        Item.Action.ActionType.CREATE_NEW_CONTACT -> context.getString(R.string.create_new_contact)
        Item.Action.ActionType.ADD_TO_CONTACT -> context.getString(R.string.add_to_a_contact)
        Item.Action.ActionType.SEND_MESSAGE -> context.getString(R.string.send_message)
        Item.Action.ActionType.MAKE_CALL -> context.getString(
          R.string.searchContactsActionMakeCall,
          query
        )
      }

      private fun getIcon(action: Item.Action.ActionType): Drawable? {
        return ContextCompat.getDrawable(
          context, when (action) {
            Item.Action.ActionType.CREATE_NEW_CONTACT -> R.drawable.icon_04
            Item.Action.ActionType.ADD_TO_CONTACT -> R.drawable.icon_04
            Item.Action.ActionType.SEND_MESSAGE -> R.drawable.icon_05
            Item.Action.ActionType.MAKE_CALL -> R.drawable.icon_20_rotate
          }
        )
      }
    }
  }

  sealed class Item(val index: Index) {
    class Contact(val value: DialerSearchContact) : Item(Index.CONTACT)
    class Header(val value: String) : Item(Index.HEADER)
    class Action(val type: ActionType) : Item(Index.ACTION) {
      enum class ActionType {
        CREATE_NEW_CONTACT,
        ADD_TO_CONTACT,
        MAKE_CALL,
        SEND_MESSAGE
      }
    }

    enum class Index {
      CONTACT,
      HEADER,
      ACTION
    }
  }
}

fun Context.getContactImagePlaceholder(
  contact: DialerSearchContact,
  generator: ColorGenerator
): TextDrawable {
  val filteredName = contact.name.replace("[^a-zA-Z0-9]".toRegex(), "")
  var firstCharStr = ""

  if (filteredName.isNotEmpty()) {
    val firstChar = filteredName[0]
    firstCharStr = firstChar.toString()
  }

  return TextDrawable.builder()
    .beginConfig()
    .endConfig()
    .buildRound(firstCharStr, generator.getColor(contact.id))
}