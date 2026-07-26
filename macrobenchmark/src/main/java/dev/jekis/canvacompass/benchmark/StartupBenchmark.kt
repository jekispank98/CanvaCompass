package dev.jekis.canvacompass.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This benchmark measures the startup performance of the app.
 * It is recommended to run these benchmarks on a physical device in a stable state.
 *
 * Use these metrics to identify performance regressions and verify the impact of Baseline Profiles.
 */

@LargeTest
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupCold() = startup(StartupMode.COLD)

    @Test
    fun startupWarm() = startup(StartupMode.WARM)

    @Test
    fun startupHot() = startup(StartupMode.HOT)

    @Test
    fun startupColdNoCompilation() = startup(
        startupMode = StartupMode.COLD,
        compilationMode = CompilationMode.None()
    )

    private fun startup(
        startupMode: StartupMode,
        compilationMode: CompilationMode = CompilationMode.DEFAULT
    ) = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilationMode,
        iterations = DEFAULT_ITERATIONS,
        startupMode = startupMode,
        setupBlock = {
            pressHome()
        }
    ) {
        startActivityAndWait()
    }

    companion object {
        const val TARGET_PACKAGE = "dev.jekis.canvacompass"
        const val DEFAULT_ITERATIONS = 10

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.executeShellCommand("settings put global window_animation_scale 0")
            device.executeShellCommand("settings put global transition_animation_scale 0")
            device.executeShellCommand("settings put global animator_duration_scale 0")
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.executeShellCommand("settings put global window_animation_scale 1")
            device.executeShellCommand("settings put global transition_animation_scale 1")
            device.executeShellCommand("settings put global animator_duration_scale 1")
        }
    }
}
