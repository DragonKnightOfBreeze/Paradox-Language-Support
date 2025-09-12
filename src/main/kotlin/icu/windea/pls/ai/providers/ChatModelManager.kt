package icu.windea.pls.ai.providers

import com.intellij.util.application
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import icu.windea.pls.ai.PlsAiFacade

@Suppress("unused")
object ChatModelManager {
    private val providers = mapOf(
        ChatModelProviderType.OPEN_AI to OpenAiChatModelProvider(),
        ChatModelProviderType.ANTHROPIC to AnthropicChatModelProvider(),
        ChatModelProviderType.LOCAL to LocalChatModelProvider()
    )

    fun getProviderType(): ChatModelProviderType {
        return when {
            application.isUnitTestMode -> ChatModelProviderType.resolve(System.getProperty("pls.ai.providerType", "OPEN_AI"))
            else -> PlsAiFacade.getSettings().providerType
        }
    }

    fun getProvider(type: ChatModelProviderType = getProviderType()): ChatModelProvider<*> {
        return providers.getValue(type)
    }

    fun getChatModel(type: ChatModelProviderType = getProviderType()): ChatModel? {
        return getProvider(type).getChatModel()
    }

    fun getStreamingChatModel(type: ChatModelProviderType = getProviderType()): StreamingChatModel? {
        return getProvider(type).getStreamingChatModel()
    }
}
