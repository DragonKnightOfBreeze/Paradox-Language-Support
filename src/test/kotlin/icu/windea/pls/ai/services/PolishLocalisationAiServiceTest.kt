package icu.windea.pls.ai.services

import com.intellij.openapi.application.*
import com.intellij.testFramework.*
import com.intellij.testFramework.fixtures.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.manipulators.*
import icu.windea.pls.lang.util.manipulators.*
import icu.windea.pls.localisation.psi.*
import kotlinx.coroutines.*
import org.junit.*

@TestDataPath("\$CONTENT_ROOT/testData")
class PolishLocalisationAiServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun test() {
        myFixture.configureByFile("ai/t_wilderness_l_simp_chinese.yml")
        val file = myFixture.file as ParadoxLocalisationFile
        val elements = ParadoxLocalisationManipulator.buildSequence(file)
        val contexts = runReadAction { elements.map { ParadoxLocalisationContext.from(it) } }.toList()
        val description = "用华丽而夸张的文字，强调生体荒野（即共生智能）是完美的"
        val request = PolishLocalisationAiRequest(file.project, file, contexts, description)
        val aiService = PlsAiFacade.getPolishLocalisationService()
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
