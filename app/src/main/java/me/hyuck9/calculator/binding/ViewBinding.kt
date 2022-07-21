package me.hyuck9.calculator.binding

import android.view.View
import android.widget.Toast
import androidx.databinding.BindingAdapter
import me.hyuck9.calculator.App

object ViewBinding {

	@JvmStatic
	@BindingAdapter("toast")
	fun bindToast(view: View, text: String?) {
		if (text.isNullOrEmpty().not()) {
			App.toast?.cancel()
			App.toast = Toast.makeText(view.context, text, Toast.LENGTH_SHORT)
			App.toast!!.show()
		}
	}

}