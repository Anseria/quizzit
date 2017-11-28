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

data class FirebaseGroup(val key: Int = 0,
                         val name: String = "",
                         val max_score: Int = 0,
                         val description: String = "",
                         val authorFBid: String = "")

class GroupRepository {

    companion object {
        val TAG = "GroupRepository"
    }

    private val database = FirebaseDatabase.getInstance()
    private val groupsRef = database.getReference("groups")
    val allGroups: MediatorLiveData<List<Group>> = MediatorLiveData()

    private val valueEventListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
            Log.d(TAG, "Error in Firebase communication")
        }

        override fun onDataChange(snapshot: DataSnapshot?) {
            Log.d(TAG, "the data is $snapshot")
            val latestGroups = snapshot?.children?.map {
                val firebaseGroup = it.getValue(FirebaseGroup::class.java)
                return@map Group(firebaseGroup?.key ?: 0,
                        it.key,
                        firebaseGroup?.name ?: "",
                        firebaseGroup?.max_score ?: 0,
                        firebaseGroup?.description ?: "",
                        firebaseGroup?.authorFBid ?: "")
            } ?: emptyList()

            allGroups.postValue(latestGroups)
        }
    }

    fun startListening() {
        groupsRef.addValueEventListener(valueEventListener)
    }

    fun stopListening() {
        groupsRef.removeEventListener(valueEventListener)
    }

    fun loadAllGroupsExceptMyOwn(FBid: String): MediatorLiveData<List<Group>> {

        val mediatorLiveData = MediatorLiveData<List<Any>>()
        groupsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, "Error in Firebase communication")
            }

            override fun onDataChange(snapshot: DataSnapshot?) {

                val latestGroups = snapshot?.children?.map {
                    val firebaseGroup = it.getValue(FirebaseGroup::class.java)
                    if (firebaseGroup?.authorFBid == FBid) {}
                    else {
                        return@map Group(firebaseGroup?.key ?: 0,
                                it.key,
                                firebaseGroup?.name ?: "",
                                firebaseGroup?.max_score ?: 0,
                                firebaseGroup?.description ?: "",
                                firebaseGroup?.authorFBid ?: "")
                    }
                } ?: emptyList()
                Log.d(TAG, "latestgroups är $latestGroups")
                mediatorLiveData.postValue(latestGroups)

                /*val listOfFirebaseGroups = mutableListOf<Group>()
                for (i in snapshot!!.children) {
                    val firebaseGroup = i?.getValue(FirebaseGroup::class.java)
                    if(firebaseGroup?.authorFBid != FBid) {
                        val returningFirebaseGroup = Group(firebaseGroup?.key ?: 0,
                                i.key ?: "",
                                firebaseGroup?.name ?: "",
                                firebaseGroup?.max_score ?: 0,
                                firebaseGroup?.description ?: "",
                                firebaseGroup?.authorFBid ?: "")
                        Log.d(TAG, "firebasegroup is $returningFirebaseGroup")
                        listOfFirebaseGroups.add(returningFirebaseGroup)
                    }
                }
                val listOfGroup = listOf<Group>()

                Log.d(TAG, "List of groups contains: $listOfFirebaseGroups")
                mediatorLiveData.postValue(listOfFirebaseGroups)
                Log.d(TAG, "mediator is $mediatorLiveData")*/
            }
        })
        Log.d(TAG, "returning")
        return MediatorLiveData()
    }

    fun loadGroup(FBid: String): MediatorLiveData<Group> {
        val mediatorLiveData = MediatorLiveData<Group>()
        if (FBid != "") {
            groupsRef.child(FBid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    Log.d(TAG, "Error in Firebase communication")
                }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    val firebaseGroup = snapshot?.getValue(FirebaseGroup::class.java)
                    val group = Group(0,
                            snapshot?.key ?: "",
                            firebaseGroup?.name ?: "",
                            firebaseGroup?.max_score ?: 0,
                            firebaseGroup?.description ?: "",
                            firebaseGroup?.authorFBid ?: "")
                    mediatorLiveData.postValue(group)

                }
            })
        } else {
            mediatorLiveData.postValue(Group(0, "", "", 0, "", ""))
        }

        return MediatorLiveData()
    }

    fun saveGroup(group: Group): String {
        val firebasegroup = FirebaseGroup(0,
                group.name,
                group.max_score,
                group.description,
                group.authorFBid)
        var FBid = ""
        when (group.FBid) {
            "" -> {
                val newRef = groupsRef.push()
                FBid = newRef.key
                newRef.setValue(firebasegroup).addOnCompleteListener {
                    Log.d(TAG, "Completed; ${it.isSuccessful}")
                }
            }
            else -> {
                FBid = group.FBid
                groupsRef.child(FBid).setValue(firebasegroup)
            }
        }
        return FBid
    }

    fun removeGroup(group: Group) {
        if (group.FBid != "") {
            groupsRef.child(group.FBid).removeValue()
        }
    }
}
