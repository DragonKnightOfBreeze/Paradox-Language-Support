package icu.windea.pls.ai.util

import com.google.common.cache.*
import dev.langchain4j.service.*
import icu.windea.pls.ai.messages.PlsAiSystemMessages
import icu.windea.pls.ai.services.*
import icu.windea.pls.ai.tools.*

object PlsAiServiceManager {
    val services: Cache<String, Any> = CacheBuilder.newBuilder().build()

    fun getAiService(): PlsAiService {
        return services.get("") { createAiService() } as PlsAiService
    }

    fun getStreamingAiService(): PlsStreamingAiService {
        return services.get("STREAMING") { createStreamingAiService() } as PlsStreamingAiService
    }

    private fun createAiService(): PlsStreamingAiService {
        return AiServices.builder(PlsStreamingAiService::class.java)
            .chatModel(PlsChatModelManager.getChatModel())
            .systemMessageProvider { PlsAiSystemMessages.general }
            .tools(PlsAiTools())
            .build()
    }

    private fun createStreamingAiService(): PlsStreamingAiService {
        return AiServices.builder(PlsStreamingAiService::class.java)
            .streamingChatModel(PlsChatModelManager.getStreamingChatModel())
            .systemMessageProvider { PlsAiSystemMessages.general }
            .tools(PlsAiTools())
            .build()
    }
}
