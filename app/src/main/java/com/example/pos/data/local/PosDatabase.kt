package com.example.pos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [MenuItemEntity::class, OrderEntity::class, AppSettingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {

    abstract fun menuItemDao(): MenuItemDao
    abstract fun orderDao(): OrderDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: PosDatabase? = null

        fun getInstance(context: Context): PosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PosDatabase::class.java,
                    "rk_tiffines_pos.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default menu on creation
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            database.menuItemDao().insertMenuItems(DefaultMenuData.INITIAL_MENU_ITEMS)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
