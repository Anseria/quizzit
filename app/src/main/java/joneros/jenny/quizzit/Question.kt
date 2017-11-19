package joneros.jenny.quizzit

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.persistence.room.*
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Entity(tableName = "questionlista")
data class Question(@PrimaryKey(autoGenerate = true) val key: Int,
                    val question: String,
                    val answer: String,
                    val image: String,
                    val groupname: Int)

@Dao
interface QuestionDao {
    @Query("SELECT key, question, answer, image, groupname FROM questionlista")
    fun loadAllQuestions(): LiveData<List<Question>>

    @Query("SELECT key, question, answer, image, groupname FROM questionlista WHERE key = :key")
    fun loadQuestion(key: Int): LiveData<Question>

    @Query("SELECT key, question, answer, image, groupname FROM questionlista WHERE groupname = :groupname")
    fun loadQuestionsByGroup(groupname: Int): LiveData<List<Question>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveQuestion(question: Question): Long

    @Delete
    fun removeQuestion(question: Question)
}

class QuestionViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        val TAG = "QuestionViewModel"
    }

    val allQuestions: LiveData<List<Question>>
    private val questionsDao: QuestionDao
    private val executor: Executor = Executors.newCachedThreadPool()
    private val context = application

    init {
        val database = Room
                .databaseBuilder(application, MyDatabase::class.java,
                        "questionlista.db")
                .build()
        questionsDao = database.questionDao()
        allQuestions = questionsDao.loadAllQuestions()
    }

    override fun onCleared() {
        super.onCleared()
        (executor as ExecutorService).shutdown()
    }

    fun loadQuestion(key: Int): LiveData<Question> {
        return questionsDao.loadQuestion(key)
    }

    fun loadQuestionsByGroup(groupname: Int): LiveData<List<Question>> {
        return questionsDao.loadQuestionsByGroup(groupname)
    }

    /*fun openBitmap(fileInput: InputStream): LiveData<Bitmap> {
        val mediatorLiveData = MediatorLiveData<Bitmap>()

        executor.execute {
            val bitmap = BitmapFactory.decodeStream(fileInput)
            mediatorLiveData.postValue(bitmap)
        }

        return mediatorLiveData
    }*/

    fun openBitmap(filename: String): LiveData<Bitmap> {
        val mediatorLiveData = MediatorLiveData<Bitmap>()

        executor.execute {
            val fileInput = context.openFileInput(filename)
            val bitmap = BitmapFactory.decodeStream(fileInput)
            mediatorLiveData.postValue(bitmap)
        }
        return mediatorLiveData
    }

    fun saveQuestion(question: Question, bitmap: Bitmap) {
        executor.execute {
            val key = questionsDao.saveQuestion(question)
            val bitmapFilename = "${key}.jpg"
            questionsDao.saveQuestion(question.copy(key = key.toInt(), image = bitmapFilename))
            val fileOutput = context.openFileOutput(bitmapFilename, Context.MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutput)
        }
    }

    fun updateQuestion(question: Question) {
        executor.execute { questionsDao.saveQuestion(question) }
    }

    fun removeQuestion(question: Question) {
        executor.execute { questionsDao.removeQuestion(question) }
    }
}



