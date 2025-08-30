package icu.windea.pls.script

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.findChild
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptSyntaxPsiTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testTemplateExpression() {
        myFixture.configureByFile("script/t_syntax_template_expression.txt")
        val file = myFixture.file as ParadoxScriptFile
        Assert.assertNotNull(file.block)
    }

    fun testAttachedCommentsBasic() {
        myFixture.configureByFile("script/t_syntax_attached_comments.txt")
        val file = myFixture.file as ParadoxScriptFile
        val rootBlock = file.block!!
        val rootProperty = rootBlock.findChild<ParadoxScriptProperty> { it.name == "root" }
        Assert.assertNotNull(rootProperty)
    }

    fun testTypeKeyPrefix() {
        myFixture.configureByFile("script/t_syntax_type_key_prefix.txt")
        val file = myFixture.file as ParadoxScriptFile
        Assert.assertNotNull(file.block)
    }
    
    fun testCodeStyleSettingsSample() {
        myFixture.configureByFile("script/t_syntax_codesettings.txt")
        val file = myFixture.file as ParadoxScriptFile
        Assert.assertNotNull(file.block)
    }

    fun testEmptyFile() {
        myFixture.configureByFile("script/t_syntax_empty.txt")
        val file = myFixture.file as ParadoxScriptFile
        Assert.assertNull(file.block)
    }
}
