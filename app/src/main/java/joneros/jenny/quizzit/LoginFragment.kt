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
import kotlinx.android.synthetic.main.fragment_login.*


/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    companion object {
        val TAG = "LoginFragment"
    }

    var mAuth : FirebaseAuth? = null
    var currentUser = this.mAuth?.currentUser

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            val email = usernameText.text.toString()
            val password = passwordText.text.toString()
            signIn(email, password)
        }

        createAccountButton.setOnClickListener {
            val email = usernameText.text.toString()
            val password = passwordText.text.toString()
            createNewUser(email, password)
        }
    }


    fun createNewUser(email: String, password: String) {
        Log.d(TAG, "email is $email")
        Log.d(TAG, "password is $password")
        if(this.mAuth == null) {
        } else {
            this.mAuth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "successfully created user")
                            signIn(email, password)
                        } else {
                            Log.d(TAG, "failed created user, with exception ${task.exception}")
                        }

                    }
        }
    }

    fun signIn(email: String, password: String) {
        if(this.mAuth == null) {
        } else {
            this.mAuth!!.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener{ task ->
                        if( task.isSuccessful){
                            Log.d(TAG, "successfully signed in")
                            goToStartFragment()
                        }
                        else{
                            Log.d(TAG, "failed signing in")
                        }
                    }
        }
    }

    fun goToStartFragment() {
        fragmentManager
                .beginTransaction()
                .replace(R.id.activityMain_containerLayout, StartFragment())
                .commit()
        Log.d(TAG, "going to startfragment")
    }

    override fun onStart() {
        super.onStart()

        if(currentUser == null) {
            currentUser = mAuth?.currentUser
        }
    }


}// Required empty public constructor
