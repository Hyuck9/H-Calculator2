package me.hyuck9.calculator.view.base

import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivity : AppCompatActivity() {

	protected inline fun <reified T : ViewDataBinding> binding(
		@LayoutRes resId: Int
	): Lazy<T> = lazy {
		val binding: ViewDataBinding = DataBindingUtil.setContentView(this, resId)
		binding.lifecycleOwner = this@BaseActivity
		binding as T
	}
}