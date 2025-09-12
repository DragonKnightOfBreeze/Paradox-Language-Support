package icu.windea.pls.ai.services

import com.intellij.openapi.application.runReadAction
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.model.requests.TranslateLocalisationAiRequest
import icu.windea.pls.ai.providers.ChatModelManager
import icu.windea.pls.ai.util.manipulators.ParadoxLocalisationAiManipulator
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationManipulator
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import kotlinx.coroutines.runBlocking
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class TranslateLocalisationAiServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testOpenAi() {
        System.setProperty("pls.ai.providerType", "OPEN_AI")
        doTest()
    }

    fun testAnthropic() {
        System.setProperty("pls.ai.providerType", "ANTHROPIC")
        doTest()
    }

    private fun doTest() {
        myFixture.configureByFile("ai/t_wilderness_l_simp_chinese.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        val elements = ParadoxLocalisationManipulator.buildSequence(file)
        val contexts = runReadAction { elements.map { ParadoxLocalisationContext.from(it) } }.toList()
        val request = TranslateLocalisationAiRequest(file.project, file, contexts, CwtLocaleConfig.resolveFallback(), null)
        val aiService = PlsAiFacade.getTranslateLocalisationService()
        val resultFlow = aiService.manipulate(request)
        runBlocking {
            ParadoxLocalisationAiManipulator.collectResultFlow(request, resultFlow)
        }
        Assert.assertEquals(contexts.size, request.index)

        println("AI SERVICE PROVIDER:")
        println(ChatModelManager.getProviderType())
        println("AI OUTPUT:")
        println(ParadoxLocalisationManipulator.joinText(contexts))
    }
}
