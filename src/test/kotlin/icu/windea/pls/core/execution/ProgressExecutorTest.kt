package icu.windea.pls.core.execution

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.SystemInfo
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.milliseconds

/**
 * @see ProgressExecutor
 */
@RunWith(JUnit4::class)
class ProgressExecutorTest : BasePlatformTestCase() {
    @Test
    fun execute_echo_smokeTest() {
        val output = ProgressExecutor(createEchoCommandLine("hello")).execute()

        Assert.assertEquals(0, output.exitCode)
        Assert.assertEquals("hello", output.stdout.trim())
        Assert.assertFalse(output.isTimeout)
        Assert.assertFalse(output.isCancelled)
    }

    @Test
    fun execute_echo_withoutIndicator_smokeTest() {
        val output = ProgressExecutor(createEchoCommandLine("hello"))
            .withoutIndicator()
            .execute()

        Assert.assertEquals(0, output.exitCode)
        Assert.assertEquals("hello", output.stdout.trim())
    }

    @Test
    fun execute_encoding_utf8Output_test() {
        val commandLine = if (SystemInfo.isWindows) {
            GeneralCommandLine(
                "powershell",
                "-NoProfile",
                "-NonInteractive",
                "-Command",
                "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; Write-Output '中文测试'"
            )
        } else {
            GeneralCommandLine("/bin/sh", "-c", "printf '中文测试'")
        }
        val output = ProgressExecutor(commandLine.withCharset(Charsets.UTF_8)).execute()

        Assert.assertEquals(0, output.exitCode)
        Assert.assertEquals("中文测试", output.stdout.trim())
    }

    @Test
    fun execute_git_smokeTest() {
        Assume.assumeTrue("git is not available", PathEnvironmentVariableUtil.findInPath("git") != null)

        val output = ProgressExecutor(
            GeneralCommandLine("git", "--version").withCharset(Charsets.UTF_8)
        ).withTimeout(ExecutionTimeouts.normal).execute()

        Assert.assertEquals(0, output.exitCode)
        Assert.assertTrue(output.stdout.contains("git", ignoreCase = true))
        Assert.assertFalse(output.isTimeout)
        Assert.assertFalse(output.isCancelled)
    }

    @Test
    fun execute_exitCode_and_stderr_test() {
        val output = ProgressExecutor(createFailCommandLine("boom")).execute()

        Assert.assertNotEquals(0, output.exitCode)
        Assert.assertTrue(output.stderr.contains("boom"))
        Assert.assertFalse(output.isTimeout)
        Assert.assertFalse(output.isCancelled)
    }

    @Test
    fun execute_timeout_test() {
        val startTime = System.nanoTime()
        val output = ProgressExecutor(createSleepCommandLine(10))
            .withoutIndicator()
            .withTimeout(200.milliseconds)
            .execute()
        val elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)

        Assert.assertTrue(output.isTimeout)
        Assert.assertFalse(output.isCancelled)
        Assert.assertTrue("timeout should take effect, elapsed=$elapsedMillis ms", elapsedMillis < 5000)
    }

    @Test
    fun execute_withIndicator_cancellation_test() {
        val indicator = ProgressIndicatorBase()
        indicator.start()
        val startTime = System.nanoTime()
        val cancelThread = thread(name = "progress-executor-cancel") {
            Thread.sleep(200)
            indicator.cancel()
        }
        val output = try {
            ProgressExecutor(createSleepCommandLine(10))
                .withIndicator(indicator)
                .withTimeout(ExecutionTimeouts.short)
                .execute()
        } finally {
            cancelThread.join(2000)
            indicator.stop()
        }
        val elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)

        Assert.assertTrue(output.isCancelled)
        Assert.assertFalse(output.isTimeout)
        Assert.assertTrue("cancellation should take effect, elapsed=$elapsedMillis ms", elapsedMillis < 5000)
    }

    @Test
    fun execute_withCurrentIndicator_cancellation_test() {
        val indicator = ProgressIndicatorBase()
        val startTime = System.nanoTime()
        val cancelThread = thread(name = "progress-executor-current-indicator-cancel") {
            Thread.sleep(200)
            indicator.cancel()
        }
        val output = try {
            ProgressManager.getInstance().runProcess(
                Computable {
                    ProgressExecutor(createSleepCommandLine(10))
                        .withTimeout(ExecutionTimeouts.short)
                        .execute()
                },
                indicator
            )
        } finally {
            cancelThread.join(2000)
        }
        val elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)

        Assert.assertTrue(output.isCancelled)
        Assert.assertFalse(output.isTimeout)
        Assert.assertTrue("current indicator cancellation should take effect, elapsed=$elapsedMillis ms", elapsedMillis < 5000)
    }

    @Suppress("SameParameterValue")
    private fun createEchoCommandLine(text: String): GeneralCommandLine {
        return if (SystemInfo.isWindows) {
            GeneralCommandLine("cmd", "/c", "echo $text")
        } else {
            GeneralCommandLine("/bin/sh", "-c", "echo $text")
        }
    }

    @Suppress("SameParameterValue")
    private fun createFailCommandLine(text: String): GeneralCommandLine {
        return if (SystemInfo.isWindows) {
            GeneralCommandLine("cmd", "/c", "echo $text 1>&2 & exit /b 1")
        } else {
            GeneralCommandLine("/bin/sh", "-c", "echo $text 1>&2; exit 1")
        }
    }

    @Suppress("SameParameterValue")
    private fun createSleepCommandLine(seconds: Int): GeneralCommandLine {
        return if (SystemInfo.isWindows) {
            // `ping` 的 `-n` 参数表示次数；额外增加一次以接近指定的秒数
            GeneralCommandLine("cmd", "/c", "ping", "-n", "${seconds + 1}", "127.0.0.1")
        } else {
            GeneralCommandLine("/bin/sh", "-c", "sleep $seconds")
        }
    }
}
