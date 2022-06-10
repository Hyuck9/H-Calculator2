package me.hyuck9.calculator.di

import me.hyuck9.calculator.view.viewmodel.CalculatorInputViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
	viewModel { CalculatorInputViewModel() }
}