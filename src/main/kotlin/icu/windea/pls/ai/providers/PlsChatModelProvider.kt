package icu.windea.pls.ai.providers

import com.intellij.openapi.extensions.*
import dev.langchain4j.model.chat.*
import icu.windea.pls.ai.*

interface PlsChatModelProvider {
    val type: PlsChatModelType

    fun isAvailable(): Boolean = true

    fun createChatModel(): ChatModel?

    fun createStreamingChatModel(): StreamingChatModel?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<PlsChatModelProvider>("icu.windea.pls.ai.chatModelProvider")

        fun get(type: PlsChatModelType): PlsChatModelProvider? {
            return EP_NAME.extensionList.find { it.type == type }
        }
    }
}
