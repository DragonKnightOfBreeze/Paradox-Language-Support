package icu.windea.pls.core.util.console

import org.jdesktop.swingx.util.OS
import org.junit.Assert
import org.junit.Assume
import org.junit.Test

class CommandExecutorTest {
    @Test
    fun testUtf8OutputForCmd() {
        Assume.assumeTrue("Windows only", OS.isWindows())

        val command = "echo 中文测试"
        val expect = "中文测试"
        Assert.assertEquals(expect, CommandExecutor().execute(command, CommandType.CMD))
    }

    @Test
    fun testUtf8OutputForPowerShell() {
        Assume.assumeTrue("Windows only", OS.isWindows())

        val command = "echo 中文测试"
        val expect = "中文测试"
        Assert.assertEquals(expect, CommandExecutor().execute(command, CommandType.POWER_SHELL))
    }
}
