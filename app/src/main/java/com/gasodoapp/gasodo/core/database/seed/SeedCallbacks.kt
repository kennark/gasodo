package com.gasodoapp.gasodo.core.database.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gasodoapp.gasodo.core.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider

/**
 * Callbacks to execute on database creation.
 */
class SeedCallbacks(
    private val provider: Provider<AppDatabase>,
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        scope.launch(Dispatchers.IO) {
            provider.get().maintenanceServiceTypeDao().insertAll(MaintenanceServiceTypesSeed.list)
        }
    }
}