package icu.windea.pls.ai.providers

import icu.windea.pls.ai.ChronicleAiBundle

/**
 * AI 服务提供者的类型。
 *
 * @see ChatModelProvider
 */
enum class ChatModelProviderType(val text: String) {
    /**
     * 使用 [OPEN AI](https://openai.com) 的 API。
     */
    OPEN_AI(ChronicleAiBundle.message("ai.providerType.openAI")),
    /**
     * 使用 [ANTHROPIC](https://www.anthropic.com) 的 API。
     */
    ANTHROPIC(ChronicleAiBundle.message("ai.providerType.anthropic")),
    /**
     * 使用 [Ollama](https://ollama.com)。
     */
    LOCAL(ChronicleAiBundle.message("ai.providerType.local")),
    ;

    companion object {
        fun resolve(type: String): ChatModelProviderType {
            return entries.find { it.name == type.uppercase() } ?: OPEN_AI
        }
    }
}
