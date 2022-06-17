package me.hyuck9.calculator.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

inline fun <T> LifecycleOwner.observeLiveData(data: LiveData<T>, crossinline onChanged: (T) -> Unit) {
    data.observe(this) {
        it?.let { value ->
            onChanged(value)
        }
    }
}