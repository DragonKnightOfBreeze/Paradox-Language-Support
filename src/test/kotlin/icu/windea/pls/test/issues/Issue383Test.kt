package icu.windea.pls.test.issues

import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.inspections.script.common.MissingParameterInspection
import icu.windea.pls.lang.inspections.script.common.UnsupportedParameterInspection
import icu.windea.pls.lang.inspections.script.common.UnusedParameterInspection
import icu.windea.pls.lang.inspections.script.expression.ConflictingResolvedExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.IncorrectExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.MissingExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.TooManyExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
import icu.windea.pls.lang.inspections.script.scope.ConflictingScopeContextInferenceInspection
import icu.windea.pls.lang.inspections.script.scope.IncorrectScopeInspection
import icu.windea.pls.lang.inspections.script.scope.IncorrectScopeLinkChainInspection
import icu.windea.pls.lang.inspections.script.scope.IncorrectScopeSwitchInspection
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.references.script.ParadoxParameterPsiReference
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.ep.resolve.config.CwtTriggerWithParametersAwareOverriddenConfigProvider

/**
 * See: [#383](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/383)
 *
 * @see CwtTriggerWithParametersAwareOverriddenConfigProvider
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue383Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/383")
        markConfigDirectory("issues/383/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testInspection_ScopeMatched() {
        enableAllNeededInspections()

        // from: stellaris@common/script_values/0_script_values.txt
        markFileInfo(ParadoxGameType.Stellaris, "common/script_values/test.txt")
        myFixture.configureByText("test.txt", """
            created_enclave_number = {
                base = 1
                complex_trigger_modifier = {
                    trigger = count_country
                    parameters = {
                        limit = { has_country_flag = created_by }
                    }
                    mode = weight
                }
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_WithParameter() {
        enableAllNeededInspections()

        // from: stellaris@common/script_values/0_script_values.txt
        markFileInfo(ParadoxGameType.Stellaris, "common/script_values/test.txt")
        myFixture.configureByText("test.txt", """
            num_starbase_modules_of_type = {
                complex_trigger_modifier = {
                    trigger = count_starbase_modules
                    parameters = {
                        type = ${'$'}TYPE$
                    }
                    mode = add
                }
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        myFixture.checkHighlighting()
    }

    @Test
    fun testReferenceResolution_ScopeMatched() {
        // from: stellaris@common/script_values/0_script_values.txt
        markFileInfo(ParadoxGameType.Stellaris, "common/script_values/test.txt")
        myFixture.configureByText("test.txt", """
            created_enclave_number = {
                base = 1
                complex_trigger_modifier = {
                    trigger = count_country
                    parameters = {
                        limit = { has_country_flag = <caret>created_by }
                    }
                    mode = weight
                }
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val reference = myFixture.findReferenceAtCaret()!!
        val resolved = reference.resolve()!!
        assertTrue(resolved is ParadoxDynamicValueLightElement && resolved.name == "created_by" && resolved.presentableType == "country_flag")

        // check scope context
        val element = myFixture.findElementAtCaret()!!
        val property = element.parentOfType<ParadoxScriptProperty>()!!
        val scopeContext = ParadoxScopeManager.getScopeContext(property)!!
        assertEquals(mapOf("this" to "country", "root" to "any", "prev" to "any"), scopeContext.toScopeIdMap())
    }

    @Test
    fun testReferenceResolution_WithParameter() {
        // from: stellaris@common/script_values/0_script_values.txt
        markFileInfo(ParadoxGameType.Stellaris, "common/script_values/test.txt")
        myFixture.configureByText("test.txt", """
            num_starbase_modules_of_type = {
                complex_trigger_modifier = {
                    trigger = count_starbase_modules
                    parameters = {
                        type = $<caret>TYPE$
                    }
                    mode = add
                }
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val reference = myFixture.findReferenceAtCaret()!!
        assertTrue(reference is PsiMultiReference)
        reference as PsiMultiReference
        val parameterReference = reference.references[0]
        assertTrue(parameterReference is ParadoxParameterPsiReference)
        val expressionReference = reference.references[1]
        assertTrue(expressionReference is ParadoxScriptExpressionPsiReference)

        parameterReference as ParadoxParameterPsiReference
        val resolved = parameterReference.resolve()!!
        assertTrue(resolved is ParadoxParameterLightElement && resolved.name == "TYPE" && resolved.contextKey == "script_value@num_starbase_modules_of_type")

        // check inferred type
        resolved as ParadoxParameterLightElement
        val inferredType = ParadoxParameterManager.getInferredType(resolved)
        assertEquals("<starbase_module>", inferredType)

        expressionReference as ParadoxScriptExpressionPsiReference
        val config = expressionReference.config
        assertEquals("<starbase_module>", config.configExpression.expressionString)

        // check scope context
        val element = myFixture.findElementAtCaret()!!
        val property = element.parentOfType<ParadoxScriptProperty>()!!
        val scopeContext = ParadoxScopeManager.getScopeContext(property)!!
        assertEquals(mapOf("this" to "any", "root" to "any"), scopeContext.toScopeIdMap())
    }

    private fun enableAllNeededInspections() {

        // enable all needed expression inspections
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)
        myFixture.enableInspections(ConflictingResolvedExpressionInspection::class.java)
        myFixture.enableInspections(MissingExpressionInspection::class.java)
        myFixture.enableInspections(TooManyExpressionInspection::class.java)
        myFixture.enableInspections(IncorrectExpressionInspection::class.java)
        // enable all scope inspections
        myFixture.enableInspections(IncorrectScopeInspection::class.java)
        myFixture.enableInspections(IncorrectScopeSwitchInspection::class.java)
        myFixture.enableInspections(IncorrectScopeSwitchInspection::class.java)
        myFixture.enableInspections(IncorrectScopeLinkChainInspection::class.java)
        myFixture.enableInspections(ConflictingScopeContextInferenceInspection::class.java)
        // enable all parameter inspections
        myFixture.enableInspections(UnsupportedParameterInspection::class.java)
        myFixture.enableInspections(UnusedParameterInspection::class.java)
        myFixture.enableInspections(MissingParameterInspection::class.java)
    }
}
