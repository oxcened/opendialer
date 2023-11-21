package dev.alenajam.opendialer.features.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.alenajam.opendialer.databinding.FragmentProfileBinding
import dev.alenajam.opendialer.model.OnStatusBarColorChange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class ProfileFragment : Fragment(), View.OnClickListener {
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  private lateinit var binding: FragmentProfileBinding
  private var onStatusBarColorChange: OnStatusBarColorChange? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    parentFragment?.let {
      if (context is OnStatusBarColorChange) {
        onStatusBarColorChange = context
      }
    }
    (activity?.application as dev.alenajam.opendialer.App).applicationComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = FragmentProfileBinding.inflate(inflater).also { binding = it }.root

  @ExperimentalCoroutinesApi
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    listOf(
      binding.about,
      binding.options
    ).forEach { it.setOnClickListener(this) }
  }

  override fun onClick(v: View) {
    when (v) {
      binding.about -> startActivity(Intent(context, dev.alenajam.opendialer.activity.AboutActivity::class.java))
      binding.options -> startActivity(Intent(context, dev.alenajam.opendialer.activity.SettingsActivity::class.java))
    }
  }
}