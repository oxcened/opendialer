package dev.alenajam.opendialer.core.di.viewmodel

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * Custom key for [ViewModel]s
 */
@MapKey
@Target(
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)