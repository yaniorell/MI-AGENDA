package com.miagenda.app.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.repository.CategoryRepository
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryManagerUiState(
    val categories: List<Category> = emptyList(),
    val name: String = "",
    val colorArgb: Int = categoryPalette.first(),
    val nameError: Boolean = false
)

val categoryPalette: List<Int> = listOf(
    0xFF7252B8.toInt(),
    0xFF24C5D9.toInt(),
    0xFFFF829F.toInt(),
    0xFF7AA2FF.toInt(),
    0xFF3DDC97.toInt(),
    0xFFFFD66B.toInt(),
    0xFFC5C8E8.toInt(),
    0xFFF0A3C2.toInt(),
    0xFF8E93B8.toInt()
)

class CategoryManagerViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val stateInternal = MutableStateFlow(CategoryManagerUiState())
    val uiState: StateFlow<CategoryManagerUiState> = stateInternal.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.observeAll().collect { categories ->
                stateInternal.update { it.copy(categories = categories) }
            }
        }
    }

    fun onNameChange(value: String) {
        stateInternal.update { it.copy(name = value, nameError = false) }
    }

    fun onColorChange(colorArgb: Int) {
        stateInternal.update { it.copy(colorArgb = colorArgb) }
    }

    fun save() {
        val current = stateInternal.value
        if (current.name.isBlank()) {
            stateInternal.update { it.copy(nameError = true) }
            return
        }
        viewModelScope.launch {
            val stamp = LocalDateTime.now()
            val nextOrder = (current.categories.maxOfOrNull { it.sortOrder } ?: -1) + 1
            categoryRepository.insert(
                Category(
                    id = 0,
                    name = current.name,
                    colorArgb = current.colorArgb,
                    iconKey = "label",
                    sortOrder = nextOrder,
                    isArchived = false,
                    createdAt = stamp,
                    updatedAt = stamp
                )
            )
            stateInternal.update { it.copy(name = "") }
        }
    }

    fun setArchived(category: Category, archived: Boolean) {
        viewModelScope.launch {
            categoryRepository.update(category.copy(isArchived = archived))
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch {
            categoryRepository.delete(category.id)
        }
    }
}
