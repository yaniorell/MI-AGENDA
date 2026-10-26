package com.miagenda.app.ui.category

import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.repository.CategoryRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategorySaveTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun siGuardarFallaElNombreSeQueda() = runTest(dispatcher) {
        val repository = FakeCategories(fail = true)
        val viewModel = CategoryManagerViewModel(repository)
        viewModel.onNameChange("Club")
        viewModel.save()
        advanceUntilIdle()
        assertEquals("Club", viewModel.uiState.value.name)
        assertTrue(viewModel.uiState.value.saveFailed)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun siGuardarSaleBienSeLimpiaElNombre() = runTest(dispatcher) {
        val repository = FakeCategories(fail = false)
        val viewModel = CategoryManagerViewModel(repository)
        viewModel.onNameChange("Club")
        viewModel.save()
        advanceUntilIdle()
        assertEquals("", viewModel.uiState.value.name)
        assertFalse(viewModel.uiState.value.saveFailed)
        assertEquals("Club", repository.saved.single().name)
    }

    @Test
    fun unNombreEscritoDuranteElGuardadoNoSeBorra() = runTest(dispatcher) {
        val repository = FakeCategories(fail = false)
        val viewModel = CategoryManagerViewModel(repository)
        viewModel.onNameChange("Club")
        viewModel.save()
        viewModel.onNameChange("Club de lectura")
        advanceUntilIdle()
        assertEquals("Club de lectura", viewModel.uiState.value.name)
        assertEquals("Club", repository.saved.single().name)
    }
}

private class FakeCategories(
    private val fail: Boolean
) : CategoryRepository {
    val saved = mutableListOf<Category>()
    private val categories = MutableStateFlow<List<Category>>(emptyList())

    override fun observeAll(): Flow<List<Category>> = categories

    override suspend fun getAll(): List<Category> = categories.value

    override suspend fun get(id: Long): Category? = categories.value.firstOrNull { it.id == id }

    override suspend fun count(): Int = categories.value.size

    override suspend fun insert(category: Category): Category {
        if (fail) {
            throw IOException("disk")
        }
        val stored = category.copy(id = (saved.size + 1).toLong())
        saved += stored
        categories.value = categories.value + stored
        return stored
    }

    override suspend fun update(category: Category): Category = category

    override suspend fun delete(id: Long) = Unit
}
