package com.example.myapplication.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Base de dados principal da aplicação
 */
@Database(
    entities = [User::class], // adiciona outras entidades conforme necessidade
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class) // mantém se usar conversores, senão remova
abstract class AppDatabase : RoomDatabase() {

    // DAOs
    abstract fun userDao(): UserDao
    // adiciona outros DAOs conforme necessário

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }

        private suspend fun populateDatabase(database: AppDatabase) {
            val userDao = database.userDao()

            // Criar utilizador administrador
            userDao.insertUser(
                User(email = "admin", password = "1234", isAdmin = true)
            )

            // Criar utilizadores de teste
            userDao.insertUser(
                User(email = "joao@ipbeja.pt", password = "1234", isAdmin = false)
            )
            userDao.insertUser(
                User(email = "maria@ipbeja.pt", password = "1234", isAdmin = false)
            )
        }
    }
}

/**
 * Conversores para tipos de dados personalizados
 */
class Converters {

    // Exemplo: converter Date para Long e vice-versa
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
