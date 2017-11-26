package joneros.jenny.quizzit


import android.arch.lifecycle.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_create_game.*
import kotlinx.android.synthetic.main.fragment_start.*

/**
 * A simple [Fragment] subclass.
 */
class CreateGameFragment : Fragment() {

    companion object {
        val TAG = "CreateGameFragment"
    }

    var groupKey: Int = 0
    var group = Group(0,"", "", 0, "", "")
    var questions = emptyList<Question>()
    var groupFBid = ""
    var userFBid = ""
    var mAuth : FirebaseAuth? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_create_game, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            userFBid = ""
        } else {
            userFBid = mAuth!!.currentUser!!.uid
        }

        val questionViewModel = ViewModelProviders.of(activity).get(QuestionViewModel::class.java)

        questionList.adapter = QuestionAdapter()
        questionList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val viewModel = ViewModelProviders.of(activity).get(GroupViewModel::class.java)
        val groupId = arguments.getInt("groupId")
        if (groupId == 0) {
            btn_deleteGroup.visibility = View.INVISIBLE
            btn_addNewQuestion.visibility = View.INVISIBLE
        }
        viewModel.loadGroup(groupId).observe(this, Observer {
            group = it ?: Group(0,"", "", 0, "", "")
            groupKey = group.key
            groupFBid = group.FBid
            etxt_groupName.text.clear()
            etxt_groupName.text.append(group.name)
            etxt_description.text.clear()
            etxt_description.text.append(group.description)

            questionViewModel.loadQuestionsByGroup(groupKey).observe(this, Observer {
                questions = it ?: emptyList()
                (questionList.adapter as CreateGameFragment.QuestionAdapter).questions = it ?: emptyList()
                questionList.adapter.notifyDataSetChanged()
            })

        })

        btn_addNewQuestion.setOnClickListener {
            val newQuestionFragment = NewQuestionFragment()
            val arguments = Bundle()
            arguments.putInt("groupId", groupKey)
            newQuestionFragment.arguments = arguments

            fragmentManager
                    .beginTransaction()
                    .replace(R.id.activityMain_containerLayout, newQuestionFragment)
                    .addToBackStack(null)
                    .commit()
        }

        btn_saveGroup.setOnClickListener {
            val groupName = etxt_groupName.text.toString()
            val groupDescription = etxt_description.text.toString()
            val updatedGroup = Group(groupKey, groupFBid, groupName, 0, groupDescription, userFBid)
            viewModel.saveGroup(updatedGroup)
            fragmentManager.popBackStack()
        }

        btn_shareGroup.setOnClickListener {
        }

        btn_deleteGroup.setOnClickListener {
            for (i in 0..questions.size - 1) {
                questionViewModel.removeQuestion(questions[i])
            }
            viewModel.removeGroup(group)
            fragmentManager.popBackStack()
        }
    }


    inner class QuestionAdapter : RecyclerView.Adapter<QuestionViewHolder>() {


        var questions: List<Question> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): QuestionViewHolder {
            val itemview = LayoutInflater
                    .from(parent?.context)
                    .inflate(R.layout.question_in_game_item, parent, false)
            return QuestionViewHolder(itemview)
        }

        override fun getItemCount(): Int = questions.size

        override fun onBindViewHolder(holder: QuestionViewHolder?, position: Int) {

            val questionViewModel = ViewModelProviders.of(activity).get(QuestionViewModel::class.java)

            val currentQuestion = questions[position]

            questionViewModel.openBitmap(currentQuestion.image).observe(this@CreateGameFragment, Observer {
                val questionImage = it
                holder?.image?.setImageBitmap(questionImage)
                holder?.answer?.text = currentQuestion.answer
                holder?.questionKey = currentQuestion.key
                Log.d(TAG, "hejhej")
            }
            )
        }
    }

    class QuestionViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview), View.OnClickListener {

        val answer: TextView = itemview.findViewById(R.id.question_inlist_item)
        val image: ImageView = itemview.findViewById(R.id.image_inlist_item)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val newQuestionFragment = NewQuestionFragment()
            val arguments = Bundle()
            arguments.putInt("questionKey", questionKey)
            newQuestionFragment.arguments = arguments

            (view?.context as FragmentActivity)
                    .supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.activityMain_containerLayout, newQuestionFragment)
                    .addToBackStack(null)
                    .commit()
        }

        var questionKey = -1
    }

}// Required emmpty public constructor
