package joneros.jenny.quizzit

import android.util.Log
import java.util.*


/**
 * Created by Jenny on 2017-09-15.mm
 */
class GameController {

    companion object {
        val TAG = "GameController"
    }

    var points = 5
    var score = 0
    val selectedButtonsAsInts = mutableListOf<Int>()
    val spacePositionsInAnswer = mutableListOf<Int>()
    val spaceOrder = mutableListOf<Int>()
    val positions = mutableListOf<Int>()
    val shuffledLetters = mutableListOf<Char>()
    var currentQuestion = 0
    val random = Random()
    var questions = emptyList<Question>()
    var question: Question? = Question(-1, "", "", "", -1)
    val listOfIncorrectLetters = mutableListOf<Int>()
    var amountOfTimesToSackLetters = 1


    var alphabet: ArrayList<Char> = arrayListOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')
    var lettersInAlphabet = 26

    fun clearThings() {
        points = 5
        spacePositionsInAnswer.clear()
        spaceOrder.clear()
        selectedButtonsAsInts.clear()
        shuffledLetters.clear()
        positions.clear()
        listOfIncorrectLetters.clear()
        amountOfTimesToSackLetters = 1
    }

    fun getRidOfSpacesInAnswer(answer: String): String {
        spacePositionsInAnswer.clear()
        spaceOrder.clear()
        var resultAnswer = ""
        for (i in 0..answer.length - 1) {
            if (answer[i] == ' ') {
                spacePositionsInAnswer += i
                spaceOrder += i - spaceOrder.size
            } else {
                resultAnswer += answer[i]
            }
        }
        return resultAnswer
    }

    fun getNewQuestion(): Unit {
        question = questions[currentQuestion]
        currentQuestion++
    }

    fun getLetters(): CharArray {
        val cleanAnswer = getRidOfSpacesInAnswer(question!!.answer)
        return randomizeLetters(cleanAnswer)
    }

    fun randomizeLetters(name: String): CharArray {
        var charNames = name.toCharArray()
        if ((14 - name.length) > 0) {
            for (i in 0..13 - name.length) {
                charNames += alphabet[rand(lettersInAlphabet)]
            }
        }
        return shuffle(items = charNames)
    }

    fun rand(number: Int): Int {
        return random.nextInt(number)
    }

    fun generateRandomPositions(items: CharArray) {
        val rg = Random()
        for (i in 0..items.size - 1) {
            var randomPosition = rg.nextInt(items.size)
            for (j in 0..positions.size - 1) {
                if (positions.contains(randomPosition)) {
                    if (randomPosition < items.size - 1) {
                        randomPosition += 1
                    } else randomPosition = 0
                }
            }
            positions += randomPosition
        }
    }

    fun shuffle(items: CharArray): CharArray {
        generateRandomPositions(items)
        val itemlist = mutableListOf<Char>()
        for (i in 0..items.size - 1) {
            itemlist += items[i]
        }
        for (i in 0..itemlist.size - 1) {
            items[positions[i]] = itemlist[i]
        }
        for (i in 0..items.size - 1) {
            shuffledLetters += items[i]
        }
        return items
    }

    fun weMustMoveLettersBack(): Boolean {
        val cluesUsed = 5 - points
        val timesToSack = timesToSackLetters(question!!.answer.length)
        var tentativeAnswerLength = 0
        var stillGuessing = true
        if (timesToSack >= cluesUsed) {
            stillGuessing = true
        } else {
            stillGuessing = false
            tentativeAnswerLength = cluesUsed - timesToSack
        }
        if(stillGuessing == true) {
            tentativeAnswerLength = 0
        }
        if (selectedButtonsAsInts.size > tentativeAnswerLength) {
            return true
        }
        else {
            return false
        }
    }

    fun whichClue(): Int {
        val clueIndex = points
        points = points - 1
        return clueIndex
    }

    fun identifyIncorrectLetters() {
        val positionList = mutableListOf<Int>()
        for (i in 0..positions.size - 1) {
            positionList += positions[i]
        }
        for (i in 0..getRidOfSpacesInAnswer(question!!.answer).length - 1) {
            positionList.removeAt(0)
        }
        listOfIncorrectLetters.addAll(positionList)
    }

    fun timesToSackLetters(answerLength: Int): Int {
        var amount = 1
        when (answerLength) {
            1 -> amount = 4
            2 -> amount = 4
            3 -> amount = 3
            4 -> amount = 2
            else -> amount = 1
        }
        return amount
    }

    fun incorrectLettersToBeSacked(total: Int, answerLength: Int): Int {
        val b = timesToSackLetters(answerLength)
        val a = total / b
        return a
    }

    fun identifyNonChosenLetters(): MutableList<Int> {
        val positionList = mutableListOf<Int>()
        for (i in 0..positions.size - 1) {
            positionList += positions[i]
        }
        for (i in 0..getRidOfSpacesInAnswer(question!!.answer).length - 1) {
            positionList.remove(selectedButtonsAsInts[i])
        }
        return positionList
    }

    fun identifyCorrectLetter(number: Int): Int {
        return positions[number]
    }

    fun checkTheAnswer(): Boolean {
        var lettersInLine = mutableListOf<Char>()
        for (i in 0..selectedButtonsAsInts.size - 1) {
            val char = shuffledLetters[selectedButtonsAsInts[i]]
            lettersInLine.add(char)
        }
        val tempAnswer = getRidOfSpacesInAnswer(question!!.answer)
        for (i in 0..tempAnswer.length - 1) {
            if (tempAnswer[i] != lettersInLine[i]) {
                return false
            }
        }
        score += points
        return true
    }
}
