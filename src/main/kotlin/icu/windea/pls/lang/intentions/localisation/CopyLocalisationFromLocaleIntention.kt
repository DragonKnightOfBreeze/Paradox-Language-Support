package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.platform.ide.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.ui.locale.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import kotlinx.coroutines.*
import java.awt.datatransfer.*

/**
 * 复制来自特定语言区域的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
class CopyLocalisationFromLocaleIntention : CopyLocalisationIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisationFromLocale")

    override fun doInvoke(project: Project, editor: Editor?, file: PsiFile?, elements: List<ParadoxLocalisationProperty>) {
        if (editor == null) return
        val allLocales = ParadoxLocaleManager.getLocaleConfigs()
        val localePopup = ParadoxLocaleListPopup(allLocales)
        localePopup.doFinalStep {
            val selectedLocale = localePopup.selectedLocale ?: return@doFinalStep
            val coroutineScope = PlsFacade.getCoroutineScope(project)
            coroutineScope.launch {
                val text = getText(project, file, elements, selectedLocale) ?: return@launch
                copyText(project, text, selectedLocale)
            }
        }
        JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(editor)
    }

    private suspend fun getText(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>, locale: CwtLocalisationLocaleConfig): String? {
        return withBackgroundProgress(project, "Copy localisation(s) to the clipboard from specified locale (target locale: ${locale})") {
            elements.map { it to ParadoxLocalisationSnippets.from(it) }
                .filter { (_, snippets) -> snippets.text.isNotBlank() }
                .map { (_, snippets) ->
                    async {
                        val newText = getNewText(project, file, snippets, locale) ?: return@async
                        snippets.newText = newText
                    }
                }.awaitAll()
            elements.map { it to ParadoxLocalisationSnippets.from(it) }.joinToString("\n") { it.second.renderNew() }
        }
    }

    private fun getNewText(project: Project, file: PsiFile?, snippets: ParadoxLocalisationSnippets, locale: CwtLocalisationLocaleConfig): String? {
        val selector = selector(project, file).localisation().contextSensitive().locale(locale)
        val e = ParadoxLocalisationSearch.search(snippets.key, selector).find() ?: return null
        val newText = e.value ?: return null
        return newText
    }

    private fun copyText(project: Project, text: String, locale: CwtLocalisationLocaleConfig) {
        createNotification(PlsBundle.message("intention.copyLocalisationFromLocale.notification.success", locale), NotificationType.INFORMATION).notify(project)
        CopyPasteManager.getInstance().setContents(StringSelection(text))
    }
}
