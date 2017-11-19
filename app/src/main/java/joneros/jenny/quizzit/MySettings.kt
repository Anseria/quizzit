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

@Entity(tableName = "settingslista")
data class MySettings(@PrimaryKey(autoGenerate = false) val userFBid: String)

@Dao
interface SettingsDao {
    @Query("SELECT userFBid FROM settingslista")
    fun loadSettings(): LiveData<MySettings>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSettings(settings: MySettings)

    @Delete
    fun removeSettings(settings: MySettings)
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        val TAG = "SettingsViewModel"
    }

    val allSettings: LiveData<MySettings>
    private val settingsDao: SettingsDao
    private val executor: Executor = Executors.newCachedThreadPool()

    init {
        val database = Room
                .databaseBuilder(application, MyDatabase::class.java,
                        "settingslista.db")
                .build()
        settingsDao = database.settingsDao()
        allSettings = settingsDao.loadSettings()
    }

    override fun onCleared() {
        super.onCleared()
        (executor as ExecutorService).shutdown()
    }

    fun saveSettings(settings: MySettings) {
        executor.execute { settingsDao.saveSettings(settings) }
    }

    fun removeSettings(settings: MySettings) {
        executor.execute { settingsDao.removeSettings(settings) }
    }
}