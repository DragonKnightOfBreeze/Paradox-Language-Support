package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import java.awt.datatransfer.*

/**
 * 复制本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
class CopyLocalisationIntention : ManipulateLocalisationIntentionBase.Default(), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisation")

    override suspend fun doHandle(project: Project, file: PsiFile?, context: Context) {
        val (elements) = context
        val textToCopy = readAction { elements.joinToString("\n") { it.text } }
        CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
        createNotification().notify(project)
    }

    private fun createNotification(): Notification {
        val content = PlsBundle.message("intention.copyLocalisation.notification", Messages.success())
        return createNotification(content, NotificationType.INFORMATION)
    }
}
