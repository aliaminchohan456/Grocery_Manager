package com.example.grocerymanager.feature.categories

import app.cash.turbine.test
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.repository.CategoryRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = mockk<CategoryRepository>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `askDelete sets pending for default categories`() = runTest(dispatcher) {
        coEvery { repo.observeCategories() } returns flowOf(
            listOf(
                Category(1, "Dairy", "Egg", "#000", isDefault = true, sortOrder = 1),
                Category(2, "Mine", "Box", "#999", isDefault = false, sortOrder = 100),
            ),
        )
        val vm = CategoriesViewModel(repo)
        advanceUntilIdle()

        vm.askDelete(Category(1, "Dairy", "Egg", "#000", isDefault = true, sortOrder = 1))
        advanceUntilIdle()

        assertThat(vm.state.value.pendingDelete).isNotNull()
        assertThat(vm.state.value.pendingDelete?.id).isEqualTo(1L)
    }

    @Test
    fun `askDelete sets pending for custom categories`() = runTest(dispatcher) {
        coEvery { repo.observeCategories() } returns flowOf(
            listOf(Category(2, "Mine", "Box", "#999", isDefault = false, sortOrder = 100)),
        )
        val vm = CategoriesViewModel(repo)
        advanceUntilIdle()

        vm.askDelete(Category(2, "Mine", "Box", "#999", isDefault = false, sortOrder = 100))
        advanceUntilIdle()

        assertThat(vm.state.value.pendingDelete).isNotNull()
        assertThat(vm.state.value.pendingDelete?.id).isEqualTo(2L)
    }

    @Test
    fun `confirmDelete invokes repo and clears pending`() = runTest(dispatcher) {
        coEvery { repo.observeCategories() } returns flowOf(
            listOf(Category(2, "Mine", "Box", "#999", isDefault = false, sortOrder = 100)),
        )
        coEvery { repo.delete(2L) } returns true
        val vm = CategoriesViewModel(repo)
        advanceUntilIdle()

        vm.askDelete(Category(2, "Mine", "Box", "#999", isDefault = false, sortOrder = 100))
        advanceUntilIdle()
        vm.confirmDelete()
        advanceUntilIdle()

        coVerify { repo.delete(2L) }
        assertThat(vm.state.value.pendingDelete).isNull()
    }
}
