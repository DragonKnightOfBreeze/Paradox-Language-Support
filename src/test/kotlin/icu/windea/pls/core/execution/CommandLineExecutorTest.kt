package icu.windea.pls.core.execution

import org.jdesktop.swingx.util.OS
import org.junit.Assert
import org.junit.Assume
import org.junit.Test

class CommandLineExecutorTest {
    @Test
    fun testUtf8OutputForCmd() {
        Assume.assumeTrue("Windows only", OS.isWindows())

        val command = "echo 中文测试"
        val expect = "中文测试"
        Assert.assertEquals(expect, CommandLineExecutor().execute(command, CommandType.CMD))
    }

    @Test
    fun testUtf8OutputForPowerShell() {
        Assume.assumeTrue("Windows only", OS.isWindows())

        val command = "echo 中文测试"
        val expect = "中文测试"
        Assert.assertEquals(expect, CommandLineExecutor().execute(command, CommandType.POWER_SHELL))
    }
}
