package icu.windea.pls.localisation

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationMoreSyntaxPsiTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testAdvancedCombined() {
        myFixture.configureByFile("localisation/t_syntax_advanced_combined.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        Assert.assertNotNull(file.propertyList)
        Assert.assertEquals(5, file.properties.size)
    }

    fun testBoundaryOnlyHeader() {
        myFixture.configureByFile("localisation/t_syntax_only_header.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        Assert.assertNotNull(file.propertyList)
        Assert.assertTrue(file.properties.isEmpty())
    }

    fun testBoundaryOnlyComments() {
        myFixture.configureByFile("localisation/t_syntax_only_comments.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        Assert.assertNull(file.propertyList)
        Assert.assertTrue(file.properties.isEmpty())
    }

    fun testErrorUnclosedQuote() {
        myFixture.configureByFile("localisation/t_syntax_error_unclosed_quote.yml")
        val errors = PsiTreeUtil.findChildrenOfType(myFixture.file, PsiErrorElement::class.java)
        Assert.assertTrue(errors.isNotEmpty())
    }
}
