package com.example.grocerymanager.feature.setup

import com.example.grocerymanager.core.preferences.ThemeMode
import com.example.grocerymanager.core.preferences.UnitSystem
import com.example.grocerymanager.core.preferences.UserPreferences
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.domain.usecase.SetMonthlyBudgetUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetupViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val preferences = FakePreferences()
    private val setBudget = mockk<SetMonthlyBudgetUseCase>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state reflects initial preferences and editable budget input`() = runTest(dispatcher) {
        val vm = SetupViewModel(preferences, setBudget)
        advanceUntilIdle()

        val s = vm.state.value
        assertThat(s.currencyCode).isEqualTo("PKR")
        assertThat(s.themeMode).isEqualTo(ThemeMode.Dark)
        assertThat(s.unitSystem).isEqualTo(UnitSystem.Imperial)
    }

    @Test
    fun `setBudget updates local editable input without losing prefs`() = runTest(dispatcher) {
        val vm = SetupViewModel(preferences, setBudget)
        advanceUntilIdle()

        vm.setBudget("12.30")
        advanceUntilIdle()

        val s = vm.state.value
        assertThat(s.budgetInput).isEqualTo("12.30")
        assertThat(s.budgetError).isNull()
        // Preferences were untouched.
        assertThat(s.currencyCode).isEqualTo("PKR")
    }

    @Test
    fun `setBudget invalid input sets error`() = runTest(dispatcher) {
        val vm = SetupViewModel(preferences, setBudget)
        advanceUntilIdle()

        vm.setBudget("abc")
        advanceUntilIdle()

        val s = vm.state.value
        assertThat(s.budgetError).isNotNull()
    }

    @Test
    fun `completeSetup with budget calls SetMonthlyBudgetUseCase and setSetupComplete`() = runTest(dispatcher) {
        val vm = SetupViewModel(preferences, setBudget)
        advanceUntilIdle()

        vm.setBudget("1500")
        advanceUntilIdle()
        vm.completeSetup()
        advanceUntilIdle()

        coVerify { setBudget(any(), any(), 150_000L, any()) }
        assertThat(preferences.setupCompleted).isTrue()
    }

    @Test
    fun `completeSetup with empty budget skips SetMonthlyBudgetUseCase but still sets setup complete`() = runTest(dispatcher) {
        val vm = SetupViewModel(preferences, setBudget)
        advanceUntilIdle()

        vm.completeSetup()
        advanceUntilIdle()

        coVerify(exactly = 0) { setBudget(any(), any(), any(), any()) }
        assertThat(preferences.setupCompleted).isTrue()
    }
}

private class FakePreferences(
    context: android.content.Context = mockk(relaxed = true),
) : UserPreferencesRepository(context) {
    private val flow = MutableStateFlow(
        UserPreferences(
            onboardingComplete = false,
            setupComplete = false,
            currencyCode = "PKR",
            unitSystem = UnitSystem.Imperial,
            themeMode = ThemeMode.Dark,
            alertThreshold = 0.9f,
        ),
    )
    override val preferences: kotlinx.coroutines.flow.Flow<UserPreferences> = flow
    var setupCompleted = false
    override suspend fun setSetupComplete(complete: Boolean) { setupCompleted = complete }
}
