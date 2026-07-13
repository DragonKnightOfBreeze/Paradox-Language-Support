package icu.windea.pls.lang.manipulation

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.collections.context
import icu.windea.pls.core.collections.forward
import icu.windea.pls.lang.psi.conditional
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxScriptFileManipulationService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptFileManipulationServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun members_basicAndOptions() {
        myFixture.configureByFile("features/walking/walking_test_1.test.txt")
        val file = myFixture.file as ParadoxScriptFile

        run {
            val names = ParadoxScriptFileManipulationService.members(file).map { it.text.trim() }.toList()
            Assert.assertEquals(3, names.size)
            Assert.assertTrue(names[0].startsWith("a"))
            Assert.assertTrue(names[1].startsWith("b"))
            Assert.assertTrue(names[2].startsWith("e"))
        }
        run {
            val names = ParadoxScriptFileManipulationService.members(file)
                .context { conditional(true) }
                .map { it.text.trim() }
                .toList()
            Assert.assertEquals(5, names.size)
            Assert.assertTrue(names.any { it.startsWith("c") })
            Assert.assertTrue(names.any { it.startsWith("d") })
        }
        run {
            val names = ParadoxScriptFileManipulationService.members(file)
                .context { conditional(true) + forward(false) }
                .map { it.text.trim() }
                .toList()
            Assert.assertEquals(listOf("e = 5", "d = 4", "c = 3", "b = 2", "a = 1"), names)
        }
    }

    @Test
    fun members_fromFileAndBlock() {
        myFixture.configureByFile("features/walking/walking_test_2.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val block = file.block
        Assert.assertNotNull(block)
        val membersFromFile = ParadoxScriptFileManipulationService.members(file).map { it.text.trim() }.toList()
        val membersFromBlock = ParadoxScriptFileManipulationService.members(block!!).map { it.text.trim() }.toList()
        Assert.assertEquals(membersFromFile, membersFromBlock)
    }

    @Test
    fun members_emptyFile() {
        myFixture.configureByFile("features/walking/walking_test_empty.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val members = ParadoxScriptFileManipulationService.members(file).toList()
        Assert.assertTrue(members.isEmpty())
    }
}
