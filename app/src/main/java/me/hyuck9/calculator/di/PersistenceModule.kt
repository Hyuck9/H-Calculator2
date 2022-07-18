package me.hyuck9.calculator.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.hyuck9.calculator.data.db.AppDatabase
import me.hyuck9.calculator.data.db.dao.HistoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

	@Provides
	@Singleton
	fun provideAppDatabase(
		@ApplicationContext context: Context
	): AppDatabase = AppDatabase.build(context)

	@Provides
	@Singleton
	fun provideHistoryDao(appDatabase: AppDatabase): HistoryDao = appDatabase.historyDao()

}