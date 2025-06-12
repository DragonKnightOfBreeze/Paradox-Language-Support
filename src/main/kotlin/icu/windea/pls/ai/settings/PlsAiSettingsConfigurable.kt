package icu.windea.pls.ai.settings

import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.ai.*
import icu.windea.pls.ai.util.*

class PlsAiSettingsConfigurable : BoundConfigurable(PlsAiBundle.message("settings.ai")), SearchableConfigurable {
    override fun getId() = "pls.ai"

    private val groupNameOpenAI = "pls.ai.openAI"

    override fun createPanel(): DialogPanel {
        val settings = PlsAiManager.getSettings()
        return panel {
            //enable
            row {
                checkBox(PlsAiBundle.message("settings.ai.enable")).bindSelected(settings::enable)
                    .comment(PlsAiBundle.message("settings.ai.enable.comment", MAX_LINE_LENGTH_WORD_WRAP))
            }
            row {
                checkBox(PlsAiBundle.message("settings.ai.withContext")).bindSelected(settings::withContext)
                    .comment(PlsAiBundle.message("settings.ai.withContext.comment", MAX_LINE_LENGTH_WORD_WRAP))
            }

            //openAI
            group(PlsAiBundle.message("settings.ai.openAI")) {
                //modelName
                row {
                    label(PlsAiBundle.message("settings.ai.openAI.modelName")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::modelName.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.getDefaultOpenAiModelName()) }
                }
                //apiEndpoint
                row {
                    label(PlsAiBundle.message("settings.ai.openAI.apiEndpoint")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::apiEndpoint.toNonNullableProperty("")).align(Align.FILL)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.getDefaultOpenAiApiEndpoint()) }
                }
                //apiKey
                row {
                    label(PlsAiBundle.message("settings.ai.openAI.apiKey")).widthGroup(groupNameOpenAI)
                    passwordField().bindText(settings.openAI::apiKey.toNonNullableProperty("")).align(Align.FILL)
                        .validationOnInput { validateOpenAiApiKey(this, it, false) }
                        .validationOnApply { validateOpenAiApiKey(this, it, true) }
                }
            }
        }
    }

    @Suppress("DialogTitleCapitalization")
    private fun validateOpenAiApiKey(builder: ValidationInfoBuilder, field: JBPasswordField, onApply: Boolean): ValidationInfo? {
        if(onApply) {
            if(!PlsAiManager.getSettings().enable) return null
            if(field.password.isEmpty()) {
                return builder.error("settings.ai.openAI.apiKey.2")
            }
        } else {
            if(field.password.isEmpty()) {
                return builder.warning("settings.ai.openAI.apiKey.1")
            }
        }
        return null
    }
}
