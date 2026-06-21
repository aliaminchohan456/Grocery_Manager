package com.example.grocerymanager.feature.selectcurrency

import com.example.grocerymanager.core.common.Currencies
import com.example.grocerymanager.core.common.CurrencyOption
import com.example.grocerymanager.core.preferences.ThemeMode
import com.example.grocerymanager.core.preferences.UnitSystem
import com.example.grocerymanager.core.preferences.UserPreferences
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
class SelectCurrencyViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var preferences: FakePreferences
    private lateinit var vm: SelectCurrencyViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        preferences = FakePreferences(
            UserPreferences(
                onboardingComplete = true,
                setupComplete = false,
                currencyCode = "PKR",
                unitSystem = UnitSystem.Metric,
                themeMode = ThemeMode.System,
                alertThreshold = 0.8f,
            ),
        )
        vm = SelectCurrencyViewModel(preferences)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `state reflects selected currency from preferences`() = runTest(dispatcher) {
        advanceUntilIdle()
        val s = vm.state.value
        assertThat(s.selectedCode).isEqualTo("PKR")
    }

    @Test
    fun `default state shows full list when query is empty`() = runTest(dispatcher) {
        advanceUntilIdle()
        val s = vm.state.value
        assertThat(s.filtered).isNotEmpty()
        // filtered must contain every supported currency when the query is empty.
        assertThat(s.filtered.size).isEqualTo(Currencies.supported.size)
    }

    @Test
    fun `setting query filters list by code`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.setQuery("pkr")
        advanceUntilIdle()
        val s = vm.state.value
        assertThat(s.query).isEqualTo("pkr")
        assertThat(s.filtered).isNotEmpty()
        assertThat(s.filtered.any { it.code == "PKR" }).isTrue()
    }

    @Test
    fun `setting query filters list by display name`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.setQuery("pakistan")
        advanceUntilIdle()
        val s = vm.state.value
        assertThat(s.filtered.any { it.code == "PKR" }).isTrue()
    }

    @Test
    fun `no match returns empty list`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.setQuery("zzznotacurrency")
        advanceUntilIdle()
        val s = vm.state.value
        assertThat(s.filtered).isEmpty()
    }

    @Test
    fun `selectCurrency persists the chosen code`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.selectCurrency("USD")
        advanceUntilIdle()
        assertThat(preferences.lastSetCurrencyCode).isEqualTo("USD")
        // The state picks up the new selected code from preferences.
        val s = vm.state.value
        assertThat(s.selectedCode).isEqualTo("USD")
    }
}

private class FakePreferences(
    initial: UserPreferences = UserPreferences(),
) : UserPreferencesRepository(context = mockk(relaxed = true)) {
    private val flow = MutableStateFlow(initial)
    override val preferences: kotlinx.coroutines.flow.Flow<UserPreferences> = flow
    var setupComplete: Boolean = initial.setupComplete
    var lastSetCurrencyCode: String? = null
    override suspend fun setOnboardingComplete(complete: Boolean) {}
    override suspend fun setSetupComplete(complete: Boolean) {
        setupComplete = complete
        flow.value = flow.value.copy(setupComplete = complete)
    }
    override suspend fun setCurrencyCode(code: String) {
        lastSetCurrencyCode = code
        flow.value = flow.value.copy(currencyCode = code)
    }
    override suspend fun setUnitSystem(system: UnitSystem) {}
    override suspend fun setThemeMode(mode: ThemeMode) {}
    override suspend fun setAlertThreshold(value: Float) {}
    override suspend fun clearAll() {}
}
