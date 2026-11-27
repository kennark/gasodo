package com.gasodoapp.gasodo.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Provides a fresh in-memory [AppDatabase] for every test.
 *
 * The database is built from the real Room generated implementation against the SQLite engine of the
 * device, so the hand written SQL in the DAOs is actually executed. Because it lives in memory, each
 * test starts from an empty schema (no seed data from
 * [com.gasodoapp.gasodo.core.database.seed.SeedCallbacks]) and leaves nothing behind.
 */
class InMemoryDatabaseRule : TestRule {

    lateinit var database: AppDatabase
        private set

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                val context = ApplicationProvider.getApplicationContext<Context>()
                database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
                try {
                    base.evaluate()
                } finally {
                    database.close()
                }
            }
        }
}
