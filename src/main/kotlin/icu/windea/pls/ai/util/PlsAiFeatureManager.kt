package icu.windea.pls.ai.util

import com.intellij.openapi.diagnostic.*
import dev.langchain4j.model.output.*
import dev.langchain4j.service.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.messages.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.model.*
import java.lang.invoke.*

object PlsAiFeatureManager {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    private fun loggerRequest(gameType: ParadoxGameType, userMessage: String) {
        logger.info("[AI REQUEST] game type: $gameType, user message:\n$userMessage")
    }

    private fun loggerResponse(result: Result<List<ParadoxLocalisationData>>) {
        logger.info("[AI RESPONSE] finish reason: ${result.finishReason()}, token usage: ${result.tokenUsage()}, content:\n${result.content()}")
    }

    //region Translate Localisation

    fun translateLocalisation(request: TranslateLocalisationsRequest): List<ParadoxLocalisationData>? {
        if (request.localisations.isEmpty()) return emptyList()
        val userMessage = PlsAiUserMessages.translateLocalisation(request)
        val gameType = request.gameType ?: return null

        return translateLocalisation(userMessage, gameType)
    }

    fun translateLocalisation(userMessage: String, gameType: ParadoxGameType): List<ParadoxLocalisationData>? {
        loggerRequest(gameType, userMessage)
        val aiService = PlsAiServiceManager.getAiService()
        val result = aiService.translateLocalisation(userMessage, gameType)
        loggerResponse(result)

        if (result.finishReason() != FinishReason.STOP) throw PlsAiRequestException(result)
        return result.content()
    }

    //endregion

    //endregion Colorize Localisation

    fun colorizeLocalisation(request: ColorizeLocalisationsRequest): List<ParadoxLocalisationData>? {
        if (request.localisations.isEmpty()) return emptyList()
        val userMessage = PlsAiUserMessages.colorizeLocalisation(request)
        val gameType = request.gameType ?: return null

        return colorizeLocalisation(userMessage, gameType)
    }

    fun colorizeLocalisation(userMessage: String, gameType: ParadoxGameType): List<ParadoxLocalisationData>? {
        loggerRequest(gameType, userMessage)
        val aiService = PlsAiServiceManager.getAiService()
        val result = aiService.colorizeLocalisation(userMessage, gameType)
        loggerResponse(result)

        if (result.finishReason() != FinishReason.STOP) throw PlsAiRequestException(result)
        return result.content()
    }

    //endregion
}
