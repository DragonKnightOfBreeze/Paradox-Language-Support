package icu.windea.pls.ai.providers

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel

/**
 * AI 服务提供者（目前不作为 EP）。
 *
 * @see ChatModelProviderType
 * @see ChatModelProvider.Options
 */
interface ChatModelProvider<S : ChatModelProvider.Options> {
    val type: ChatModelProviderType

    val options: S?

    /**
     * 快速检查该 AI 服务是否可用。
     * 注意：这里的检查应尽可能轻量（例如仅检验配置是否完整或基础连通性），
     * 真实调用建议在“测试”入口中完成。
     */
    fun isAvailable(): Boolean

    fun getChatModel(): ChatModel?

    fun getStreamingChatModel(): StreamingChatModel?

    /**
     * AI 服务提供者的选项。再次封装一层,可由用户配置或者来自环境变量。
     */
    interface Options
}
