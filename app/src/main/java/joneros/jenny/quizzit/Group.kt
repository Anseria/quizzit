package joneros.jenny.quizzit

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by Jenny on 2017-09-22.
 */
@Entity(tableName = "grouplista")
data class Group(@PrimaryKey(autoGenerate = true) val key: Int,
                 val FBid: String,
                 val name: String,
                 val max_score: Int,
                 val description: String,
                 val authorFBid: String)

@Dao
interface GroupDao {
    @Query("SELECT key, FBid, name, max_score, description, authorFBid FROM grouplista")
    fun loadAllGroups(): LiveData<List<Group>>

    @Query("SELECT key, FBid, name, max_score, description, authorFBid FROM grouplista WHERE key = :key")
    fun loadGroup(key: Int): LiveData<Group>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveGroup(group: Group)

    @Delete
    fun removeGroup(group: Group)
}

class GroupViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        val TAG = "GroupViewModel"
    }

    val allGroups: LiveData<List<Group>>
    private val groupsDao: GroupDao
    private val executor: Executor = Executors.newCachedThreadPool()

    init {
        val database = Room
                .databaseBuilder(application, MyDatabase::class.java,
                        "grouplista.db")
                .build()
        groupsDao = database.groupDao()
        allGroups = groupsDao.loadAllGroups()
    }

    override fun onCleared() {
        super.onCleared()
        (executor as ExecutorService).shutdown()
    }

    fun loadGroup(key: Int): LiveData<Group> {
        return groupsDao.loadGroup(key)
    }

    fun saveGroup(group: Group) {
        executor.execute { groupsDao.saveGroup(group) }
    }

    fun removeGroup(group: Group) {
        executor.execute { groupsDao.removeGroup(group) }
    }
}
