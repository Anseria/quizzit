package joneros.jenny.quizzit

import android.arch.lifecycle.MediatorLiveData
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Created by thomas on 2017-11-26.
 */

data class FirebaseQuestion(val key: Int,
                            val question: String,
                            val answer: String,
                            val image: String,
                            val groupname: Int,
                            val groupFBid: String)

class QuestionRepository {

    companion object {
        val TAG = "QuestionRepository"
    }

    private val database = FirebaseDatabase.getInstance()
    private val questionsRef = database.getReference("questions")
    val allQuestions: MediatorLiveData<List<Question>> = MediatorLiveData()

    private val valueEventListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
            Log.d(TAG, "Error in Firebase communication")
        }

        override fun onDataChange(snapshot: DataSnapshot?) {
            val latestQuestions = snapshot?.children?.map {
                val firebaseQuestion = it.getValue(FirebaseQuestion::class.java)
                return@map Question(firebaseQuestion?.key ?: 0,
                        it.key,
                        firebaseQuestion?.question ?: "",
                        firebaseQuestion?.answer ?: "",
                        firebaseQuestion?.image ?: "",
                        firebaseQuestion?.groupname ?: 0,
                        firebaseQuestion?.groupFBid ?: "")
            } ?: emptyList()

            allQuestions.postValue(latestQuestions)
        }
    }

    fun startListening() {
        questionsRef.addValueEventListener(valueEventListener)
    }

    fun stopListening() {
        questionsRef.removeEventListener(valueEventListener)
    }

    fun loadQuestion(FBid: String): MediatorLiveData<Question> {
        val mediatorLiveData = MediatorLiveData<Question>()
        if (FBid != "") {
            questionsRef.child(FBid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    Log.d(TAG, "Error in Firebase communication")
                }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    val firebaseQuestion = snapshot?.getValue(FirebaseQuestion::class.java)
                    val question = Question(0,
                            snapshot?.key ?: "",
                            firebaseQuestion?.question ?: "",
                            firebaseQuestion?.answer ?: "",
                            firebaseQuestion?.image ?: "",
                            firebaseQuestion?.groupname ?: 0,
                            firebaseQuestion?.groupFBid ?: "")
                    mediatorLiveData.postValue(question)

                }
            })
        } else {
            mediatorLiveData.postValue(Question(0, "", "", "", "", 0,""))
        }

        return MediatorLiveData()
    }

    fun saveQuestion(question: Question): String {
        val firebaseQuestion = FirebaseQuestion(question.key,
                question.question,
                question.answer,
                question.image,
                question.groupname,
                question.groupFBid)
        var FBid = ""
        when (question.FBid) {
            "" -> {
                val newRef = questionsRef.push()
                FBid = newRef.key
                newRef.setValue(firebaseQuestion).addOnCompleteListener {
                    Log.d(TAG, "Completed; ${it.isSuccessful}")
                }
            }
            else -> {
                FBid = question.FBid
                questionsRef.child(FBid).setValue(firebaseQuestion)
            }
        }
        return FBid
    }

    fun removeQuestion(question: Question) {
        if (question.FBid != "") {
            questionsRef.child(question.FBid).removeValue()
        }
    }
}