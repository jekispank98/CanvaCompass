package dev.jekis.canvacompass.benchmark

import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.uiAutomator

private const val TARGET_PACKAGE = "dev.jekis.canvacompass"
private const val DEFAULT_ITERATIONS = 5

@LargeTest
@RunWith(AndroidJUnit4::class)
class SampleStartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startup() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        iterations = DEFAULT_ITERATIONS,
    ) {
        uiAutomator { startApp(TARGET_PACKAGE) }
    }
}