package com.stolz.connect.ui.settings

import app.cash.turbine.test
import com.stolz.connect.data.preferences.ThemeMode
import com.stolz.connect.data.preferences.ThemePreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var themePreferences: ThemePreferences
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        themePreferences = mockk()
    }

    @Test
    fun `initial state loads theme mode from preferences`() = runTest {
        every { themePreferences.getThemeMode() } returns ThemeMode.DARK
        viewModel = SettingsViewModel(themePreferences)

        advanceUntilIdle()

        viewModel.themeMode.test {
            assertEquals(ThemeMode.DARK, awaitItem())
        }
    }

    @Test
    fun `setThemeMode updates preferences and state`() = runTest {
        every { themePreferences.getThemeMode() } returns ThemeMode.SYSTEM
        coEvery { themePreferences.setThemeMode(ThemeMode.LIGHT) } returns Unit
        viewModel = SettingsViewModel(themePreferences)

        advanceUntilIdle()

        viewModel.setThemeMode(ThemeMode.LIGHT)
        advanceUntilIdle()

        coVerify { themePreferences.setThemeMode(ThemeMode.LIGHT) }

        viewModel.themeMode.test {
            assertEquals(ThemeMode.LIGHT, awaitItem())
        }
    }

    @Test
    fun `setThemeMode handles all theme modes`() = runTest {
        every { themePreferences.getThemeMode() } returns ThemeMode.SYSTEM
        coEvery { themePreferences.setThemeMode(any()) } returns Unit
        viewModel = SettingsViewModel(themePreferences)

        advanceUntilIdle()

        viewModel.setThemeMode(ThemeMode.LIGHT)
        advanceUntilIdle()
        coVerify { themePreferences.setThemeMode(ThemeMode.LIGHT) }

        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()
        coVerify { themePreferences.setThemeMode(ThemeMode.DARK) }

        viewModel.setThemeMode(ThemeMode.SYSTEM)
        advanceUntilIdle()
        coVerify { themePreferences.setThemeMode(ThemeMode.SYSTEM) }
    }
}
