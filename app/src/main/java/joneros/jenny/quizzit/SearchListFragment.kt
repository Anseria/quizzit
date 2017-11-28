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
import kotlinx.android.synthetic.main.fragment_search_list.*


/**
 * A simple [Fragment] subclass.
 */
class SearchListFragment : Fragment() {

    companion object {
        val TAG = "SearchListFragment"
    }

    var mAuth : FirebaseAuth? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_search_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            Log.d(TAG, "NOT SIGNED IN")
        } else {
            Log.d(TAG, "signed in, uid is ${mAuth!!.currentUser?.uid}")
        }

        searchListView.adapter = SearchGroupAdapter()
        searchListView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val viewModel = ViewModelProviders.of(activity).get(GroupViewModel::class.java)

        viewModel.loadAllFirebaseGroups().observe(this, Observer {
            (searchListView.adapter as SearchGroupAdapter).groups = it ?: emptyList()
            searchListView.adapter.notifyDataSetChanged()
        })
    }

    class SearchGroupAdapter : RecyclerView.Adapter<SearchGroupViewHolder>() {
        var groups: List<Group> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SearchGroupViewHolder {
            val itemView = LayoutInflater
                    .from(parent?.context)
                    .inflate(R.layout.group_item, parent, false)
            return SearchGroupViewHolder(itemView)
        }

        override fun getItemCount(): Int = groups.size

        override fun onBindViewHolder(holder: SearchGroupViewHolder?, position: Int) {
            val group = groups[position]
            val title = group.name
            holder?.groupName?.text = title
        }
    }

    class SearchGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupName: TextView = itemView.findViewById(R.id.group_name)

    }
}

// Required empty public constructor
