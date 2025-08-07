package icu.windea.pls.ai.services

import com.intellij.ide.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import dev.langchain4j.kotlin.model.chat.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.coroutines.*
import icu.windea.pls.lang.util.manipulators.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.awt.*
import kotlin.contracts.*

abstract class PlsAiManipulateLocalisationService : PlsAiService {
    protected val logger = logger<PlsAiManipulateLocalisationService>()

    fun Flow<StreamingChatModelReply>.toResultFlow(): Flow<ParadoxLocalisationResult> {
        return toLineFlow({
            when (it) {
                is StreamingChatModelReply.PartialResponse -> it.partialResponse
                is StreamingChatModelReply.CompleteResponse -> ""
                is StreamingChatModelReply.Error -> throw it.cause
            }
        }, {
            ParadoxLocalisationResult.fromLine(it)
        }).onCompletion { e ->
            when {
                e is CancellationException -> logger.warn("[AI RESPONSE] Cancelled.")
                e != null -> logger.warn("[AI RESPONSE] Failed.", e)
                else -> logger.info("[AI RESPONSE] Done.")
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    fun checkResultFlow(resultFlow: Flow<ParadoxLocalisationResult>?) {
        contract {
            returns() implies (resultFlow != null)
        }
        if (resultFlow == null) { //resultFlow返回null，这意味着AI设置不合法，例如API KEY未填写（但不包括API kEY已填写但正确的情况）
            throw IllegalStateException(PlsBundle.message("ai.manipulation.localisation.error.1"))
        }
    }

    fun checkResult(context: ParadoxLocalisationContext, result: ParadoxLocalisationResult) {
        if (context.key.isEmpty()) { //输出内容的格式不合法
            throw IllegalStateException(PlsBundle.message("ai.manipulation.localisation.error.2"))
        }
        if (context.key != result.key) { //不期望的结果，直接报错，中断收集
            throw IllegalStateException(PlsBundle.message("ai.manipulation.localisation.error.3", context.key, result.key))
        }
    }

    fun createDescriptionPopup(
        project: Project,
        historyPropertyName: String,
        callback: (String) -> Unit
    ): JBPopup {
        val submitted = AtomicBooleanProperty(false)
        val textField = TextFieldWithStoredHistory(historyPropertyName).apply { textEditor.addActionListener { submitted.set(true) } }
        val panel = panel {
            row {
                cell(textField).align(AlignX.FILL).focused().columns(COLUMNS_LARGE).smaller()
            }
            row {
                comment(PlsBundle.message("ai.manipulation.localisation.popup.comment")).align(AlignX.LEFT).smaller()
            }
            row {
                text(PlsBundle.message("ai.manipulation.localisation.popup.tip")).align(AlignX.LEFT).smaller().smallerFont()
                button(PlsBundle.message("ai.manipulation.localisation.popup.button.submit")) { submitted.set(true) }.align(AlignX.RIGHT).smaller()
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
            .setCancelOnOtherWindowOpen(false)
            .setCancelButton(MinimizeButton(IdeBundle.message("tooltip.hide")))
            .setMinSize(Dimension(640, 120))
            .setOkHandler { callback(textField.text.trim()) }
            .createPopup()
        submitted.afterSet { popup.closeOk(null) }
        return popup
    }
}
