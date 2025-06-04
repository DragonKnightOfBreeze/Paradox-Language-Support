package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.CwtLocalisationLocaleConfig
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.localisation.psi.*
import kotlinx.coroutines.launch
import java.awt.datatransfer.*

/**
 * 复制本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
class CopyLocalisationIntention : CopyLocalisationIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisation")

    override fun doInvoke(project: Project, editor: Editor?, file: PsiFile?, elements: List<ParadoxLocalisationProperty>) {
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val text = getText(project, file, elements) ?: return@launch
            copyText(project, text)
        }
    }

    private fun getText(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>): String? {
        val text = elements.joinToString("\n") { it.text }
        return text
    }

    private fun copyText(project: Project, text: String) {
        createNotification(PlsBundle.message("intention.copyLocalisation.notification.success"), NotificationType.INFORMATION).notify(project)
        CopyPasteManager.getInstance().setContents(StringSelection(text))
    }
}
