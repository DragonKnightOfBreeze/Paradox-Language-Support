package icu.windea.pls.ai

import dev.langchain4j.service.*

class PlsAiRequestException(
    val result: Result<*>
) : IllegalStateException()
