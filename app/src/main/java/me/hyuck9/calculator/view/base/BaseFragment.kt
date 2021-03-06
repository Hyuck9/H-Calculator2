package me.hyuck9.calculator.view.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

	protected inline fun <reified T : ViewDataBinding> binding(
		inflater: LayoutInflater,
		@LayoutRes resId: Int,
		container: ViewGroup?
	): T {
		val binding: ViewDataBinding = DataBindingUtil.inflate(inflater, resId, container, false)
		binding.lifecycleOwner = viewLifecycleOwner
		return binding as T
	}

}