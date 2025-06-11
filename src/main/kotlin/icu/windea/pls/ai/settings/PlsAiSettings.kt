package icu.windea.pls.ai.settings

import com.intellij.credentialStore.*
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.core.*

/**
 * PLS AI设置。可以在插件的对应设置页面中进行配置。
 */
@Service(Service.Level.APP)
@State(name = "PlsAiSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class PlsAiSettings : SimplePersistentStateComponent<PlsAiSettingsState>(PlsAiSettingsState())

/**
 * @property enable 是否启用基于AI的各种功能。
 * @property withContext 是否在提示中附加上下文信息。
 */
class PlsAiSettingsState : BaseState() {
    var enable by property(false)
    var withContext by property(false)

    @get:Property(surroundWithTag = false)
    var openAI by property(OpenAiState())

    /**
     * @property modelName 模型名称。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     * @property apiEndpoint API端点。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     * @property apiKey API密钥。密文保存。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     */
    @Tag("openAI")
    class OpenAiState : BaseState() {
        //这里不提供默认值，因为这会纠结于是默认使用OpenAI，还是默认使用DeepSeek……

        var modelName by string()
        var apiEndpoint by string()
        var apiKey by CredentialAttributes("PLS_AI_OPEN_AI_API_KEY")
    }
}
