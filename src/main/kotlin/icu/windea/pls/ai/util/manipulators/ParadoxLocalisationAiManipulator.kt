package icu.windea.pls.ai.util.manipulators

import com.intellij.ide.IdeBundle
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.util.MinimizeButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.model.requests.ManipulateLocalisationAiRequest
import icu.windea.pls.ai.model.requests.PolishLocalisationAiRequest
import icu.windea.pls.ai.model.requests.TranslateLocalisationAiRequest
import icu.windea.pls.ai.model.results.LocalisationAiResult
import icu.windea.pls.ai.providers.ChatModelProviderType
import icu.windea.pls.core.smaller
import icu.windea.pls.core.smallerFont
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import kotlinx.coroutines.flow.Flow
import java.awt.Dimension
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object ParadoxLocalisationAiManipulator {
    suspend fun handleTextWithAiTranslation(request: TranslateLocalisationAiRequest, callback: suspend (LocalisationAiResult) -> Unit) {
        val aiService = PlsAiFacade.getTranslateLocalisationService()
        val resultFlow = aiService.manipulate(request)
        collectResultFlow(request, resultFlow, callback)
    }

    suspend fun handleTextWithAiPolishing(request: PolishLocalisationAiRequest, callback: suspend (LocalisationAiResult) -> Unit) {
        val aiService = PlsAiFacade.getPolishLocalisationService()
        val resultFlow = aiService.manipulate(request)
        collectResultFlow(request, resultFlow, callback)
    }

    suspend fun collectResultFlow(request: ManipulateLocalisationAiRequest, resultFlow: Flow<LocalisationAiResult>?, callback: suspend (LocalisationAiResult) -> Unit = {}) {
        checkResultFlow(resultFlow)
        resultFlow.collect { data ->
            val context = request.localisationContexts[request.index]
            checkResult(context, data)
            context.newText = data.text
            callback(data)
            request.index++
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun checkResultFlow(resultFlow: Flow<LocalisationAiResult>?) {
        contract {
            returns() implies (resultFlow != null)
        }
        if (resultFlow == null) { // 这意味着 AI 设置不正确，例如 API KEY 未填写（但不包括已填写但不正确的情况）
            throw IllegalStateException(PlsBundle.message("ai.manipulation.localisation.error.1"))
        }
    }

    private fun checkResult(context: ParadoxLocalisationContext, result: LocalisationAiResult) {
        if (result.key.isEmpty()) { // 输出内容的格式不正确
            throw IllegalStateException(PlsBundle.message("ai.manipulation.localisation.error.2"))
        }
        if (result.key != context.key) { // 输出的本地化的键不匹配
            throw IllegalStateException(PlsBundle.message("ai.manipulation.localisation.error.3", context.key, result.key))
        }
    }

    //ee.carlrobert.codegpt.ui.EditCodePopover
    fun createPopup(
        project: Project,
        callback: (String) -> Unit
    ): JBPopup {
        val submitted = AtomicBooleanProperty(false)
        val textField = JBTextField().apply { addActionListener { submitted.set(true) } } //目前不使用 TextFieldWithStoredHistory

        // 需要在 UI 发生变化时就更新设置
        val settings = PlsAiFacade.getSettings()
        val providerType = AtomicProperty(settings.providerType).apply { afterChange { settings.providerType = it } }

        val panel = panel {
            row {
                cell(textField).align(AlignX.FILL).focused().smaller()
            }
            row {
                comment(PlsBundle.message("ai.manipulation.localisation.popup.comment")).align(AlignX.LEFT).smaller()
                button(PlsBundle.message("ai.manipulation.localisation.popup.button.submit")) { submitted.set(true) }.align(AlignX.RIGHT).smaller()
            }
            separator()
            row {
                text(PlsBundle.message("ai.manipulation.localisation.popup.tip")).align(AlignX.LEFT).smaller().smallerFont()

                label(PlsBundle.message("ai.popup.provider"))
                comboBox(ChatModelProviderType.entries, textListCellRenderer { it?.text }).align(AlignX.RIGHT).bindItem(providerType)
            }
        }
        val popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, textField)
            .setTitle(PlsBundle.message("ai.manipulation.localisation.popup.title"))
            .setProject(project)
            .setResizable(true)
            .setMovable(true)
            .setRequestFocus(true)
            .setCancelOnClickOutside(false)
            .setCancelOnWindowDeactivation(false)
            .setCancelButton(MinimizeButton(IdeBundle.message("tooltip.hide")))
            .setMinSize(Dimension(640, 120))
            .setDimensionServiceKey(project, "PLS_AI_LOCALISATION_MANIPULATION_POPUP", false)
            .setOkHandler { callback(textField.text.trim()) }
            .createPopup()
        submitted.afterSet { popup.closeOk(null) }
        return popup
    }
}
