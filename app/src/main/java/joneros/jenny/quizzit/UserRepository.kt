package joneros.jenny.quizzit

import android.arch.lifecycle.MediatorLiveData
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Created by thomas on 2017-11-19.
 */

data class FirebaseUser(val name: String = "")

class UserRepository {

    companion object {
        val TAG = "UserRepository"
    }

    val allUsers: MediatorLiveData<List<User>> = MediatorLiveData()
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    private val valueEventListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
            Log.d(TAG, "Error in Firebase communication")
        }

        override fun onDataChange(snapshot: DataSnapshot?) {
            val latestUsers = snapshot?.children?.map {
                val firebaseUser = it.getValue(FirebaseUser::class.java)
                return@map User(it.key,
                        firebaseUser?.name ?: "")
            } ?: emptyList()

            allUsers.postValue(latestUsers)
        }
    }

    fun startListening() {
        usersRef.addValueEventListener(valueEventListener)
    }

    fun stopListening() {
        usersRef.removeEventListener(valueEventListener)
    }

    fun loadUser(FBid: String): MediatorLiveData<User> {
        val mediatorLiveData = MediatorLiveData<User>()
        if (FBid != "") {
            usersRef.child(FBid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    Log.d(TAG, "Error in Firebase communication")
                }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    val firebaseUser = snapshot?.getValue(FirebaseUser::class.java)
                    val user = User(snapshot?.key ?: "",
                            firebaseUser?.name ?: "")
                    mediatorLiveData.postValue(user)

                }
            })
        } else {
            mediatorLiveData.postValue(User("", ""))
        }

        return MediatorLiveData()
    }

    fun saveUser(user: User) {
        val firebaseUser = FirebaseUser(user.name)
        when (user.FBid) {
            "" -> {
                usersRef.push().setValue(firebaseUser).addOnCompleteListener {
                    Log.d(TAG, "Completed; ${it.isSuccessful}")
                }
            }
            else -> usersRef.child(user.FBid).setValue(firebaseUser)
        }
    }

    fun removeContact(user: User) {
        if (user.FBid != "") {
            usersRef.child(user.FBid).removeValue()
        }
    }
}