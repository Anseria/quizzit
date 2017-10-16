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
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_group_list.*
import kotlinx.android.synthetic.main.group_item.*

/**
 * A simple [Fragment] subclasskk.
 */
class GroupListFragment : LifecycleFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_group_list, container, false)

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            R.id.editGroup -> {
                if (!editMode.editMode) {
                    editMode.editMode = true
                    item.icon = ContextCompat.getDrawable(context, R.drawable.ic_done_black_24dp)
                    item.title = "Done"
                    item_edit_sign?.visibility = View.VISIBLE
                    recView_groups.adapter.notifyDataSetChanged()
                } else {
                    editMode.editMode = false
                    item.icon = ContextCompat.getDrawable(context, R.drawable.ic_edit_black_24dp)
                    item.title = "Edit"
                    item_edit_sign?.visibility = View.GONE
                    recView_groups.adapter.notifyDataSetChanged()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        recView_groups.adapter = GroupAdapter()
        recView_groups.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val viewModel = ViewModelProviders.of(activity).get(GroupViewModel::class.java)

        viewModel.allGroups.observe(this, Observer {
            (recView_groups.adapter as GroupAdapter).groups = it ?: emptyList()
            recView_groups.adapter.notifyDataSetChanged()
        })


        addGroup.setOnClickListener {
            val createGameFragment = CreateGameFragment()
            val arguments = Bundle()
            arguments.putInt("groupId", 0)
            createGameFragment.arguments = arguments
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.activityMain_containerLayout, createGameFragment)
                    .addToBackStack(null)
                    .commit()
        }
    }

    class GroupAdapter : RecyclerView.Adapter<GroupViewHolder>() {
        var groups: List<Group> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): GroupViewHolder {
            val itemView = LayoutInflater
                    .from(parent?.context)
                    .inflate(R.layout.group_item, parent, false)
            return GroupViewHolder(itemView)
        }

        override fun getItemCount(): Int = groups.size

        override fun onBindViewHolder(holder: GroupViewHolder?, position: Int) {
            val group = groups[position]
            val title = group.name
            holder?.groupName?.text = title
            holder?.groupId = group.key
            holder?.maxscore?.text = group.max_score.toString()

            if (editMode.editMode && groups.size != 0) {
                holder?.editSign?.visibility = View.VISIBLE
            } else {
                holder?.editSign?.visibility = View.GONE
            }
        }
    }

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val groupName: TextView = itemView.findViewById(R.id.group_name)
        val maxscore: TextView = itemView.findViewById(R.id.group_maxScore)
        val editSign: ImageView = itemView.findViewById(R.id.item_edit_sign)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            if (!editMode.editMode) {
                val gameFragment = GameFragment()
                val arguments = Bundle()
                arguments.putInt("groupId", groupId)
                gameFragment.arguments = arguments
                editMode.editMode = false

                (view?.context as FragmentActivity)
                        .supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.activityMain_containerLayout, gameFragment)
                        .addToBackStack(null)
                        .commit()
            } else {
                val createGameFragment = CreateGameFragment()
                val arguments = Bundle()
                arguments.putInt("groupId", groupId)
                createGameFragment.arguments = arguments
                editMode.editMode = false

                (view?.context as FragmentActivity)
                        .supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.activityMain_containerLayout, createGameFragment)
                        .addToBackStack(null)
                        .commit()
            }
        }

        var groupId: Int = -1
    }
}

object editMode {
    var editMode = false
}
