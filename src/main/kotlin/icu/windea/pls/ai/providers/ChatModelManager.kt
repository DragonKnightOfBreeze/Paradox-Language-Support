package icu.windea.pls.ai.providers

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.ai.settings.ChronicleAiSettings

@Suppress("unused")
object ChatModelManager {
    fun getProviderType(): ChatModelProviderType {
        return when {
            ChronicleFacade.isUnitTestMode() -> ChatModelProviderType.resolve(System.getProperty("chronicle.ai.providerType", "OPEN_AI"))
            else -> ChronicleAiSettings.getInstance().state.providerType
        }
    }

    fun getProvider(type: ChatModelProviderType = getProviderType()): ChatModelProvider<*> {
        return when (type) {
            ChatModelProviderType.OPEN_AI -> OpenAiChatModelProvider.INSTANCE
            ChatModelProviderType.ANTHROPIC -> AnthropicChatModelProvider.INSTANCE
            ChatModelProviderType.LOCAL -> LocalChatModelProvider.INSTANCE
        }
    }

    fun getChatModel(type: ChatModelProviderType = getProviderType()): ChatModel? {
        return getProvider(type).getChatModel()
    }

    fun getStreamingChatModel(type: ChatModelProviderType = getProviderType()): StreamingChatModel? {
        return getProvider(type).getStreamingChatModel()
    }
}
