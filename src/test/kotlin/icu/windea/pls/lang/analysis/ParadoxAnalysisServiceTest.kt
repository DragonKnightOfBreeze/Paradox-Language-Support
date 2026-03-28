package icu.windea.pls.lang.analysis

import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ParadoxAnalysisServiceTest : BasePlatformTestCase() {
    @Test
    fun isIgnoredFile() {
        Assert.assertTrue(checkIgnoredFile("readme.txt"))
        Assert.assertTrue(checkIgnoredFile("changelog.txt"))
        Assert.assertTrue(checkIgnoredFile("license.txt"))
        Assert.assertTrue(checkIgnoredFile("credits.txt"))
        Assert.assertTrue(checkIgnoredFile("99_README_CONCEPTS.txt"))
        Assert.assertTrue(checkIgnoredFile("99_README_GRAMMAR.txt"))
        Assert.assertTrue(checkIgnoredFile("99_README_SAVED_LEADERS.txt"))
        Assert.assertTrue(checkIgnoredFile("readme_1.txt"))
        Assert.assertTrue(checkIgnoredFile("README_2.txt"))
        Assert.assertFalse(checkIgnoredFile("HI_README.txt"))
        Assert.assertFalse(checkIgnoredFile("99_advanced_documentation.txt"))
    }

    private fun checkIgnoredFile(fileName: String): Boolean {
        val file = LightVirtualFile(fileName)
        return ParadoxAnalysisService.isIgnoredFile(file)
    }
}
