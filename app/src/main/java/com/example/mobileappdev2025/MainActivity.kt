package com.example.mobileappdev2025

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileInputStream
import java.util.Random
import java.util.Scanner

data class WordDefinition(val word: String, val definition: String, var streak: Int = 0);

class MainActivity : AppCompatActivity() {
    private val ADD_WORD_CODE = 1234; // 1-65535
    private lateinit var myAdapter : ArrayAdapter<String>; // connect from data to gui
    private var dataDefList = ArrayList<String>(); // data
    private var wordDefinition = mutableListOf<WordDefinition>();
    private var score : Int = 0;
    private var totalCorrect : Int = 0;
    private var totalWrong : Int = 0;
    private var currentStreak: Int = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadWordsFromDisk()
        pickNewWordAndLoadDataList()
        setupList()

        val defList = findViewById<ListView>(R.id.dynamic_def_list)
        defList.setOnItemClickListener { _, _, index, _ ->
            val isCorrect = wordDefinition[0].definition == dataDefList[index]
            onDefinitionSelected(isCorrect)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_WORD_CODE && resultCode == RESULT_OK && data != null){
            val word = data.getStringExtra("word")?:""
            val def = data.getStringExtra("def")?:""

            Log.d("MAD", word)
            Log.d("MAD", def)

            if (word == "" || def == "")
                return

            wordDefinition.add(WordDefinition(word, def))

            pickNewWordAndLoadDataList()
            myAdapter.notifyDataSetChanged()
        }
    }

    private fun loadWordsFromDisk() {
        val file = File(applicationContext.filesDir, "user_data.csv")

        if (file.exists()) {
            val readResult = FileInputStream(file)
            val scanner = Scanner(readResult)

            while(scanner.hasNextLine()){
                val line = scanner.nextLine()
                val wd = line.split("|")
                wordDefinition.add(WordDefinition(wd[0], wd[1]))
            }
        } else {
            file.createNewFile()

            val reader = Scanner(resources.openRawResource(R.raw.default_words))
            while(reader.hasNextLine()){
                val line = reader.nextLine()
                val wd = line.split("|")
                wordDefinition.add(WordDefinition(wd[0], wd[1]))
                file.appendText("${wd[0]}|${wd[1]}\n")
            }
        }
    }

    private fun pickNewWordAndLoadDataList() {
        // Separate words into frequently used and rarely used based on streak count
        val frequentWords = wordDefinition.filter { it.streak < 2 }.toMutableList()
        val rareWords = wordDefinition.filter { it.streak >= 2 }.toMutableList()

        frequentWords.shuffle()
        rareWords.shuffle()

        // Prioritize frequent words unless none are available
        val targetWord = frequentWords.firstOrNull() ?: rareWords.firstOrNull() ?: return

        // Reorder word list so the target word is first
        wordDefinition.remove(targetWord)
        wordDefinition.add(0, targetWord)

        // Clear current list and add the target word definition
        dataDefList.clear()
        dataDefList.add(targetWord.definition)

        // Add up to 3 other unique definitions
        val extraDefinitions = wordDefinition.filter { it.word != targetWord.word }
            .shuffled()
            .take(3)
            .map { it.definition }

        dataDefList.addAll(extraDefinitions)
        dataDefList.shuffle()

        // Display the target word
        findViewById<TextView>(R.id.word).text = targetWord.word
    }


    private fun setupList() {
        myAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataDefList)

        // connect to list
        val defList = findViewById<ListView>(R.id.dynamic_def_list)
        defList.adapter = myAdapter
    }

    private fun onDefinitionSelected(isCorrect: Boolean) {
        if (isCorrect) {
            score += 10
            currentStreak++
            score = score + currentStreak
            totalCorrect++
        } else {
            score -= 5
            currentStreak = 0
            totalWrong++
        }

        findViewById<TextView>(R.id.score_text).text = "Score: $score"

        pickNewWordAndLoadDataList()
        myAdapter.notifyDataSetChanged()
    }

    fun openStats(view : View) {
        val myIntent = Intent(this, StatsActivity::class.java)
        myIntent.putExtra("score", score.toString())
        myIntent.putExtra("totalCorrect", totalCorrect.toString())
        myIntent.putExtra("totalWrong", totalWrong.toString())
        myIntent.putExtra("currentStreak", currentStreak.toString())
        startActivity(myIntent)
    }

    fun openAddWord(view : View) {
        var myIntent = Intent(this, AddWordActivity::class.java)
        startActivityForResult(myIntent, ADD_WORD_CODE)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Save user stats and streak to user_stats file
        saveStatsToFile()
        saveStreaksToFile()
    }

    private fun saveStatsToFile() {
        val statsFile = File(applicationContext.filesDir, "user_stats.csv")

        try {
            val writer = statsFile.printWriter()

            // Save score, totalCorrect, totalWrong, and currentStreak
            writer.println("Score,$score")
            writer.println("TotalCorrect,$totalCorrect")
            writer.println("TotalWrong,$totalWrong")
            writer.println("CurrentStreak,$currentStreak")

            writer.close()
        } catch (e: Exception) {
            Log.e("StatsSaveError", "Error saving stats to file: ${e.message}")
        }
    }

    private fun saveStreaksToFile() {
        val streaksFile = File(applicationContext.filesDir, "user_streaks.csv")

        try {
            val writer = streaksFile.printWriter()

            // Save per-word streak information
            for (wd in wordDefinition) {
                writer.println("${wd.word},${wd.streak}")
            }

            writer.close()
        } catch (e: Exception) {
            Log.e("StreaksSaveError", "Error saving streaks to file: ${e.message}")
        }
    }
}
