package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Sends a text prompt to the Gemini API and returns the text response.
     */
    suspend fun generateResponse(prompt: String, chatHistory: List<ChatMessage> = emptyList()): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "placeholder") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to mock intelligent response.")
            return@withContext getLocalCoachFallback(prompt)
        }

        try {
            val systemPrompt = "You are Body Control AI, a smart personalized fitness strategist coach. " +
                    "The user is Alex Johnson (Age 28, Height 182cm, Weight 85kg). Give brief, highly motivational and scientifically accurate coaching feedback, keeping under 80 words. " +
                    "Adopt a professional, modern, encouraging, gym-oriented voice with a dark electric energy."

            // Build request JSON safely with org.json
            val requestJson = JSONObject()
            
            // Add system instructions
            val systemInstructionJson = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            }
            requestJson.put("systemInstruction", systemInstructionJson)

            // Construct contents list with historical messages
            val contentsArray = JSONArray()

            // Map chatHistory to contents matching API (user or model role)
            for (msg in chatHistory) {
                val turn = JSONObject()
                turn.put("role", if (msg.role == "user") "user" else "model")
                turn.put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", msg.content) })
                })
                contentsArray.put(turn)
            }

            // Current message turn
            val latestTurn = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", prompt) })
                })
            }
            contentsArray.put(latestTurn)
            
            requestJson.put("contents", contentsArray)

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed code: ${response.code}, body: $errorBody")
                    return@withContext "Error: Gemini response failed with status ${response.code}. Try configuring secrets."
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentObj = candidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No message received.")
                    }
                }
                "Body Control Coach is analyzing your input... Ready details soon!"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating AI response", e)
            "Fallback: It looks like I had trouble connecting to my strategist network. Based on your target: Ensure you maintain proper macro balance (Protein is key!) and hydrate adequately (at least 3.0L)."
        }
    }

    /**
     * Requests the Gemini model to return 3 generated meals for a specific target.
     */
    suspend fun generateMealPlan(dietPreference: String): List<MealItemStub> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "placeholder") {
            Log.w(TAG, "Gemini API key is not configured for meals. Falling back to local nutrition algorithm.")
            return@withContext getLocalMealFallback(dietPreference)
        }

        try {
            val prompt = "Regenerate a personalized nutrition-focused, delicious meal plan targeting: $dietPreference. " +
                    "Return exactly 3 meals (Breakfast, Snack, Lunch). You MUST respond with ONLY a raw, valid JSON array, and absolutely nothing else. " +
                    "Do not wrapping the JSON with markdown code blocks (like ```json ... ```) or headers. " +
                    "JSON item schema:\n" +
                    "{\n" +
                    "  \"category\": \"Breakfast\" | \"Snack\" | \"Lunch\",\n" +
                    "  \"title\": String (e.g. 'Egg & Toast', 'Almond Chia Pudding', 'Spicy Chicken Salad'),\n" +
                    "  \"kCal\": Integer (e.g. 350),\n" +
                    "  \"proteinG\": Integer (e.g. 18),\n" +
                    "  \"carbsG\": Integer (e.g. 32),\n" +
                    "  \"fatsG\": Integer (e.g. 12),\n" +
                    "  \"timeLabel\": String (e.g. '07:00' or '11:00' or '14:00')\n" +
                    "}"

            val requestJson = JSONObject()
            requestJson.put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            } )

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext getLocalMealFallback(dietPreference)
                }

                val rawBody = response.body?.string() ?: ""
                var cleanJson = rawBody
                // Handle possible Gemini markdown output wraps
                if (cleanJson.contains("```json")) {
                    cleanJson = cleanJson.substringAfter("```json").substringBefore("```")
                } else if (cleanJson.contains("```")) {
                    cleanJson = cleanJson.substringAfter("```").substringBefore("```")
                }
                cleanJson = cleanJson.trim()

                val jsonArray = JSONArray(cleanJson)
                val mealStubs = mutableListOf<MealItemStub>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    mealStubs.add(
                        MealItemStub(
                            category = obj.optString("category", "Meal"),
                            title = obj.optString("title", "Healthy Dish"),
                            kCal = obj.optInt("kCal", 300),
                            proteinG = obj.optInt("proteinG", 15),
                            carbsG = obj.optInt("carbsG", 25),
                            fatsG = obj.optInt("fatsG", 10),
                            timeLabel = obj.optString("timeLabel", "12:00")
                        )
                    )
                }
                return@withContext mealStubs
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating AI meal plan", e)
            getLocalMealFallback(dietPreference)
        }
    }

    private fun getLocalCoachFallback(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("weight loss") || lower.contains("slim") -> {
                "To optimize weight loss, keep daily calories near 1,800. Prioritize 1.5g of protein per kg of bodyweight to maintain lean mass while stripping fat. I've adjusted your daily macro targets accordingly!"
            }
            lower.contains("hydrat") || lower.contains("water") -> {
                "Water regulates joint lubrication, kinetic energy, and cognitive focus. We recommend matching your goal of 3.5 Liters today. Drink 500ml right now to kickstart metabolism!"
            }
            lower.contains("sluggish") || lower.contains("tired") -> {
                "Feeling sluggish is often due to low hydration or glycemic drops. Focus on low-glycemic carbs like oats or berries and ensure you are taking in enough sodium/magnesium. Let's optimize nutrition today!"
            }
            lower.contains("macro") || lower.contains("carb") || lower.contains("protein") -> {
                "For your build (182cm, 85kg), aim for 150g Protein, 200g Carbs, and 60g Fats. Keep protein intake level to guard muscle density, and adjust carb intake based on active gym splits."
            }
            lower.contains("rice") || lower.contains("night") -> {
                "Eating white rice at night is perfectly fine as long as it fits into your daily calorie budget. Carbs at night can actually help with sleep and muscle glycogen recovery for tomorrow's lift!"
            }
            else -> {
                "Strategist insight: To maximize muscle recovery, optimize your nutrition-split today. Concentrate on fiber-dense meals post-workout and try to increase your hydration intake to steady overall energy."
            }
        }
    }

    private fun getLocalMealFallback(preference: String): List<MealItemStub> {
        val multiplier = if (preference.lowercase().contains("shred") || preference.lowercase().contains("loss")) 0.8 else 1.2
        return listOf(
            MealItemStub(
                category = "Breakfast",
                title = "Almond Avocado Toast (AI Adjusted)",
                kCal = (342 * multiplier).toInt(),
                proteinG = 16,
                carbsG = 28,
                fatsG = 15,
                timeLabel = "07:00"
            ),
            MealItemStub(
                category = "Snack",
                title = "Mixed Berries with Greek Yogurt (AI Adjusted)",
                kCal = (130 * multiplier).toInt(),
                proteinG = 12,
                carbsG = 18,
                fatsG = 2,
                timeLabel = "11:00"
            ),
            MealItemStub(
                category = "Lunch",
                title = "Grilled Salmon Rice Bowl (AI Adjusted)",
                kCal = (580 * multiplier).toInt(),
                proteinG = 45,
                carbsG = 48,
                fatsG = 14,
                timeLabel = "14:00"
            )
        )
    }
}

data class MealItemStub(
    val category: String,
    val title: String,
    val kCal: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatsG: Int,
    val timeLabel: String
)
