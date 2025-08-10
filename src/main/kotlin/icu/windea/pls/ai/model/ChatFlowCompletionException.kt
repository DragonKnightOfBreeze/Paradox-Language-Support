@file:Suppress("unused")

package icu.windea.pls.ai.model

import dev.langchain4j.model.chat.response.*

class ChatFlowCompletionException(
    val response: ChatResponse
) : RuntimeException()
