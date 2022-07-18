package me.hyuck9.calculator.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import me.hyuck9.calculator.data.db.dao.HistoryDao
import me.hyuck9.calculator.data.repository.HistoryRepository

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

	@Provides
	@ViewModelScoped
	fun provideHistoryRepository(
		historyDao: HistoryDao
	): HistoryRepository = HistoryRepository(historyDao)

}