package icu.windea.pls.ai.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.annotations.Property
import com.intellij.util.xmlb.annotations.Tag
import icu.windea.pls.ai.AiConstants
import icu.windea.pls.ai.providers.ChatModelProviderType
import icu.windea.pls.core.getValue
import icu.windea.pls.core.setValue
import icu.windea.pls.model.constants.PlsConstants

/**
 * 插件的 AI 设置。
 */
@Service
@State(name = "PlsAiSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class PlsAiSettings : SimplePersistentStateComponent<PlsAiSettings.State>(State()) {
    fun isEnabled() = state.enable

    companion object {
        @JvmStatic
        fun getInstance(): PlsAiSettings = service()
    }

    /**
     * @property enable 是否启用基于 AI 的各种功能。
     * @property withContext 是否向提示中注入上下文信息。
     * @property providerType 当前使用的 AI 服务提供者的类型。
     */
    class State : BaseState() {
        var enable by property(false)
        var withContext by property(false)
        var providerType by enum<ChatModelProviderType>(ChatModelProviderType.OPEN_AI)

        @get:Property(surroundWithTag = false)
        var features by property(FeaturesState())

        @get:Property(surroundWithTag = false)
        var openAI by property(OpenAiState())

        @get:Property(surroundWithTag = false)
        var anthropic by property(AnthropicState())

        @get:Property(surroundWithTag = false)
        var local by property(LocalState())
    }

    /**
     * @property localisationChunkSize 本地化条目的分块大小。即每次输入的本地化条目的最大数量。
     * @property localisationMemorySize 本地化条目的记忆大小。即会话记忆中本地化条目的最大数值。
     */
    @Tag("features")
    class FeaturesState : BaseState() {
        var localisationChunkSize by property(AiConstants.Settings.defaultLocalisationChunkSize)
        var localisationMemorySize by property(AiConstants.Settings.defaultLocalisationMemorySize)
    }

    /**
     * @property modelName 模型名称。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     * @property apiEndpoint API 端点。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     * @property apiKey API 密钥。密文保存。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     */
    @Tag("openAI")
    class OpenAiState : BaseState() {
        var modelName by string()
        var apiEndpoint by string()
        var apiKey by CredentialAttributes("PLS_AI_OPEN_AI_API_KEY")
        var fromEnv by property(false)
        var modelNameEnv by string()
        var apiEndpointEnv by string()
        var apiKeyEnv by string()
    }

    /**
     * @property modelName 模型名称。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     * @property apiEndpoint API 端点。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     * @property apiKey API 密钥。密文保存。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     */
    @Tag("anthropic")
    class AnthropicState : BaseState() {
        var modelName by string()
        var apiEndpoint by string()
        var apiKey by CredentialAttributes("PLS_AI_ANTHROPIC_API_KEY")
        var fromEnv by property(false)
        var modelNameEnv by string()
        var apiEndpointEnv by string()
        var apiKeyEnv by string()
    }

    /**
     * @property modelName 模型名称。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     * @property apiEndpoint API 端点。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     */
    @Tag("local")
    class LocalState : BaseState() {
        var modelName by string()
        var apiEndpoint by string()
        var fromEnv by property(false)
        var modelNameEnv by string()
        var apiEndpointEnv by string()
    }
}
