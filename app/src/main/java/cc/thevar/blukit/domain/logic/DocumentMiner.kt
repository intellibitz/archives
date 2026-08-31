/**
 * BLUKIT DOMAIN: DOCUMENT MINER
 *
 * Specialized worker for offline intelligence. 
 * Extracts structured data from shared documents (PDF, Text, JSON) without internet.
 */
package cc.thevar.blukit.domain.logic

import android.content.Context
import android.util.Log
import cc.thevar.blukit.domain.model.Echo
import java.io.File
import java.util.regex.Pattern

class DocumentMiner(private val context: Context) {

    data class MinedData(
        val tasks: List<String> = emptyList(),
        val entities: List<String> = emptyList(),
        val detectedDates: List<Long> = emptyList(),
        val summarySnippet: String? = null
    )

    /**
     * Mines a file Echo for intelligence.
     */
    fun mineFile(echo: Echo): MinedData {
        if (echo.type != Echo.TYPE_FILE && echo.type != Echo.TYPE_IMAGE) return MinedData()
        
        val filePath = echo.content
        val file = File(filePath)
        if (!file.exists()) return MinedData()

        return try {
            when {
                echo.mimeType == "text/plain" || filePath.endsWith(".txt") -> mineTextFile(file)
                echo.mimeType == "application/json" || filePath.endsWith(".json") -> mineJsonFile(file)
                else -> MinedData(summarySnippet = "UNSUPPORTED FORMAT: ${echo.fileName}")
            }
        } catch (e: Exception) {
            Log.e("DocumentMiner", "Mining failed: ${e.message}")
            MinedData()
        }
    }

    private fun mineTextFile(file: File): MinedData {
        val content = file.readText().uppercase()
        val tasks = mutableListOf<String>()
        
        // Simple heuristic for tasks: Lines starting with [ ], TODO, or - [ ]
        content.lines().forEach { line ->
            if (line.contains("TODO") || line.contains("TASK:") || line.startsWith("- [ ]")) {
                tasks.add(line.trim().removePrefix("- [ ]").removePrefix("TODO:").trim())
            }
        }

        // Entity extraction (Capitalized words not in common stopwords)
        val entities = extractPotentialEntities(content)
        
        return MinedData(
            tasks = tasks.take(5),
            entities = entities.take(10),
            summarySnippet = content.take(100).replace("\n", " ") + "..."
        )
    }

    private fun mineJsonFile(file: File): MinedData {
        // Basic JSON mining: looking for keys like "task", "title", "description"
        val content = file.readText()
        val tasks = mutableListOf<String>()
        val taskPattern = Pattern.compile("\"(?:task|title|todo)\"\\s*:\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
        val matcher = taskPattern.matcher(content)
        while (matcher.find()) {
            matcher.group(1)?.let { tasks.add(it) }
        }
        
        return MinedData(
            tasks = tasks.take(5),
            summarySnippet = "STRUCTURED DATA DETECTED"
        )
    }

    private fun extractPotentialEntities(text: String): List<String> {
        val words = text.split(Regex("\\s+"))
        val stopWords = setOf("THE", "AND", "WITH", "THIS", "THAT", "FROM")
        return words.filter { it.length > 4 && it !in stopWords }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .map { it.first }
    }
}
