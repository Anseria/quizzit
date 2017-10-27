package joneros.jenny.quizzit


import android.animation.Animator
import android.animation.TimeInterpolator
import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.fragment_game.*
import android.widget.Toast


@Suppress("DEPRECATION")
/**
 * A simple [Fragment] subclass.mm*/

class GameFragment : LifecycleFragment() {

    companion object {
        val TAG = "GameFragment"
    }

    val gamecontroller = GameController()
    var arrayOfLetters: CharArray? = null
    var groupKey: Int = 0
    var round = 0
    val arrayButtonsToPress = mutableListOf<Button>()
    val isThePressButtonsMoving = mutableListOf<Boolean>()
    val arrayButtonsToSee = mutableListOf<Button>()
    val isTheSeeButtonsMoving = mutableListOf<Boolean>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater!!.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonPressCollection = arrayOf(first, first2, first3, first4, first5, first6, first7, first8, first9, first10, first11, first12, first13, first14)
        val buttonSeeCollection = arrayOf(set1, set2, set3, set4, set5, set6, set7, set8, set9, set10, set11, set12, set13, set14)

        arrayButtonsToPress.addAll(buttonPressCollection)
        arrayButtonsToSee.addAll(buttonSeeCollection)
        for (i in 0..arrayButtonsToPress.size - 1) {
            isThePressButtonsMoving.add(false)
        }
        for (i in 0..arrayButtonsToSee.size - 1) {
            isTheSeeButtonsMoving.add(false)
        }

        val questionViewModel = ViewModelProviders.of(activity).get(QuestionViewModel::class.java)
        groupKey = arguments.getInt("groupId")

        questionViewModel.loadQuestionsByGroup(groupKey).observe(this, Observer {
            gamecontroller.questions = it ?: emptyList()
            if (gamecontroller.questions.size == 0) {
                Toast.makeText(activity, "No Questions in this group", Toast.LENGTH_SHORT).show()
            } else {
                setNewQuestion()
                setListeners()
            }
        })
        txtv_score.text = gamecontroller.score.toString() + "p"
    }

    fun setUpButtons() {
        for (i in 0..arrayButtonsToPress.size - 1) {
            arrayButtonsToPress[i].visibility = VISIBLE
        }
        for (i in 0..arrayButtonsToSee.size - 1) {
            arrayButtonsToSee[i].visibility = View.INVISIBLE
            arrayButtonsToSee[i].background = ContextCompat.getDrawable(context, R.drawable.letter_button)
            arrayButtonsToSee[i].alpha = 0.5f
            arrayButtonsToSee[i].translationY = 0f
        }
    }

    fun checkAnswer() {
        if (gamecontroller.checkTheAnswer()) {
            round++
            removeNonChosenLetters()
            for (i in 0..gamecontroller.question!!.answer.length - 1) {
                val button = arrayButtonsToSee[i]
                if (gamecontroller.points != 0) {
                    button.background = ContextCompat.getDrawable(context, R.drawable.letter_button_green)
                    button.setOnClickListener(null)
                    letTheButtonsJump()
                }
                clueButton.setOnClickListener(null)
            }
            if (round == gamecontroller.questions.size) {
                val groupViewModel = ViewModelProviders.of(activity).get(GroupViewModel::class.java)
                groupViewModel.loadGroup(groupKey).observe(this, Observer {
                    var group: Group = it!!
                    val testScore = gamecontroller.score.toString()
                    Toast.makeText(activity, testScore, Toast.LENGTH_SHORT).show()
                    if (group.max_score < gamecontroller.score) {
                        val updatedGroup = Group(groupKey, group.name, gamecontroller.score, group.description)
                        groupViewModel.saveGroup(updatedGroup)
                    }
                    Handler().postDelayed({
                        fragmentManager.popBackStack() }, 1500)
                })
            }
            txtv_score.text = gamecontroller.score.toString() + "p"
            if (round < gamecontroller.questions.size) {
                Handler().postDelayed({ startNewGame() }, 1000)
            }

        }
    }

    fun letTheButtonsJump() {
        val amountOfLetters = gamecontroller.question!!.answer.length - 1
        val delayLength = 200
        if (amountOfLetters != 0) {
            400 / amountOfLetters
        }
        for (i in 0..amountOfLetters) {
            val a = i * delayLength.toLong()
            arrayButtonsToSee[i].animate()
                    .translationY(-20f)
                    .setInterpolator(object : TimeInterpolator {
                        override fun getInterpolation(p0: Float): Float {
                            return Math.sin(p0 * Math.PI).toFloat()
                        }
                    })
                    .setStartDelay(a)
        }
    }

    fun startNewGame() {
        gamecontroller.clearThings()
        setUpButtons()
        setNewQuestion()
        setListeners()
    }

    fun setNewQuestion(): Unit {
        gamecontroller.getNewQuestion()
        arrayOfLetters = gamecontroller.getLetters()
        gamecontroller.identifyIncorrectLetters()
        gamecontroller.timesToSackLetters(gamecontroller.question!!.answer.length)
        myPointsTextView.text = gamecontroller.points.toString() + "p"
        val questionViewModel = ViewModelProviders.of(activity).get(QuestionViewModel::class.java)
        questionViewModel.openBitmap(gamecontroller.question!!.image).observe(this, Observer {
            questionImageView.setImageBitmap(it)
        })
        tvWhoIsThis.text = gamecontroller.question!!.question
        setLetters()
        prepareForAnswer()
    }

    fun prepareForAnswer() {
        val amountofletters = gamecontroller.question!!.answer.length - 1
        for (i in 0..amountofletters) {
            val buttonInPlay = arrayButtonsToSee[i]
            buttonInPlay.background = ContextCompat.getDrawable(context, R.drawable.letter_button_shadow)
            buttonInPlay.visibility = VISIBLE
            buttonInPlay.text = ""
            for (j in 0..gamecontroller.spacePositionsInAnswer.size - 1) {
                if (i == gamecontroller.spacePositionsInAnswer[j]) {
                    buttonInPlay.visibility = View.INVISIBLE
                }
            }
        }
    }

    fun giveClueAfterMovingButton(number: Int) {
        myPointsTextView.animate()
                .alpha(1f)
                .setInterpolator(object : TimeInterpolator {
                    override fun getInterpolation(p0: Float): Float {
                        return Math.sin(p0 * Math.PI / 2).toFloat()
                    }
                })
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        giveLetter(number)
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationStart(p0: Animator?) {
                    }
                })
    }

    fun removeIncorrectLettersAfterMovingButton() {
        myPointsTextView.animate()
                .alpha(1f)
                .setInterpolator(object : TimeInterpolator {
                    override fun getInterpolation(p0: Float): Float {
                        return Math.sin(p0 * Math.PI / 2).toFloat()
                    }
                })
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        removeIncorrectLetters()
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationStart(p0: Animator?) {
                    }
                })
    }

    fun giveCorrectAnswerAfterMovingButton(startLetter: Int) {
        myPointsTextView.animate()
                .alpha(1f)
                .setInterpolator(object : TimeInterpolator {
                    override fun getInterpolation(p0: Float): Float {
                        return Math.sin(p0 * Math.PI / 2).toFloat()
                    }
                })
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        giveCorrectAnswer(startLetter)
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationStart(p0: Animator?) {
                    }
                })
    }

    fun prepareToGiveClue() {
        val mustMoveBack = gamecontroller.weMustMoveLettersBack()
        val clueIndex = gamecontroller.whichClue()
        val lengthOfAnswer = gamecontroller.question!!.answer.length
        val timesToSackLetters = gamecontroller.timesToSackLetters(lengthOfAnswer)
        myPointsTextView.text = gamecontroller.points.toString() + "p"
        var lettersMovingBack = 0
        if ((5 - clueIndex) - timesToSackLetters > 0) {
            lettersMovingBack = (5 - clueIndex) - timesToSackLetters
        }
        val amountOfLettersGivenWhenGivingWholeAnswer = 4 - timesToSackLetters
        if (clueIndex == 1 && !mustMoveBack) {
            giveCorrectAnswer(amountOfLettersGivenWhenGivingWholeAnswer)
        } else if (clueIndex == 1 && mustMoveBack) {
            moveButtonBack(lettersMovingBack)
            giveCorrectAnswerAfterMovingButton(amountOfLettersGivenWhenGivingWholeAnswer)
        } else {
            if (5 - clueIndex < timesToSackLetters) {
                if (!mustMoveBack) {
                    removeIncorrectLetters()
                } else {
                    moveButtonBack(lettersMovingBack)
                    removeIncorrectLettersAfterMovingButton()
                }
            } else {
                if (!mustMoveBack) {
                    giveLetter((5 - clueIndex) - timesToSackLetters)
                } else {
                    moveButtonBack(lettersMovingBack)
                    giveClueAfterMovingButton((5 - clueIndex) - timesToSackLetters)
                }
            }
        }
    }

    private fun giveCorrectAnswer(startLetter: Int) {
        for (i in startLetter..gamecontroller.question!!.answer.length - 1) {
            giveLetter(i)
            clueButton.setOnClickListener(null)
        }
    }

    fun giveLetter(number: Int) {
        val nextLetter = gamecontroller.identifyCorrectLetter(number)
        moveButton(nextLetter, true)
    }

    fun removeIncorrectLetters() {
        val incorrectLetters = gamecontroller.listOfIncorrectLetters
        val lengthOfAnswer = gamecontroller.question!!.answer.length
        val totalAmountOfIncorrectLetters = gamecontroller.listOfIncorrectLetters.size - 1
        val a = gamecontroller.incorrectLettersToBeSacked(totalAmountOfIncorrectLetters, lengthOfAnswer)
        for (i in 0..a) {
            sackButton(arrayButtonsToPress[incorrectLetters[i]])
        }
        for (i in 0..a) {
            gamecontroller.listOfIncorrectLetters.removeAt(a - i)
        }
    }

    private fun removeNonChosenLetters() {
        val nonChosenLetters = gamecontroller.identifyNonChosenLetters()
        for (i in 0..nonChosenLetters.size - 1) {
            sackButton(arrayButtonsToPress[nonChosenLetters[i]])
        }
    }

    private fun sackButton(button: Button) {
        button.animate()
                .translationY(500f)
                .setInterpolator(object : TimeInterpolator {
                    override fun getInterpolation(p0: Float): Float {
                        return Math.sin(p0 * Math.PI / 2).toFloat()
                    }
                })
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        button.visibility = View.INVISIBLE
                        button.translationY = 0f
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationStart(p0: Animator?) {
                    }
                })
    }

    fun moveButton(buttonNumber: Int, isClue: Boolean) {
        val maxButton = gamecontroller.question!!.answer.length - 1

        val numberOfSelectedButtons = gamecontroller.selectedButtonsAsInts.size
        for (i in 0..maxButton) {
            if (numberOfSelectedButtons == i) {
                if (isClue == true) {
                    animateButtonMove(buttonNumber, i, true, true)
                } else {
                    animateButtonMove(buttonNumber, i, true, false)
                }
                gamecontroller.selectedButtonsAsInts += buttonNumber
            }
        }
    }

    fun animateButtonUp(buttonNumber: Int, targetButtonNumber: Int, isClue: Boolean) {
        val view = arrayButtonsToPress[buttonNumber]
        val targetView = arrayButtonsToSee[targetButtonNumber]
        isThePressButtonsMoving[buttonNumber] = true
        view.animate()
                .translationX(targetView.x - view.x - targetView.width / 2)
                .translationY(targetView.y - view.y - targetView.height / 2)
                .scaleX(1 / 2f)
                .scaleY(1 / 2f)
                .setInterpolator(object : TimeInterpolator {
                    override fun getInterpolation(p0: Float): Float {
                        return Math.sin(p0 * Math.PI / 2).toFloat()
                    }
                })
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {

                        isThePressButtonsMoving[buttonNumber] = false
                        view.visibility = View.INVISIBLE
                        view.translationX = 0f
                        view.translationY = 0f
                        view.scaleX = 1f
                        view.scaleY = 1f
                        targetView.alpha = 1f
                        targetView.visibility = VISIBLE
                        targetView.text = view.text

                        if (isClue == true) {
                            targetView.background = ContextCompat.getDrawable(context, R.drawable.letter_button_yellow)
                            targetView.setOnClickListener(null)
                        } else {
                            targetView.background = ContextCompat.getDrawable(context, R.drawable.letter_button)
                        }
                        if (targetButtonNumber == gamecontroller.question!!.answer.length - 1) {
                            checkAnswer()
                        }
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationStart(p0: Animator?) {
                    }
                })
    }

    fun animateButtonDown(buttonNumber: Int, targetButtonNumber: Int) {
        val view = arrayButtonsToSee[buttonNumber]
        val targetView = arrayButtonsToPress[targetButtonNumber]
        isThePressButtonsMoving[targetButtonNumber] = true
        view.background = ContextCompat.getDrawable(context, R.drawable.letter_button_shadow)
        view.text = ""
        view.alpha = 0f
        view.animate()
                .alpha(0.5f)
                .setInterpolator(object : TimeInterpolator {
                    override fun getInterpolation(p0: Float): Float {
                        return Math.sin(p0 * Math.PI / 2).toFloat()
                    }
                })

        targetView.translationY = -(targetView.y - view.y)
        targetView.scaleY = 1 / 2f
        targetView.scaleX = 1 / 2f
        targetView.translationX = -(targetView.x - view.x)
        targetView.visibility = VISIBLE

        targetView.animate()
                .translationX(0f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(object : TimeInterpolator {
                    override fun getInterpolation(p0: Float): Float {
                        return Math.sin(p0 * Math.PI / 2).toFloat()
                    }
                })
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        isThePressButtonsMoving[targetButtonNumber] = false
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationStart(p0: Animator?) {
                    }

                })
    }

    fun animateButtonMove(buttonNumber: Int, targetButtonNumber: Int, goingUp: Boolean, isClue: Boolean) {
        if (goingUp) {
            animateButtonUp(buttonNumber, targetButtonNumber, isClue)
        } else {
            animateButtonDown(buttonNumber, targetButtonNumber)
        }
    }

    fun moveButtonBack(position: Int) {
        val a = gamecontroller.selectedButtonsAsInts.size - 1
        for (i in 0..a - position) {
            animateButtonMove(a - i, gamecontroller.selectedButtonsAsInts[a - i], false, false)
            gamecontroller.selectedButtonsAsInts.removeAt(gamecontroller.selectedButtonsAsInts.size - 1)
        }
    }

    fun setListeners() {
        for (i in 0..arrayButtonsToPress.size - 1) {
            arrayButtonsToPress[i].setOnClickListener {
                if (!isAnyOfTheButtonsMoving()) {
                    moveButton(i, false)
                }
            }
        }
        for (i in 0..arrayButtonsToSee.size - 1) {
            arrayButtonsToSee[i].setOnClickListener {
                if (!isAnyOfTheButtonsMoving()) {
                    moveButtonBack(i)
                }
            }
        }
        clueButton.setOnClickListener {
            if (!isAnyOfTheButtonsMoving()) {
                prepareToGiveClue()
            }
        }
    }

    fun setLetters() {
        for (i in 0..arrayButtonsToPress.size - 1) {
            arrayButtonsToPress[i].text = arrayOfLetters!![i].toString()
        }
    }

    fun isAnyOfTheButtonsMoving(): Boolean {
        var isMoving = false
        for (i in 0..isThePressButtonsMoving.size - 1) {
            if (isThePressButtonsMoving[i] == true) {
                return true
            }
        }
        for (i in 0..isTheSeeButtonsMoving.size - 1) {
            if (isTheSeeButtonsMoving[i] == true)
                return true
        }
        return false
    }
}
