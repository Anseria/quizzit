package joneros.jenny.quizzit

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

/**
 * Created by Jenny on 2017-09-28.
 */
@Database(entities = arrayOf(Group::class, Question::class), version = 3)

abstract class MyDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun questionDao(): QuestionDao
}