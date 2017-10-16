package joneros.jenny.quizzit


import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_new_question.*
import android.widget.Toast
import java.util.regex.Pattern


/**
 * A simple [Fragment] subclass.
 */

class NewQuestionFragment : Fragment() {

    companion object {
        val REQUEST_CODE_TAKE_PICTURE = 101
        val PICK_IMAGE = 102
        val MAX_SIZE = 500000
    }

    var savePhoto: Bitmap? = null
    var questionKey = 0
    var groupId = 0
    var question = Question(0, "", "", "", 0)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater?.inflate(R.layout.fragment_new_question, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var viewModelQ = ViewModelProviders.of(activity).get(QuestionViewModel::class.java)
        groupId = arguments.getInt("groupId")
        questionKey = arguments.getInt("questionKey")

        viewModelQ.loadQuestion(questionKey).observe(this, Observer {
            if (it != null) {
                question = it

                viewModelQ.openBitmap(question.image).observe(this, Observer {
                    savePhoto = it
                    image_NewQuestion.setImageBitmap(savePhoto)
                })
                etxt_answer.text.clear()
                etxt_answer.text.append(question.answer)
                etxt_questionType.text.clear()
                etxt_questionType.text.append(question.question)
                groupId = question.groupname

            } else {
                questionKey = 0
            }
        })

        btn_save.setOnClickListener {
            if (savePhoto != null) {
                if ((etxt_answer.text.toString()).matches((".*\\w.*").toRegex()) && (etxt_questionType.text.toString()).matches((".*\\w.*").toRegex())) {
                    val pattern = Pattern.compile("\\s")
                    val matcher = pattern.matcher(etxt_answer.text.toString())
                    if (!matcher.find()) {
                        if (etxt_answer.text.toString().toCharArray().size <= 14) {
                            val questiontype = etxt_questionType.text.toString()
                            val answer = etxt_answer.text.toString()
                            val updatedQuestion = Question(questionKey, questiontype, answer, savePhoto.toString()!!, groupId)
                            viewModelQ.saveQuestion(updatedQuestion, savePhoto!!)
                            fragmentManager.popBackStack()
                        } else {
                            Toast.makeText(activity, getString(R.string.t_moreThenTenChars), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(activity, (getString(R.string.t_noBlankspaces)), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, getString(R.string.t_noAnwerOrQuestion), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(activity, R.string.t_noPicture, Toast.LENGTH_SHORT).show()
            }
        }

        btn_delete.setOnClickListener {
            viewModelQ.removeQuestion(question)
            fragmentManager.popBackStack()
        }

        cameraButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PICTURE)
        }

        btn_gallery.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            processTakenPicture(data ?: Intent())
        }

        if (resultCode === Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            processChoosenPicture(data ?: Intent())
        }
    }

    private fun processTakenPicture(data: Intent?) {
        val extras = data?.extras
        var photo = extras?.get("data") as Bitmap
        if (photo.byteCount > MAX_SIZE) {
            photo = rescalePhoto(photo)!!
        }
        image_NewQuestion.setImageBitmap(photo)
        savePhoto = photo
    }

    private fun processChoosenPicture(data: Intent?) {
        val imageUri = data?.getData()
        val bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri)
        image_NewQuestion.setImageBitmap(bitmap)
        savePhoto = bitmap
    }

    private fun rescalePhoto(bitmap: Bitmap?): Bitmap? {
        val newHeight = 256
        val newWidth = 256
        bitmap?.reconfigure(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        return bitmap
    }

}// Required empty public constructor


