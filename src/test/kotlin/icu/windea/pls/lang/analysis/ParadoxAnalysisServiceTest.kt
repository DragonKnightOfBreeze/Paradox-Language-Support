package icu.windea.pls.lang.analysis

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.model.paths.ParadoxPath
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ParadoxAnalysisServiceTest : BasePlatformTestCase() {
    @Test
    fun isIgnoredFile_topLevel() {
        Assert.assertTrue(checkIgnoredFile("readme.txt"))
        Assert.assertTrue(checkIgnoredFile("changelog.txt"))
        Assert.assertTrue(checkIgnoredFile("license.txt"))
        Assert.assertTrue(checkIgnoredFile("credits.txt"))
        Assert.assertTrue(checkIgnoredFile("test.txt"))
        Assert.assertFalse(checkIgnoredFile("common/test.txt"))
        Assert.assertFalse(checkIgnoredFile("test.yml"))
        Assert.assertFalse(checkIgnoredFile("common/test.yml"))
        Assert.assertFalse(checkIgnoredFile("localisation/test.yml"))
        Assert.assertFalse(checkIgnoredFile("descriptor.mod"))
        Assert.assertFalse(checkIgnoredFile("common/descriptor.mod"))
    }

    @Test
    fun isIgnoredFile_forced() {
        Assert.assertTrue(checkIgnoredFile("common/readme.txt"))
        Assert.assertTrue(checkIgnoredFile("common/changelog.txt"))
        Assert.assertTrue(checkIgnoredFile("common/license.txt"))
        Assert.assertTrue(checkIgnoredFile("common/credits.txt"))
        Assert.assertTrue(checkIgnoredFile("common/99_README_CONCEPTS.txt"))
        Assert.assertTrue(checkIgnoredFile("common/99_README_GRAMMAR.txt"))
        Assert.assertTrue(checkIgnoredFile("common/99_README_SAVED_LEADERS.txt"))
        Assert.assertTrue(checkIgnoredFile("common/readme_1.txt"))
        Assert.assertTrue(checkIgnoredFile("common/README_2.txt"))
        Assert.assertFalse(checkIgnoredFile("common/HI_README.txt"))
        Assert.assertFalse(checkIgnoredFile("common/99_advanced_documentation.txt"))
    }

    private fun checkIgnoredFile(path: String, entry: String = ""): Boolean {
        return ParadoxAnalysisService.isIgnoredFile(ParadoxPath.resolve(path), entry)
    }
}
