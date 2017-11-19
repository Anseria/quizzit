package joneros.jenny.quizzit

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by thomas on 2017-11-19.
 */
@Entity(tableName = "userlista")
data class User(@PrimaryKey(autoGenerate = false) val FBid : String,
                val name: String)

@Dao
interface UserDao {
    @Query("SELECT FBid, name FROM userlista")
    fun loadAllUsers(): LiveData<List<User>>

    @Query("SELECT FBid, name FROM userlista WHERE FBid = :FBid")
    fun loadUser(FBid: String): LiveData<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUser(user: User)

    @Delete
    fun removeUser(user: User)
}

class UserViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        val TAG = "UserViewModel"
    }

    val allUsers: LiveData<List<User>>
    private val usersDao: UserDao
    private val executor: Executor = Executors.newCachedThreadPool()
    val userRepository = UserRepository()

    init {
        val database = Room
                .databaseBuilder(application, MyDatabase::class.java,
                        "userlista.db")
                .build()
        usersDao = database.usersDao()
        allUsers = usersDao.loadAllUsers()
    }

    override fun onCleared() {
        super.onCleared()
        (executor as ExecutorService).shutdown()
    }

    fun loadUser(FBid: String): LiveData<User> {
        return usersDao.loadUser(FBid)
    }

    fun saveUser(user: User) {
        executor.execute { usersDao.saveUser(user) }
    }

    fun removeUser(user: User) {
        executor.execute { usersDao.removeUser(user) }
    }

    fun saveUserToFirebase(user: User): String {
        return userRepository.saveUser(user)
    }

    fun loadUserFromFirebase(FBid: String) {

    }

    fun removeUserFromFirebase(user: User) {

    }
}