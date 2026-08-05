package icu.windea.pls.core.execution

import com.intellij.openapi.util.SystemInfo
import org.junit.Assert
import org.junit.Assume
import org.junit.Test

/**
 * @see CommandLineExecutor
 */
class CommandLineExecutorTest {
    @Test
    fun testUtf8OutputForCmd() {
        Assume.assumeTrue("Windows only", SystemInfo.isWindows)

        val command = "echo 中文测试"
        val expect = "中文测试"
        Assert.assertEquals(expect, CommandLineExecutor().execute(command, CommandType.CMD))
    }

    @Test
    fun testUtf8OutputForPowerShell() {
        Assume.assumeTrue("Windows only", SystemInfo.isWindows)

        val command = "echo 中文测试"
        val expect = "中文测试"
        Assert.assertEquals(expect, CommandLineExecutor().execute(command, CommandType.POWER_SHELL))
    }
}
