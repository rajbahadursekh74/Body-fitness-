package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BodyControlViewModel(application: Application) : AndroidViewModel(application) {

    private val database: BodyControlDatabase by lazy {
        Room.databaseBuilder(
            application,
            BodyControlDatabase::class.java,
            "body_control_db"
        ).fallbackToDestructiveMigration().build()
    }

    private val repository: BodyControlRepository by lazy {
        BodyControlRepository(database.dao)
    }

    // Static dates for horizontal weekly picker
    val daysOfWeek = listOf(
        DayInfo("MON", "18", "2026-05-18"),
        DayInfo("TUE", "19", "2026-05-19"),
        DayInfo("WED", "20", "2026-05-20"),
        DayInfo("THU", "21", "2026-05-21"),
        DayInfo("FRI", "22", "2026-05-22"),
        DayInfo("SAT", "23", "2026-05-23"),
        DayInfo("SUN", "24", "2026-05-24")
    )

    // Current Date string (defaults to Friday 22 May 2026)
    val currentDateStr = "2026-05-22"

    private val _selectedDate = MutableStateFlow("2026-05-22")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Observable states from Local Database
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val allMetrics: StateFlow<List<DailyMetrics>> = repository.allMetricsList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics for the currently selected date
    val currentMetrics: StateFlow<DailyMetrics> = selectedDate
        .flatMapLatest { date ->
            repository.getDailyMetrics(date).map { it ?: DailyMetrics(date, waterIntakeMl = 0, steps = 0, loggedWeightKg = 76.4f) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyMetrics("2026-05-22", waterIntakeMl = 0, steps = 0))

    // Meals for the currently selected date
    val mealsForSelectedDate: StateFlow<List<MealItem>> = selectedDate
        .flatMapLatest { date ->
            repository.getMealsForDate(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatHistory: StateFlow<List<ChatMessage>> = repository.chatHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutLogs: StateFlow<List<WorkoutLog>> = repository.workoutLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Loading & Animation indicators
    private val _isCoachThinking = MutableStateFlow(false)
    val isCoachThinking: StateFlow<Boolean> = _isCoachThinking.asStateFlow()

    private val _isAiRegeneratingMeals = MutableStateFlow(false)
    val isAiRegeneratingMeals: StateFlow<Boolean> = _isAiRegeneratingMeals.asStateFlow()

    // Achievement badges tracker dynamically calculated
    val achievements: StateFlow<List<Achievement>> = combine(currentMetrics, workoutLogs) { metrics, logs ->
        val list = mutableListOf<Achievement>()
        
        // Water Achievement
        val waterProgress = metrics.waterIntakeMl / 3500f
        list.add(
            Achievement(
                id = "water_master",
                title = "Water Master",
                description = "Drink 3.5 Liters of water in a day",
                progress = waterProgress.coerceIn(0f, 1f),
                isUnlocked = waterProgress >= 1f,
                iconType = "water"
            )
        )

        // Steps Achievement
        val stepProgress = metrics.steps / 10000f
        list.add(
            Achievement(
                id = "step_hero",
                title = "10K Step Hero",
                description = "Reach 10,000 steps daily",
                progress = stepProgress.coerceIn(0f, 1f),
                isUnlocked = stepProgress >= 1f,
                iconType = "steps"
            )
        )
        
        // Workout Streak Achievement
        val completedCount = logs.size
        val workoutProgress = completedCount / 5f
        list.add(
            Achievement(
                id = "seven_day_streak",
                title = "7 Day Streak",
                description = "Complete 5 or more personal workout classes",
                progress = workoutProgress.coerceIn(0f, 1f),
                isUnlocked = completedCount >= 5,
                iconType = "workout"
            )
        )

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed initial metrics if database is fresh
        viewModelScope.launch {
            repository.allMetricsList.first().let { list ->
                if (list.isEmpty()) {
                    repository.checkAndSeedDatabase(application, currentDateStr)
                }
            }
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    // --- Habit Actions ---

    fun addWater(ml: Int) {
        viewModelScope.launch {
            val current = currentMetrics.value
            val updated = current.copy(waterIntakeMl = current.waterIntakeMl + ml)
            repository.updateDailyMetrics(updated)
        }
    }

    fun addSteps(amount: Int) {
        viewModelScope.launch {
            val current = currentMetrics.value
            val updated = current.copy(steps = current.steps + amount)
            repository.updateDailyMetrics(updated)
        }
    }

    fun logWeight(weight: Float) {
        viewModelScope.launch {
            // Update selected day metric
            val current = currentMetrics.value
            val updated = current.copy(loggedWeightKg = weight)
            repository.updateDailyMetrics(updated)

            // Update local Profile weight as well
            val profile = userProfile.value
            repository.saveProfile(profile.copy(weightKg = weight))
        }
    }

    fun updateProfile(name: String, age: Int, heightCm: Int, weightKg: Float) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(
                name = name,
                age = age,
                heightCm = heightCm,
                weightKg = weightKg
            )
            repository.saveProfile(updated)

            // Also update current metrics logged weight
            val current = currentMetrics.value
            repository.updateDailyMetrics(current.copy(loggedWeightKg = weightKg))
        }
    }

    // --- Coach Conversation Chat Logic ---

    fun askCoach(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            // 1. Add user query to persistent log
            repository.appendChatMessage(role = "user", content = query)
            _isCoachThinking.value = true

            // Send full chat history to client to enable real conversational turns
            val history = chatHistory.value
            val response = GeminiApiClient.generateResponse(query, history)

            // 2. Add assistant response
            repository.appendChatMessage(role = "model", content = response)
            _isCoachThinking.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- Meal Planner Regenerator Logic ---

    fun regenerateMealsPlan(preference: String = "High protein balanced shred plan") {
        viewModelScope.launch {
            _isAiRegeneratingMeals.value = true
            try {
                val date = selectedDate.value
                val stubs = GeminiApiClient.generateMealPlan(preference)
                val mealItems = stubs.map {
                    MealItem(
                        dateString = date,
                        timeLabel = it.timeLabel,
                        category = it.category,
                        title = "${it.category} (${it.title})",
                        kCal = it.kCal,
                        proteinG = it.proteinG,
                        carbsG = it.carbsG,
                        fatsG = it.fatsG
                    )
                }
                repository.saveMealsForDate(date, mealItems)
            } catch (e: Exception) {
                // Fallback handled, won't crash
            } finally {
                _isAiRegeneratingMeals.value = false
            }
        }
    }

    // --- Workouts Completer and streak builder ---

    fun logCompletedWorkout(title: String, durationMin: Int, caloriesBurned: Int) {
        viewModelScope.launch {
            repository.addWorkoutLog(
                title = title,
                duration = durationMin,
                kcal = caloriesBurned,
                date = currentDateStr
            )
            
            // Increment steps slightly since they worked out!
            addSteps(1500)
        }
    }
}

// Helper models for VM states
data class DayInfo(val name: String, val dateNum: String, val dateString: String)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val progress: Float,
    val isUnlocked: Boolean,
    val iconType: String
)

// Factory for ViewModel
class BodyControlViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BodyControlViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BodyControlViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
