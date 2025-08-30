package icu.windea.pls.localisation

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationSyntaxPsiTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testSampleBasic() {
        myFixture.configureByFile("localisation/t_syntax_sample.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        Assert.assertNotNull(file.propertyList)
        Assert.assertTrue(file.properties.isNotEmpty())
    }

    fun testTextFormatsAndIcons() {
        myFixture.configureByFile("localisation/t_syntax_text_formats_and_text_icons.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        Assert.assertNotNull(file.propertyList)
    }

    fun testGameTypeSupport_TextFormat_ck3_true() {
        myFixture.configureByFile("localisation/t_ck3_syntax_text_formats.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        Assert.assertTrue(ParadoxSyntaxConstraint.LocalisationTextFormat.supports(file))
    }

    fun testGameTypeSupport_TextFormat_stellaris_false() {
        myFixture.configureByFile("localisation/t_stellaris_syntax_text_formats.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        Assert.assertFalse(ParadoxSyntaxConstraint.LocalisationTextFormat.supports(file))
    }

    fun testGameTypeSupport_TextIcon_vic3_true() {
        myFixture.configureByFile("localisation/t_vic3_syntax_text_icons.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        Assert.assertTrue(ParadoxSyntaxConstraint.LocalisationTextIcon.supports(file))
    }
}
