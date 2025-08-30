package icu.windea.pls.cwt

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.cwt.psi.CwtFile
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class CwtMoreSyntaxPsiTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testAdvancedNested() {
        myFixture.configureByFile("cwt/t_syntax_advanced_nested.cwt")
        val file = myFixture.file as CwtFile
        Assert.assertNotNull(file.block)
        Assert.assertTrue(file.propertyList.isNotEmpty())
    }

    fun testBoundaryOnlyComments() {
        myFixture.configureByFile("cwt/t_syntax_only_comments.cwt")
        val file = myFixture.file as CwtFile
        Assert.assertNotNull(file.block)
        Assert.assertTrue(file.propertyList.isEmpty())
        Assert.assertTrue(file.valueList.isEmpty())
    }

    fun testErrorUnclosedBrace() {
        myFixture.configureByFile("cwt/t_syntax_error_unclosed_brace.cwt")
        val errors = PsiTreeUtil.findChildrenOfType(myFixture.file, PsiErrorElement::class.java)
        Assert.assertTrue(errors.isNotEmpty())
    }
}
