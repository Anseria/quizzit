package joneros.jenny.quizzit


import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_group_list.*
import kotlinx.android.synthetic.main.fragment_start.*
import kotlinx.android.synthetic.main.group_item.*
import android.widget.Toast
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import android.R.attr.password
import android.R.attr.password

/**
 * A simple [Fragment] subclass.
 */
class StartFragment : Fragment() {

    companion object {
        val TAG = "StartFragment"
    }

    //var userFBid = ""
    var mAuth: FirebaseAuth? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playButton.isEnabled = false

        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            Log.d(TAG, "going to loginfragment")
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.activityMain_containerLayout, LoginFragment())
                    .commit()
        } else {
            playButton.isEnabled = true
            Log.d(TAG, "signed in, uid is ${mAuth!!.currentUser?.uid}")

        }

        /*val settingsViewModel = ViewModelProviders.of(activity).get(SettingsViewModel::class.java)
        val usersViewModel = ViewModelProviders.of(activity).get(UserViewModel::class.java)

        settingsViewModel.allSettings.observe(this, Observer {
            if (it?.userFBid == "" || it?.userFBid == null) {
                val tempUser = User("", "")
                val newFBid = usersViewModel.saveUserToFirebase(tempUser)
                userFBid = newFBid
                val newSettings = MySettings(userFBid)
                settingsViewModel.saveSettings(newSettings)
                playButton.isEnabled = true
                Log.d(TAG, "my FBid aaa is $userFBid")
            } else {
                userFBid = it.userFBid
                playButton.isEnabled = true
                Log.d(TAG, "my FBid bbb is ${it.userFBid}")
            }
            Log.d(TAG, "my FBid ccc is $userFBid")
        })*/

        settingsButton.setOnClickListener {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.activityMain_containerLayout, SetSettingsFragment())
                    .addToBackStack(null)
                    .commit()
        }

        playButton.setOnClickListener {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.activityMain_containerLayout, GroupListFragment())
                    .addToBackStack(null)
                    .commit()
        }

        editButton.setOnClickListener {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.activityMain_containerLayout, EditListFragment())
                    .addToBackStack(null)
                    .commit()
        }

        searchButton.setOnClickListener {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.activityMain_containerLayout, SearchListFragment())
                    .addToBackStack(null)
                    .commit()
        }

        signoutButton.setOnClickListener {
            mAuth?.signOut()
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.activityMain_containerLayout, LoginFragment())
                    .commit()
        }
    }
}// Required empty public constructor
