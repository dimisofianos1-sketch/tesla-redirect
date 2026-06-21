package gr.posokanei4real.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gr.posokanei4real.app.data.local.AppDatabase
import gr.posokanei4real.app.data.local.GroceryDao
import gr.posokanei4real.app.data.local.PriceDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "posokanei.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideGroceryDao(db: AppDatabase): GroceryDao = db.groceryDao()

    @Provides fun providePriceDao(db: AppDatabase): PriceDao = db.priceDao()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    fun provideContext(@ApplicationContext ctx: Context): Context = ctx
}
