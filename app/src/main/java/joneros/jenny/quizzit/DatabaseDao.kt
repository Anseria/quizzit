package joneros.jenny.quizzit

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

/**
 * Created by Jenny on 2017-09-28.
 */
@Database(entities = arrayOf(Group::class, Question::class, User::class, MySettings::class), version = 8)

abstract class MyDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun questionDao(): QuestionDao
    abstract fun usersDao(): UserDao
    abstract fun settingsDao(): SettingsDao
}