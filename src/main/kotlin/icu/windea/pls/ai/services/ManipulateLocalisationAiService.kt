package icu.windea.pls.ai.services

import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.model.requests.ManipulateLocalisationAiRequest
import icu.windea.pls.ai.model.results.LocalisationAiResult
import kotlinx.coroutines.flow.Flow

abstract class ManipulateLocalisationAiService<R : ManipulateLocalisationAiRequest> : AiService {
    abstract fun manipulate(request: R): Flow<LocalisationAiResult>?

    protected fun getChunkSize(): Int {
        return PlsAiFacade.getSettings().features.localisationChunkSize.coerceAtLeast(1)
    }

    protected fun getMemorySize(): Int {
        return PlsAiFacade.getSettings().features.localisationMemorySize.coerceAtLeast(0)
    }

    protected fun getMemory(): ChatMemory {
        val maxMessages = getMemorySize() / getChunkSize()
        return MessageWindowChatMemory.withMaxMessages(maxMessages)
    }
}
