package icu.windea.pls.core

import com.intellij.openapi.util.SystemInfo
import icu.windea.pls.core.execution.CommandType
import org.junit.Assert
import org.junit.Assume
import org.junit.Test

class ExecutionExtensionsTest {
    @Test
    fun executeCommandLine_string_smokeTest() {
        Assume.assumeTrue("Windows only", SystemInfo.isWindows)
        Assert.assertEquals("hello", executeCommandLine("echo hello"))
    }

    @Test
    fun executeCommandLine_string_withCommandType_smokeTest() {
        Assume.assumeTrue("Windows only", SystemInfo.isWindows)
        Assert.assertEquals("hello", executeCommandLine("echo hello", CommandType.POWER_SHELL))
    }

    @Test
    fun executeCommandLine_list_smokeTest() {
        Assume.assumeTrue("Windows only", SystemInfo.isWindows)
        Assert.assertEquals("hello", executeCommandLine(listOf("cmd", "/c", "echo", "hello")))
    }

    @Test
    fun executeCommandLine_failure_smokeTest() {
        Assume.assumeTrue("Windows only", SystemInfo.isWindows)
        Assert.assertThrows(Exception::class.java) {
            executeCommandLine("definitely-not-a-real-command-xyz-123")
        }
    }
}
