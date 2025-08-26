package icu.windea.pls.core.util.console

import org.junit.*

class CommandExecutorTest {
    @Test
    fun testUtf8OutputForCmd() {
        val command = "echo 中文测试"
        val expect = "中文测试"
        Assert.assertEquals(expect, CommandExecutor().execute(command, CommandType.CMD))
    }

    @Test
    fun testUtf8OutputForPowerShell() {
        val command = "echo 中文测试"
        val expect = "中文测试"
        Assert.assertEquals(expect, CommandExecutor().execute(command, CommandType.POWER_SHELL))
    }
}
