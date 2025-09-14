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

    fun getChatModel(): ChatModel?

    fun getStreamingChatModel(): StreamingChatModel?

    /**
     * 通过检查选项、发起网络请求、检查本地 AI 服务状态等方式，测试 AI 服务是否可用。
     */
    fun checkStatus(options: S? = this.options): StatusResult

    /**
     * AI 服务提供者的选项。再次封装一层,可由用户配置或者来自环境变量。
     */
    interface Options

    /**
     * AI 服务状态的测试结果。
     */
    data class StatusResult(val status: Boolean, val title: String, val message: String)
}
