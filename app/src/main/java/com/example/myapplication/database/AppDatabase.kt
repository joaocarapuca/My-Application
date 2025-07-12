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
    entities = [
        User::class, 
        Message::class, 
        Post::class, 
        Like::class, 
        Schedule::class, 
        Alert::class, 
        Group::class, 
        GroupMember::class, 
        GroupMessage::class
    ],
    version = 5,
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
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun groupMessageDao(): GroupMessageDao
    
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
                .fallbackToDestructiveMigration()
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
            val groupDao = database.groupDao()
            val groupMemberDao = database.groupMemberDao()
            
            // Criar utilizador administrador
            userDao.insertUser(
                User(name = "Administrador Geral", email = "admin@ipbeja.pt", password = "1234", isAdmin = true)
            )
            
            // Criar professores
            userDao.insertUser(
                User(name = "Professor António", email = "antonio@ipbeja.pt", password = "1234", isAdmin = true)
            )
            userDao.insertUser(
                User(name = "Professora Ana", email = "ana@ipbeja.pt", password = "1234", isAdmin = true)
            )
            
            // Criar estudantes
            userDao.insertUser(
                User(name = "João Carapuça", email = "joao@ipbeja.pt", password = "1234", isAdmin = false)
            )
            userDao.insertUser(
                User(name = "Maria Silva", email = "maria@ipbeja.pt", password = "1234", isAdmin = false)
            )
            userDao.insertUser(
                User(name = "Laura Remechido", email = "laura@ipbeja.pt", password = "1234", isAdmin = false)
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
            
            // Criar grupos de exemplo
            val siGroupId = groupDao.insertGroup(
                Group(name = "SI", description = "Sistemas de Informação", createdBy = 1)
            )
            val twGroupId = groupDao.insertGroup(
                Group(name = "TW", description = "Tecnologia Web", createdBy = 1)
            )
            val designGroupId = groupDao.insertGroup(
                Group(name = "Design", description = "Design e Multimédia", createdBy = 1)
            )
            
            // Adicionar utilizadores aos grupos
            groupMemberDao.insertGroupMember(GroupMember(groupId = siGroupId.toInt(), userId = 4))
            groupMemberDao.insertGroupMember(GroupMember(groupId = twGroupId.toInt(), userId = 5))
            groupMemberDao.insertGroupMember(GroupMember(groupId = designGroupId.toInt(), userId = 6))
            groupMemberDao.insertGroupMember(GroupMember(groupId = siGroupId.toInt(), userId = 2))
        }
    }
}