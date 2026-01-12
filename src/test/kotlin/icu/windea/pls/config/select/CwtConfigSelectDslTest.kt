package icu.windea.pls.config.select

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.configGroup.CwtConfigGroupImpl
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.markIntegrationTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtConfigSelectDslTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @Test
    fun byPath_simple() {
        val fileConfig = resolveFileConfig("features/select/select_test_1.test.cwt")
        val k4 = selectScope { fileConfig.ofPath("k1/k2/k3/k4").one() }
        Assert.assertNotNull(k4)
        val k4List = selectScope { fileConfig.ofPath("k1/k2/k3/k4").all() }
        Assert.assertEquals(3, k4List.size)
    }

    private fun resolveFileConfig(path: String): CwtFileConfig {
        myFixture.configureByFile(path)
        val file = myFixture.file as CwtFile
        val configGroup = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)
        val fileConfig = CwtFileConfig.resolve(file, configGroup, file.name)
        return fileConfig
    }
}
