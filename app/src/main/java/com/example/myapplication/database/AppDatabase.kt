package com.example.myapplication.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de dados principal da aplicação
 */
@Database(
    entities = [User::class, Message::class, Post::class, Like::class, Schedule::class, Alert::class],
    version = 2, // Incrementar versão devido à mudança na estrutura
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    // DAOs
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun postDao(): PostDao
    abstract fun likeDao(): LikeDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun alertDao(): AlertDao
    
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
                .fallbackToDestructiveMigration() // Para desenvolvimento - remove em produção
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * Callback para popular a base de dados com dados iniciais
     */
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }
        
        /**
         * Popular base de dados com dados iniciais
         */
        private suspend fun populateDatabase(database: AppDatabase) {
            val userDao = database.userDao()
            val scheduleDao = database.scheduleDao()
            
            // Criar utilizador administrador
            userDao.insertUser(
                User(name = "Administrador", email = "admin", password = "1234", isAdmin = true)
            )
            
            // Criar utilizadores de teste
            userDao.insertUser(
                User(name = "João Carapuça", email = "joao@ipbeja.pt", password = "1234", isAdmin = false)
            )
            userDao.insertUser(
                User(name = "Maria Silva", email = "maria@ipbeja.pt", password = "1234", isAdmin = false)
            )
            userDao.insertUser(
                User(name = "Laura Remechido", email = "laura@ipbeja.pt", password = "1234", isAdmin = false)
            )
            userDao.insertUser(
                User(name = "Professor António", email = "professor@ipbeja.pt", password = "1234", isAdmin = true)
            )
            
            // Criar horários de exemplo
            scheduleDao.insertSchedule(
                Schedule(timeSlot = "08:00-10:00", monday = "SI", tuesday = "Design", wednesday = "Matemática", thursday = "TW", friday = "SI")
            )
            scheduleDao.insertSchedule(
                Schedule(timeSlot = "10:00-12:00", monday = "TW", tuesday = "Matemática", wednesday = "SI", thursday = "Design", friday = "Matemática")
            )
            scheduleDao.insertSchedule(
                Schedule(timeSlot = "14:00-16:00", monday = "Design", tuesday = "SI", wednesday = "TW", thursday = "Matemática", friday = "Design")
            )
        }
    }
}