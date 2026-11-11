package com.example.ui.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Exercise
import com.example.data.model.WorkoutCategory
import com.example.data.model.WorkoutDifficulty
import com.example.data.model.WorkoutRoutine
import com.example.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkoutsUiState(
    val selectedCategory: WorkoutCategory = WorkoutCategory.ALL,
    val selectedDifficulty: WorkoutDifficulty? = null,
    val searchQuery: String = "",
    val allRoutines: List<WorkoutRoutine> = emptyList(),
    val filteredRoutines: List<WorkoutRoutine> = emptyList(),
    val selectedRoutineDetail: WorkoutRoutine? = null,
    val isLoading: Boolean = false
)

class WorkoutsViewModel(
    private val fitnessRepository: FitnessRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(WorkoutCategory.ALL)
    private val _selectedDifficulty = MutableStateFlow<WorkoutDifficulty?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _currentDetailId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<WorkoutsUiState> = combine(
        fitnessRepository.allRoutines,
        _selectedCategory,
        _selectedDifficulty,
        _searchQuery
    ) { routines, category, difficulty, query ->
        val filtered = routines.filter { routine ->
            val matchesCategory = category == WorkoutCategory.ALL || routine.category == category
            val matchesDifficulty = difficulty == null || routine.difficulty == difficulty
            val matchesQuery = query.isBlank() ||
                routine.title.contains(query, ignoreCase = true) ||
                routine.description.contains(query, ignoreCase = true)
            matchesCategory && matchesDifficulty && matchesQuery
        }

        WorkoutsUiState(
            selectedCategory = category,
            selectedDifficulty = difficulty,
            searchQuery = query,
            allRoutines = routines,
            filteredRoutines = filtered,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutsUiState(isLoading = true)
    )

    fun selectCategory(category: WorkoutCategory) {
        _selectedCategory.value = category
    }

    fun selectDifficulty(difficulty: WorkoutDifficulty?) {
        _selectedDifficulty.value = difficulty
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getRoutineWithExercises(routineId: Long) = fitnessRepository.getRoutineWithExercises(routineId)

    fun recordCompletedWorkout(
        routineId: Long,
        routineTitle: String,
        category: WorkoutCategory,
        durationSeconds: Long,
        caloriesBurned: Int,
        exercisesCompleted: Int
    ) {
        viewModelScope.launch {
            fitnessRepository.recordCompletedWorkout(
                routineId = routineId,
                routineTitle = routineTitle,
                category = category,
                durationSeconds = durationSeconds,
                caloriesBurned = caloriesBurned,
                exercisesCompleted = exercisesCompleted
            )
        }
    }

    companion object {
        fun provideFactory(fitnessRepository: FitnessRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WorkoutsViewModel(fitnessRepository) as T
                }
            }
    }
}
