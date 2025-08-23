package icu.windea.pls.ai.util.manipulators

import com.intellij.ide.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.model.requests.*
import icu.windea.pls.ai.model.results.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.manipulators.*
import kotlinx.coroutines.flow.*
import java.awt.*
import kotlin.contracts.*

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
        if (resultFlow == null) { //resultFlow返回null，这意味着AI设置不合法，例如API KEY未填写（但不包括API kEY已填写但正确的情况）
            throw IllegalStateException(PlsBundle.message("ai.manipulation.localisation.error.1"))
        }
    }

    private fun checkResult(context: ParadoxLocalisationContext, result: LocalisationAiResult) {
        if (result.key.isEmpty()) { //输出内容的格式不合法
            throw IllegalStateException(PlsBundle.message("ai.manipulation.localisation.error.2"))
        }
        if (result.key != context.key) { //不期望的结果，直接报错，中断收集
            throw IllegalStateException(PlsBundle.message("ai.manipulation.localisation.error.3", context.key, result.key))
        }
    }

    fun getOptimizedDescription(description: String?): String? {
        return description?.orNull()?.substringBefore('\n')?.trim() //去除首尾空白，且截断换行符之后的文本
    }

    //ee.carlrobert.codegpt.ui.EditCodePopover
    fun createPopup(
        project: Project,
        callback: (String) -> Unit
    ): JBPopup {
        val submitted = AtomicBooleanProperty(false)
        val textField = JBTextField().apply { addActionListener { submitted.set(true) } } //目前不使用 TextFieldWithStoredHistory
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
