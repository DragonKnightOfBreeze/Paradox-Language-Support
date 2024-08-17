package icu.windea.pls.lang.intentions.localisation

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.notification.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import java.awt.datatransfer.*

/**
 * 复制本地化到剪贴板。（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
class CopyLocalisationIntention : IntentionAction, PriorityAction {
	override fun getPriority() = PriorityAction.Priority.HIGH
	
	override fun getText() = PlsBundle.message("intention.copyLocalisation")
	
	override fun getFamilyName() = text
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		val selectionStart = editor.selectionModel.selectionStart
		val selectionEnd = editor.selectionModel.selectionEnd
		if(selectionStart == selectionEnd) {
			val originalElement = file.findElementAt(selectionStart)
			return originalElement?.parentOfType<ParadoxLocalisationProperty>() != null
		} else {
			val originalStartElement = file.findElementAt(selectionStart) ?: return false
			val originalEndElement = file.findElementAt(selectionEnd)
			return hasLocalisationPropertiesBetween(originalStartElement, originalEndElement)
		}
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		val selectionStart = editor.selectionModel.selectionStart
		val selectionEnd = editor.selectionModel.selectionEnd
		val keys = mutableSetOf<String>()
		if(selectionStart == selectionEnd) {
			val originalElement = file.findElementAt(selectionStart)
			val element = originalElement?.parentOfType<ParadoxLocalisationProperty>() ?: return
			keys.add(element.name)
			val text = element.text
			CopyPasteManager.getInstance().setContents(StringSelection(text))
		} else {
			val originalStartElement = file.findElementAt(selectionStart) ?: return
			val originalEndElement = file.findElementAt(selectionEnd)
			val elements = findLocalisationPropertiesBetween(originalStartElement, originalEndElement)
			if(elements.isEmpty()) return
			elements.forEach { keys.add(it.name) }
			val text = elements.joinToString("\n") { it.text }
			CopyPasteManager.getInstance().setContents(StringSelection(text))
		}
		
		val keysText = keys.take(PlsConstants.Settings.itemLimit).joinToString { "'<code>$it</code>'" } + if(keys.size > PlsConstants.Settings.itemLimit) ", ..." else ""
		NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
			PlsBundle.message("notification.copyLocalisation.success.title"),
			PlsBundle.message("notification.copyLocalisation.success.content", keysText),
			NotificationType.INFORMATION
		).notify(project)
	}
	
	override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
	
	override fun startInWriteAction() = false
}

