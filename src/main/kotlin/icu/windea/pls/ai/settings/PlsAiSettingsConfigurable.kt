package icu.windea.pls.ai.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.selected
import icu.windea.pls.PlsBundle
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.providers.AnthropicChatModelProvider
import icu.windea.pls.ai.providers.ChatModelProviderType
import icu.windea.pls.ai.providers.LocalChatModelProvider
import icu.windea.pls.ai.providers.OpenAiChatModelProvider
import java.net.HttpURLConnection
import java.net.URL

class PlsAiSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings.ai")), SearchableConfigurable {
    override fun getId() = "pls.ai"

    override fun getHelpTopic() = "icu.windea.pls.ai.settings"

    private val groupNameOpenAI = "pls.ai.openAI"
    private val groupNameAnthropic = "pls.ai.anthropic"
    private val groupNameLocal = "pls.ai.local"

    override fun createPanel(): DialogPanel {
        val settings = PlsAiFacade.getSettings()
        return panel {
            //enable
            row {
                checkBox(PlsBundle.message("settings.ai.enable")).bindSelected(settings::enable)
                contextHelp(PlsBundle.message("settings.ai.enable.tip"))
            }
            //providerType
            row {
                label(PlsBundle.message("settings.ai.providerType"))
                comboBox(ChatModelProviderType.entries, textListCellRenderer { it?.text })
                    .bindItem(settings::providerType.toNullableProperty())
            }
            //withContext
            row {
                checkBox(PlsBundle.message("settings.ai.withContext")).bindSelected(settings::withContext)
            }

            //features
            group(PlsBundle.message("settings.ai.features")) {
                //localisationBatchSize
                row {
                    label(PlsBundle.message("settings.ai.features.localisationChunkSize"))
                    intTextField(1..Int.MAX_VALUE, 1).bindIntText(settings.features::localisationChunkSize)
                    contextHelp(PlsBundle.message("settings.ai.features.localisationChunkSize.tip"))
                }
                //localisationMemorySize
                row {
                    label(PlsBundle.message("settings.ai.features.localisationMemorySize"))
                    intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings.features::localisationMemorySize)
                    contextHelp(PlsBundle.message("settings.ai.features.localisationMemorySize.tip"))
                }
            }

            //openAI
            collapsibleGroup(PlsBundle.message("settings.ai.openAI")) {
                lateinit var envCheckBox: JBCheckBox

                //modelName
                row {
                    label(PlsBundle.message("settings.ai.openAI.modelName")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::modelName.toNonNullableProperty(""))
                        .columns(COLUMNS_MEDIUM)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultOpenAiModelName) }

                    label(PlsBundle.message("settings.ai.env"))
                        .visibleIf(envCheckBox.selected)
                    textField().bindText(settings.openAI::modelNameEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultOpenAiModelNameEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                //apiEndpoint
                row {
                    label(PlsBundle.message("settings.ai.openAI.apiEndpoint")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::apiEndpoint.toNonNullableProperty(""))
                        .columns(COLUMNS_LARGE)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultOpenAiApiEndpoint) }

                    label(PlsBundle.message("settings.ai.env"))
                        .visibleIf(envCheckBox.selected)
                    textField().bindText(settings.openAI::apiEndpointEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultOpenAiApiEndpointEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                //apiKey
                row {
                    label(PlsBundle.message("settings.ai.openAI.apiKey")).widthGroup(groupNameOpenAI)
                    passwordField().bindText(settings.openAI::apiKey.toNonNullableProperty(""))
                        .columns(COLUMNS_LARGE)
                        .validationOnInput { validateApiKey(this, it) }

                    label(PlsBundle.message("settings.ai.env"))
                        .visibleIf(envCheckBox.selected)
                    passwordField().bindText(settings.openAI::apiKeyEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultOpenAiApiKeyEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                row {
                    checkBox(PlsBundle.message("settings.ai.fromEnv")).bindSelected(settings.openAI::fromEnv)
                        .applyToComponent { envCheckBox = this }

                    button(PlsBundle.message("settings.ai.test")) { testOpenAI() }.align(AlignX.RIGHT)
                }
            }

            //anthropic
            collapsibleGroup(PlsBundle.message("settings.ai.anthropic")) {
                lateinit var envCheckBox: JBCheckBox

                //modelName
                row {
                    label(PlsBundle.message("settings.ai.anthropic.modelName")).widthGroup(groupNameAnthropic)
                    textField().bindText(settings.anthropic::modelName.toNonNullableProperty(""))
                        .columns(COLUMNS_MEDIUM)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultAnthropicModelName) }

                    label(PlsBundle.message("settings.ai.env"))
                        .visibleIf(envCheckBox.selected)
                    textField().bindText(settings.anthropic::modelNameEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultAnthropicModelNameEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                //apiEndpoint
                row {
                    label(PlsBundle.message("settings.ai.anthropic.apiEndpoint")).widthGroup(groupNameAnthropic)
                    textField().bindText(settings.anthropic::apiEndpoint.toNonNullableProperty(""))
                        .columns(COLUMNS_LARGE)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultAnthropicApiEndpoint) }

                    label(PlsBundle.message("settings.ai.env"))
                        .visibleIf(envCheckBox.selected)
                    textField().bindText(settings.anthropic::apiEndpointEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultAnthropicApiEndpointEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                //apiKey
                row {
                    label(PlsBundle.message("settings.ai.anthropic.apiKey")).widthGroup(groupNameAnthropic)
                    passwordField().bindText(settings.anthropic::apiKey.toNonNullableProperty(""))
                        .columns(COLUMNS_LARGE)
                        .validationOnInput { validateApiKey(this, it) }

                    label(PlsBundle.message("settings.ai.env"))
                        .visibleIf(envCheckBox.selected)
                    passwordField().bindText(settings.anthropic::apiKeyEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultAnthropicApiKeyEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                row {
                    checkBox(PlsBundle.message("settings.ai.fromEnv")).bindSelected(settings.anthropic::fromEnv)
                        .applyToComponent { envCheckBox = this }

                    button(PlsBundle.message("settings.ai.test")) { testAnthropic() }.align(AlignX.RIGHT)
                }
            }

            //local (Ollama)
            collapsibleGroup(PlsBundle.message("settings.ai.local")) {
                lateinit var envCheckBox: JBCheckBox

                //modelName
                row {
                    label(PlsBundle.message("settings.ai.local.modelName")).widthGroup(groupNameLocal)
                    textField().bindText(settings.local::modelName.toNonNullableProperty(""))
                        .columns(COLUMNS_MEDIUM)

                    label(PlsBundle.message("settings.ai.env"))
                        .visibleIf(envCheckBox.selected)
                    textField().bindText(settings.local::modelNameEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultLocalModelNameEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                //apiEndpoint
                row {
                    label(PlsBundle.message("settings.ai.local.apiEndpoint")).widthGroup(groupNameLocal)
                    textField().bindText(settings.local::apiEndpoint.toNonNullableProperty(""))
                        .columns(COLUMNS_LARGE)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultLocalApiEndpoint) }

                    label(PlsBundle.message("settings.ai.env"))
                        .visibleIf(envCheckBox.selected)
                    textField().bindText(settings.local::apiEndpointEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultLocalApiEndpointEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                row {
                    checkBox(PlsBundle.message("settings.ai.fromEnv")).bindSelected(settings.local::fromEnv)
                        .applyToComponent { envCheckBox = this }

                    button(PlsBundle.message("settings.ai.test")) { testLocal() }.align(AlignX.RIGHT)
                }
            }
        }
    }

    private fun testOpenAI() {
        val opts = OpenAiChatModelProvider.Options.get()
        if (opts == null) {
            Messages.showWarningDialog(
                PlsBundle.message("ai.test.error.missingConfig"),
                PlsBundle.message("ai.test.error.title")
            )
            return
        }
        val base = ensureSuffix(opts.apiEndpoint, "/v1")
        val url = base + "/models"
        val (ok, msg) = try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
                setRequestProperty("Authorization", "Bearer ${opts.apiKey}")
            }
            val code = conn.responseCode
            if (code in 200..299) true to "HTTP $code"
            else false to "HTTP $code"
        } catch (e: Exception) {
            false to (e.message ?: e::class.java.simpleName)
        }
        if (ok) Messages.showInfoMessage(
            PlsBundle.message("ai.test.success", msg),
            PlsBundle.message("ai.test.success.title")
        ) else Messages.showWarningDialog(
            PlsBundle.message("ai.test.error", msg),
            PlsBundle.message("ai.test.error.title")
        )
    }

    private fun testAnthropic() {
        val opts = AnthropicChatModelProvider.Options.get()
        if (opts == null) {
            Messages.showWarningDialog(
                PlsBundle.message("ai.test.error.missingConfig"),
                PlsBundle.message("ai.test.error.title")
            )
            return
        }
        val base = ensureSuffix(opts.apiEndpoint, "/v1")
        val url = base + "/models"
        val (ok, msg) = try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
                setRequestProperty("x-api-key", opts.apiKey)
                setRequestProperty("anthropic-version", "2023-06-01")
            }
            val code = conn.responseCode
            if (code in 200..299) true to "HTTP $code"
            else false to "HTTP $code"
        } catch (e: Exception) {
            false to (e.message ?: e::class.java.simpleName)
        }
        if (ok) Messages.showInfoMessage(
            PlsBundle.message("ai.test.success", msg),
            PlsBundle.message("ai.test.success.title")
        ) else Messages.showWarningDialog(
            PlsBundle.message("ai.test.error", msg),
            PlsBundle.message("ai.test.error.title")
        )
    }

    private fun testLocal() {
        val opts = LocalChatModelProvider.Options.get()
        if (opts == null) {
            Messages.showWarningDialog(
                PlsBundle.message("ai.test.error.missingConfig"),
                PlsBundle.message("ai.test.error.title")
            )
            return
        }
        val versionUrl = opts.apiEndpoint.trimEnd('/') + "/api/version"
        val tagsUrl = opts.apiEndpoint.trimEnd('/') + "/api/tags"
        val versionOk = try {
            val conn = (URL(versionUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 2000
                readTimeout = 2000
            }
            conn.responseCode in 200..299
        } catch (_: Exception) { false }
        if (!versionOk) {
            Messages.showWarningDialog(
                PlsBundle.message("ai.local.health.unreachable", versionUrl),
                PlsBundle.message("ai.test.error.title")
            )
            return
        }
        val hasModel = try {
            val conn = (URL(tagsUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 2000
                readTimeout = 2000
            }
            if (conn.responseCode !in 200..299) false
            else {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val n1 = "\"name\":\"${opts.modelName}\""
                val n2 = "\"model\":\"${opts.modelName}\""
                text.contains(n1) || text.contains(n2)
            }
        } catch (_: Exception) { false }
        if (hasModel) Messages.showInfoMessage(
            PlsBundle.message("ai.test.success", tagsUrl),
            PlsBundle.message("ai.test.success.title")
        ) else Messages.showWarningDialog(
            PlsBundle.message("ai.local.health.modelMissing", opts.modelName),
            PlsBundle.message("ai.test.error.title")
        )
    }

    private fun ensureSuffix(base: String, suffix: String): String {
        val b = base.trimEnd('/')
        return if (b.endsWith(suffix)) b else b + suffix
    }

    private fun validateApiKey(builder: ValidationInfoBuilder, field: JBPasswordField): ValidationInfo? {
        // 目前仅在输入时验证，不在应用时验证
        // 如果启用 AI 集成，但是这里的验证并未通过，相关功能仍然可用，只是使用后会给出警告
        if (field.password.isEmpty()) return builder.warning(PlsBundle.message("ai.missingApiKey"))
        return null
    }
}
