package icu.windea.pls.integrations.lints

import icu.windea.pls.core.executeCommand
import icu.windea.pls.core.toFile
import icu.windea.pls.test.AssumePredicates
import org.junit.Before
import org.junit.Test
import kotlin.time.measureTime

class RunTigerTest {
    @Before
    fun setup() = AssumePredicates.includeLocalEnv()

    @Test
    fun runTiger() {
        val wd = "D:\\Documents\\Projects\\_Tests\\vic3-tiger-windows-v1.8.0".toFile()
        val command = "./vic3-tiger.exe --json 'D:/Documents/Projects/_Tests/gate-mod/mod' > result.json"
        val cost = measureTime {
            executeCommand(command, workDirectory = wd)
        }
        println("Cost: $cost") // 6.050044401s
    }
}
