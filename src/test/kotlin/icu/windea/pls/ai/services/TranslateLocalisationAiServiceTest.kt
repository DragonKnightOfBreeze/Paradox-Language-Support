package icu.windea.pls.ai.services

import com.intellij.openapi.application.*
import com.intellij.testFramework.*
import com.intellij.testFramework.fixtures.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.model.requests.*
import icu.windea.pls.ai.util.manipulators.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.util.manipulators.*
import icu.windea.pls.localisation.psi.*
import kotlinx.coroutines.*
import org.junit.*

@TestDataPath("\$CONTENT_ROOT/testData")
class TranslateLocalisationAiServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun test() {
        myFixture.configureByFile("ai/t_wilderness_l_simp_chinese.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        val elements = ParadoxLocalisationManipulator.buildSequence(file)
        val contexts = runReadAction { elements.map { ParadoxLocalisationContext.from(it) } }.toList()
        val request = TranslateLocalisationAiRequest(file.project, file, contexts, CwtLocaleConfig.FALLBACK, null)
        val aiService = PlsAiFacade.getTranslateLocalisationService()
        val resultFlow = aiService.manipulate(request)
        runBlocking {
            ParadoxLocalisationAiManipulator.collectResultFlow(request, resultFlow)
        }
        Assert.assertEquals(contexts.size, request.index)

        val text = ParadoxLocalisationManipulator.joinText(contexts)
        println("AI OUTPUT:")
        println(text)
    }
}
