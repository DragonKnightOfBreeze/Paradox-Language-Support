package icu.windea.pls.ai.services

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ai.manipulation.ParadoxLocalisationAiManipulationService
import icu.windea.pls.ai.model.requests.TranslateLocalisationAiRequest
import icu.windea.pls.ai.providers.ChatModelManager
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.lang.manipulation.ParadoxLocalisationManipulationContext
import icu.windea.pls.lang.manipulation.ParadoxLocalisationManipulationService
import icu.windea.pls.lang.psi.ParadoxPsiSequenceBuilder
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.test.ChronicleAssume
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class TranslateLocalisationAiServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = ChronicleAssume.includeAi()

    @After
    fun doTearDown() {
        System.clearProperty("pls.ai.providerType")
    }

    @Test
    fun testOpenAi() {
        System.setProperty("pls.ai.providerType", "OPEN_AI")
        doTest()
    }

    @Test
    fun testAnthropic() {
        System.setProperty("pls.ai.providerType", "ANTHROPIC")
        doTest()
    }

    private fun doTest() {
        myFixture.configureByFile("ai/wilderness_l_simp_chinese_stellaris.test.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        val elements = ParadoxPsiSequenceBuilder.localisations(file)
        val contexts = runSmartReadAction { elements.map { ParadoxLocalisationManipulationContext.create(it) } }.toList()
        val request = TranslateLocalisationAiRequest(file.project, file, contexts, CwtLocaleConfig.resolveFallback(), null)
        val aiService = TranslateLocalisationAiService.getInstance()
        val resultFlow = aiService.manipulate(request)
        runBlocking {
            ParadoxLocalisationAiManipulationService.collectResultFlow(request, resultFlow)
        }
        assertEquals(contexts.size, request.index)

        println("AI SERVICE PROVIDER:")
        println(ChatModelManager.getProviderType())
        println("AI OUTPUT:")
        println(ParadoxLocalisationManipulationService.joinText(contexts))
    }
}
