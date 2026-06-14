package icu.windea.pls.ai.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.ai.AiConstants
import icu.windea.pls.ai.PlsAiBundle
import icu.windea.pls.ai.providers.AnthropicChatModelProvider
import icu.windea.pls.ai.providers.ChatModelProvider
import icu.windea.pls.ai.providers.ChatModelProviderType
import icu.windea.pls.ai.providers.LocalChatModelProvider
import icu.windea.pls.ai.providers.OpenAiChatModelProvider
import icu.windea.pls.ide.help.PlsHelpTopics

class PlsAiSettingsConfigurable : BoundConfigurable(PlsAiBundle.message("settings.ai")), SearchableConfigurable {
    override fun getId() = "pls.ai"

    override fun getHelpTopic() = PlsHelpTopics.aiSettings

    override fun createPanel(): DialogPanel {
        return panel {
            // general
            group(PlsAiBundle.message("settings.ai.general")) { configureGroupForGeneral() }
            // features
            collapsibleGroup(PlsAiBundle.message("settings.ai.features")) { configureGroupForFeatures() }
            // openAI
            collapsibleGroup(PlsAiBundle.message("settings.ai.openAI")) { configureGroupForOpenAi() }
            // anthropic
            collapsibleGroup(PlsAiBundle.message("settings.ai.anthropic")) { configureGroupForAnthropic() }
            // local (Ollama)
            collapsibleGroup(PlsAiBundle.message("settings.ai.local")) { configureGroupForLocal() }
        }
    }

    private fun Panel.configureGroupForGeneral() {
        val settings = PlsAiSettings.getInstance().state

        // enable
        row {
            checkBox(PlsAiBundle.message("settings.ai.general.enable")).bindSelected(settings::enable)
            contextHelp(PlsAiBundle.message("settings.ai.general.enable.tip"))
        }
        // withContext
        row {
            checkBox(PlsAiBundle.message("settings.ai.general.withContext")).bindSelected(settings::withContext)
            contextHelp(PlsAiBundle.message("settings.ai.general.withContext.tip"))
        }
        // providerType
        row {
            label(PlsAiBundle.message("settings.ai.general.providerType"))
            comboBox(ChatModelProviderType.entries, textListCellRenderer { it?.text })
                .bindItem(settings::providerType.toNullableProperty())
        }
    }

    private fun Panel.configureGroupForFeatures() {
        val group = "pls.ai.features"
        val settings = PlsAiSettings.getInstance().state.features

        // localisationBatchSize
        row {
            label(PlsAiBundle.message("settings.ai.features.localisationChunkSize")).widthGroup(group)
            intTextField(1..Int.MAX_VALUE, 1).bindIntText(settings::localisationChunkSize)
            contextHelp(PlsAiBundle.message("settings.ai.features.localisationChunkSize.tip"))
        }
        // localisationMemorySize
        row {
            label(PlsAiBundle.message("settings.ai.features.localisationMemorySize")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::localisationMemorySize)
            contextHelp(PlsAiBundle.message("settings.ai.features.localisationMemorySize.tip"))
        }
    }

    private fun Panel.configureGroupForOpenAi() {
        val group = "pls.ai.openAI"
        val properties = OpenAiChatModelProvider.Options.AtomicProperties()
        val settings = PlsAiSettings.getInstance().state.openAI

        // modelName
        row {
            label(PlsAiBundle.message("settings.ai.modelName")).widthGroup(group)
            textField().columns(COLUMNS_MEDIUM)
                .bindText(properties.modelName)
                .bindText(settings::modelName.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(AiConstants.OpenAi.defaultModelFromLocale) }

            label(PlsAiBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            textField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.modelNameEnv)
                .bindText(settings::modelNameEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(AiConstants.OpenAi.defaultModelEnv) }
        }
        // apiEndpoint
        row {
            label(PlsAiBundle.message("settings.ai.apiEndpoint")).widthGroup(group)
            textField().columns(COLUMNS_MEDIUM)
                .bindText(properties.apiEndpoint)
                .bindText(settings::apiEndpoint.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(AiConstants.OpenAi.defaultBaseUrlFromLocale) }

            label(PlsAiBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            textField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.apiEndpointEnv)
                .bindText(settings::apiEndpointEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(AiConstants.OpenAi.defaultBaseUrlEnv) }
        }
        // apiKey
        row {
            label(PlsAiBundle.message("settings.ai.apiKey")).widthGroup(group)
            passwordField().columns(COLUMNS_MEDIUM)
                .bindText(properties.apiKey)
                .bindText(settings::apiKey.toNonNullableProperty(""))
                .validationOnInput { validateApiKey(this, it, properties.fromEnv.get()) }

            label(PlsAiBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            passwordField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.apiKeyEnv)
                .bindText(settings::apiKeyEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(AiConstants.OpenAi.defaultApiKeyEnv) }
        }
        // operations
        row {
            button(PlsAiBundle.message("settings.ai.test")) {
                // 注意这里需要基于可能尚未保存的配置项进行测试
                val options = OpenAiChatModelProvider.Options.fromProperties(properties.toProperties())
                val r = OpenAiChatModelProvider.INSTANCE.checkStatus(options)
                showTestMessage(r)
            }
            checkBox(PlsAiBundle.message("settings.ai.fromEnv"))
                .bindSelected(properties.fromEnv)
                .bindSelected(settings::fromEnv)
        }
    }

    private fun Panel.configureGroupForAnthropic() {
        val group = "pls.ai.anthropic"
        val properties = AnthropicChatModelProvider.Options.AtomicProperties()
        val settings = PlsAiSettings.getInstance().state.anthropic

        // modelName
        row {
            label(PlsAiBundle.message("settings.ai.modelName")).widthGroup(group)
            textField().columns(COLUMNS_MEDIUM)
                .bindText(properties.modelName)
                .bindText(settings::modelName.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(AiConstants.Anthropic.defaultModelFromLocale) }

            label(PlsAiBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            textField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.modelNameEnv)
                .bindText(settings::modelNameEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(AiConstants.Anthropic.defaultModelEnv) }
        }
        // apiEndpoint
        row {
            label(PlsAiBundle.message("settings.ai.apiEndpoint")).widthGroup(group)
            textField().columns(COLUMNS_MEDIUM)
                .bindText(properties.apiEndpoint)
                .bindText(settings::apiEndpoint.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(AiConstants.Anthropic.defaultBaseUrlFromLocale) }

            label(PlsAiBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            textField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.apiEndpointEnv)
                .bindText(settings::apiEndpointEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(AiConstants.Anthropic.defaultBaseUrlEnv) }
        }
        // apiKey
        row {
            label(PlsAiBundle.message("settings.ai.apiKey")).widthGroup(group)
            passwordField().columns(COLUMNS_MEDIUM)
                .bindText(properties.apiKey)
                .bindText(settings::apiKey.toNonNullableProperty(""))
                .validationOnInput { validateApiKey(this, it, properties.fromEnv.get()) }

            label(PlsAiBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            passwordField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.apiKeyEnv)
                .bindText(settings::apiKeyEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(AiConstants.Anthropic.defaultApiKeyEnv) }
        }
        // operations
        row {
            button(PlsAiBundle.message("settings.ai.test")) {
                // 注意这里需要基于可能尚未保存的配置项进行测试
                val options = AnthropicChatModelProvider.Options.fromProperties(properties.toProperties())
                val r = AnthropicChatModelProvider.INSTANCE.checkStatus(options)
                showTestMessage(r)
            }
            checkBox(PlsAiBundle.message("settings.ai.fromEnv"))
                .bindSelected(properties.fromEnv)
                .bindSelected(settings::fromEnv)
        }
    }

    private fun Panel.configureGroupForLocal() {
        val group = "pls.ai.local"
        val properties = LocalChatModelProvider.Options.AtomicProperties()
        val settings = PlsAiSettings.getInstance().state.local

        // modelName
        row {
            label(PlsAiBundle.message("settings.ai.modelName")).widthGroup(group)
            textField().columns(COLUMNS_MEDIUM)
                .bindText(properties.modelName)
                .bindText(settings::modelName.toNonNullableProperty(""))
                .validationOnInput { validateModelName(this, it, properties.fromEnv.get()) }

            label(PlsAiBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            textField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.modelNameEnv)
                .bindText(settings::modelNameEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(AiConstants.Local.defaultModelEnv) }
        }
        // apiEndpoint
        row {
            label(PlsAiBundle.message("settings.ai.apiEndpoint")).widthGroup(group)
            textField().columns(COLUMNS_MEDIUM)
                .bindText(properties.apiEndpoint)
                .bindText(settings::apiEndpoint.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(AiConstants.Local.defaultBaseUrl) }

            label(PlsAiBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            textField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.apiEndpointEnv)
                .bindText(settings::apiEndpointEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(AiConstants.Local.defaultBaseUrlEnv) }
        }
        // operations
        row {
            button(PlsAiBundle.message("settings.ai.test")) {
                // 注意这里需要基于可能尚未保存的配置项进行测试
                val options = LocalChatModelProvider.Options.fromProperties(properties.toProperties())
                val r = LocalChatModelProvider.INSTANCE.checkStatus(options)
                showTestMessage(r)
            }
            checkBox(PlsAiBundle.message("settings.ai.fromEnv"))
                .bindSelected(properties.fromEnv)
                .bindSelected(settings::fromEnv)
        }
    }

    private fun validateModelName(builder: ValidationInfoBuilder, field: JBTextField, skip: Boolean): ValidationInfo? {
        if (!skip && field.text.isEmpty()) return builder.warning(PlsAiBundle.message("ai.validation.missingModelName"))
        return null
    }

    // private fun validateApiEndpoint(builder: ValidationInfoBuilder, field: JBTextField, skip: Boolean): ValidationInfo? {
    //     if (!skip && field.text.isEmpty()) return builder.warning(PlsAiBundle.message("ai.validation.missingApiEndpoint"))
    //     return null
    // }

    private fun validateApiKey(builder: ValidationInfoBuilder, field: JBPasswordField, skip: Boolean): ValidationInfo? {
        // 目前仅在输入时验证，不在应用时验证
        // 如果启用 AI 集成，但是这里的验证并未通过，相关功能仍然可用，只是使用后会给出警告
        if (!skip && field.password.isEmpty()) return builder.warning(PlsAiBundle.message("ai.validation.missingApiKey"))
        return null
    }

    private fun showTestMessage(statusResult: ChatModelProvider.StatusResult) {
        if (statusResult.status) {
            Messages.showInfoMessage(statusResult.message, statusResult.title)
        } else {
            Messages.showWarningDialog(statusResult.message, statusResult.title)
        }
    }
}
