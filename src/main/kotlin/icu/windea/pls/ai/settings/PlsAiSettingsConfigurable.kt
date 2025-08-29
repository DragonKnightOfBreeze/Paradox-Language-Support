package icu.windea.pls.ai.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.core.util.CallbackLock

class PlsAiSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings.ai")), SearchableConfigurable {
    override fun getId() = "pls.ai"

    private val groupNameOpenAI = "pls.ai.openAI"
    private val callbackLock = CallbackLock()

    override fun createPanel(): DialogPanel {
        callbackLock.reset()
        val settings = PlsAiFacade.getSettings()
        return panel {
            //enable
            row {
                checkBox(PlsBundle.message("settings.ai.enable")).bindSelected(settings::enable)
                    .comment(PlsBundle.message("settings.ai.enable.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            }
            //withContext
            row {
                checkBox(PlsBundle.message("settings.ai.withContext")).bindSelected(settings::withContext)
                    .comment(PlsBundle.message("settings.ai.withContext.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            }

            //openAI
            group(PlsBundle.message("settings.ai.openAI")) {
                //modelName
                row {
                    label(PlsBundle.message("settings.ai.openAI.modelName")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::modelName.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.getDefaultOpenAiModelName()) }
                        .onApply { PlsAiSettingsManager.onOpenAiSettingsChanged(callbackLock) }
                }
                //apiEndpoint
                row {
                    label(PlsBundle.message("settings.ai.openAI.apiEndpoint")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::apiEndpoint.toNonNullableProperty("")).align(Align.FILL)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.getDefaultOpenAiApiEndpoint()) }
                        .onApply { PlsAiSettingsManager.onOpenAiSettingsChanged(callbackLock) }
                }
                //apiKey
                row {
                    label(PlsBundle.message("settings.ai.openAI.apiKey")).widthGroup(groupNameOpenAI)
                    passwordField().bindText(settings.openAI::apiKey.toNonNullableProperty("")).align(Align.FILL)
                        .validationOnInput { PlsAiSettingsManager.validateOpenAiApiKey(this, it) }
                        .onApply { PlsAiSettingsManager.onOpenAiSettingsChanged(callbackLock) }
                }
            }

            //features
            collapsibleGroup(PlsBundle.message("settings.ai.features")) {
                //batchSizeOfLocalisations
                row {
                    label(PlsBundle.message("settings.ai.features.localisationBatchSize"))
                    intTextField(1..Int.MAX_VALUE, 1).bindIntText(settings.features::localisationChunkSize)
                    contextHelp(PlsBundle.message("settings.ai.features.localisationBatchSize.tip"))
                }
            }
        }
    }
}
