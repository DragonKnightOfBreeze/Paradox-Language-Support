package icu.windea.pls.localisation.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import java.awt.datatransfer.*

//TODO

/**
 * 复制本地化到剪贴板并在复制之前转化语言区域的意向。（鼠标位置对应的本地化，或者鼠标选取范围涉及到的所有本地化）
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 * 
 * 可以配置是否需要尝试翻译本地化文本。
 */
class CopyLocalisationForLocaleIntention : IntentionAction {
	override fun startInWriteAction() = false
	
	override fun getText() = PlsBundle.message("localisation.intention.copyLocalisationForLocale")
	
	override fun getFamilyName() = text
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		if(file.language != ParadoxLocalisationLanguage) return false
		val selectionStart = editor.selectionModel.selectionStart
		val selectionEnd = editor.selectionModel.selectionEnd
		if(selectionStart == selectionEnd) {
			val originalElement = file.findElementAt(selectionStart)
			return originalElement?.parentOfType<ParadoxLocalisationProperty>() != null
		} else {
			val originalStartElement = file.findElementAt(selectionStart) ?: return false
			val originalEndElement = file.findElementAt(selectionEnd) ?: return false
			return hasLocalisationPropertiesBetween(originalStartElement, originalEndElement)
		}
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		if(file.language != ParadoxLocalisationLanguage) return
		val selectionStart = editor.selectionModel.selectionStart
		val selectionEnd = editor.selectionModel.selectionEnd
		if(selectionStart == selectionEnd) {
			val originalElement = file.findElementAt(selectionStart)
			val element = originalElement?.parentOfType<ParadoxLocalisationProperty>() ?: return
			val text = element.text //使用原始文本
			CopyPasteManager.getInstance().setContents(StringSelection(text))
		} else {
			val originalStartElement = file.findElementAt(selectionStart) ?: return
			val originalEndElement = file.findElementAt(selectionEnd) ?: return
			val elements = findLocalisationPropertiesBetween(originalStartElement, originalEndElement)
			if(elements.isEmpty()) return
			val text = elements.joinToString("\n") { it.text } //使用原始文本，不加缩进
			CopyPasteManager.getInstance().setContents(StringSelection(text))
		}
	}
	
	//在翻译之前，要讲特殊标记用<>包围起来，这样翻译后就可以保留特殊标记（期望如此）
}