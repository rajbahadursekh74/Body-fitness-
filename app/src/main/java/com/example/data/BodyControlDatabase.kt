package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alex Johnson",
    val memberType: String = "Premium Member",
    val age: Int = 28,
    val heightCm: Int = 182,
    val weightKg: Float = 85.0f,
    val promoCode: String = "ALEX-BC-2026"
)

@Entity(tableName = "daily_metrics")
data class DailyMetrics(
    @PrimaryKey val dateString: String, // e.g. "2026-05-22"
    val waterIntakeMl: Int = 1800,      // e.g. 1.8L -> 1800 ml
    val steps: Int = 8432,
    val loggedWeightKg: Float = 76.4f
)

@Entity(tableName = "meals")
data class MealItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String,             // e.g. "2026-05-22"
    val timeLabel: String,              // e.g. "07:00", "11:00", "14:00"
    val category: String,               // e.g. "Breakfast", "Snack", "Lunch", "Dinner"
    val title: String,                  // e.g. "Breakfast (Egg & Toast)"
    val kCal: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatsG: Int,
    val imageUrl: String = ""           // URL or fallback placeholder name
)

@Entity(tableName = "coach_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,                   // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_log")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String,
    val workoutTitle: String,
    val durationMin: Int,
    val caloriesBurned: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// --- Daos ---

@Dao
interface BodyControlDao {
    // Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    // Daily Metrics
    @Query("SELECT * FROM daily_metrics WHERE dateString = :date LIMIT 1")
    fun getDailyMetrics(date: String): Flow<DailyMetrics?>

    @Query("SELECT * FROM daily_metrics ORDER BY dateString ASC")
    fun getAllDailyMetrics(): Flow<List<DailyMetrics>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyMetrics(metrics: DailyMetrics)

    // Meals
    @Query("SELECT * FROM meals WHERE dateString = :date ORDER BY timeLabel ASC")
    fun getMealsForDate(date: String): Flow<List<MealItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<MealItem>)

    @Query("DELETE FROM meals WHERE dateString = :date")
    suspend fun deleteMealsForDate(date: String)

    // Chat History
    @Query("SELECT * FROM coach_messages ORDER BY id ASC")
    fun getChatHistory(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM coach_messages")
    suspend fun clearChatHistory()

    // Workout logs
    @Query("SELECT * FROM workout_log ORDER BY timestamp DESC")
    fun getWorkoutLogs(): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logWorkout(log: WorkoutLog)
}

// --- Database ---

@Database(
    entities = [
        UserProfile::class,
        DailyMetrics::class,
        MealItem::class,
        ChatMessage::class,
        WorkoutLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BodyControlDatabase : RoomDatabase() {
    abstract val dao: BodyControlDao
}

// --- Repositories ---

class BodyControlRepository(private val dao: BodyControlDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allMetricsList: Flow<List<DailyMetrics>> = dao.getAllDailyMetrics()
    val chatHistory: Flow<List<ChatMessage>> = dao.getChatHistory()
    val workoutLogs: Flow<List<WorkoutLog>> = dao.getWorkoutLogs()

    suspend fun saveProfile(profile: UserProfile) {
        dao.saveUserProfile(profile)
    }

    fun getDailyMetrics(date: String): Flow<DailyMetrics?> = dao.getDailyMetrics(date)

    suspend fun updateDailyMetrics(metrics: DailyMetrics) {
        dao.saveDailyMetrics(metrics)
    }

    fun getMealsForDate(date: String): Flow<List<MealItem>> = dao.getMealsForDate(date)

    suspend fun saveMealsForDate(date: String, meals: List<MealItem>) {
        dao.deleteMealsForDate(date)
        dao.insertMeals(meals)
    }

    suspend fun appendChatMessage(role: String, content: String): ChatMessage {
        val msg = ChatMessage(role = role, content = content)
        dao.insertMessage(msg)
        return msg
    }

    suspend fun clearHistory() {
        dao.clearChatHistory()
    }

    suspend fun addWorkoutLog(title: String, duration: Int, kcal: Int, date: String) {
        dao.logWorkout(WorkoutLog(workoutTitle = title, durationMin = duration, caloriesBurned = kcal, dateString = date))
    }

    // Seeding database with original UI design elements if first time loading
    suspend fun checkAndSeedDatabase(context: Context, currentDate: String) {
        // Pre-loaded Coach/AI start turn
        dao.insertMessage(ChatMessage(
            role = "model",
            content = "Good morning, Alex. I've analyzed your sleep data from last night (7.5h) and your heart rate variability is up by 12%. Ready for a high-intensity session, or should we focus on nutrition today?"
        ))
        
        // Save initial default profile
        dao.saveUserProfile(UserProfile())

        // Save progress indicators matching the Screenshots for week days (e.g. M, T, W, T, F, S, S)
        // Weight logs to paint a flawless weekly weight progress curve!
        val weightHistory = listOf(
            DailyMetrics("2026-05-18", waterIntakeMl = 2500, steps = 9500, loggedWeightKg = 77.2f), // Mon
            DailyMetrics("2026-05-19", waterIntakeMl = 3000, steps = 12000, loggedWeightKg = 77.0f), // Tue
            DailyMetrics("2026-05-20", waterIntakeMl = 2800, steps = 11000, loggedWeightKg = 76.8f), // Wed
            DailyMetrics("2026-05-21", waterIntakeMl = 3100, steps = 8900, loggedWeightKg = 76.6f), // Thu
            DailyMetrics(currentDate, waterIntakeMl = 1800, steps = 8432, loggedWeightKg = 76.4f)  // Today (Fri 22 May 2026)
        )
        weightHistory.forEach {
            dao.saveDailyMetrics(it)
        }

        // Add Meals for current date
        val todayMeals = listOf(
            MealItem(
                dateString = currentDate,
                timeLabel = "07:00",
                category = "Breakfast",
                title = "Breakfast (Egg & Toast)",
                kCal = 350,
                proteinG = 18,
                carbsG = 32,
                fatsG = 14,
                imageUrl = "egg_toast"
            ),
            MealItem(
                dateString = currentDate,
                timeLabel = "11:00",
                category = "Snack",
                title = "Fruits (Apple & Berries)",
                kCal = 120,
                proteinG = 1,
                carbsG = 28,
                fatsG = 0,
                imageUrl = "apple_berries"
            ),
            MealItem(
                dateString = currentDate,
                timeLabel = "14:00",
                category = "Lunch",
                title = "Lunch (Chicken & Rice)",
                kCal = 550,
                proteinG = 42,
                carbsG = 58,
                fatsG = 12,
                imageUrl = "chicken_rice"
            )
        )
        dao.insertMeals(todayMeals)
    }
}
