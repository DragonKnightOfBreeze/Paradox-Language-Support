package icu.windea.pls.ai.model

import icu.windea.pls.ai.providers.*

enum class ChatModelType(
    val provider: ChatModelProvider<*>
) {
    OPEN_AI(OpenAiChatModelProvider()),
    ;
}
