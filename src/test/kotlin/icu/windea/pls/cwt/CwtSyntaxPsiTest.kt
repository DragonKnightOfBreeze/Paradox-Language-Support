package icu.windea.pls.cwt

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.cwt.psi.CwtFile
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class CwtSyntaxPsiTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testExtendedConfigs() {
        myFixture.configureByFile("cwt/t_syntax_extended_configs.cwt")
        val file = myFixture.file as CwtFile
        Assert.assertNotNull(file.block)
        Assert.assertTrue(file.propertyList.isNotEmpty())
    }

    fun testTemplateExpression() {
        myFixture.configureByFile("cwt/t_syntax_template_expression.cwt")
        val file = myFixture.file as CwtFile
        Assert.assertNotNull(file.block)
    }

    fun testTypeKeyPrefix() {
        myFixture.configureByFile("cwt/t_syntax_type_key_prefix.cwt")
        val file = myFixture.file as CwtFile
        Assert.assertNotNull(file.block)
    }
}
