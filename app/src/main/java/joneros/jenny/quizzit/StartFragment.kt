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
import kotlinx.android.synthetic.main.fragment_group_list.*
import kotlinx.android.synthetic.main.fragment_start.*
import kotlinx.android.synthetic.main.group_item.*


/**
 * A simple [Fragment] subclass.
 */
class StartFragment : Fragment() {

    companion object {
        val TAG = "StartFragment"
    }

    var userFBid = ""

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        Log.d(TAG, "my FBid is $userFBid")
        playButton.isEnabled = false

        val settingsViewModel = ViewModelProviders.of(activity).get(SettingsViewModel::class.java)
        val usersViewModel = ViewModelProviders.of(activity).get(UserViewModel::class.java)

        settingsViewModel.allSettings.observe(this, Observer {
            if(it?.userFBid == "" || it?.userFBid == null) {
                val tempUser = User("","")
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
        })

        playButton.setOnClickListener{
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.activityMain_containerLayout, GroupListFragment())
                    .addToBackStack(null)
                    .commit()
        }
    }

}// Required empty public constructor
