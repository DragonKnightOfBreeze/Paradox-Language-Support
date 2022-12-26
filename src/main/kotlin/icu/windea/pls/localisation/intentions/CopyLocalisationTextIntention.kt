package icu.windea.pls.localisation.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import java.awt.datatransfer.*

/**
 * 将本地化文本复制到剪贴板的意向。
 */
class CopyLocalisationTextIntention : IntentionAction {
	override fun getText() = PlsBundle.message("localisation.intention.copyLocalisationText")
	
	override fun getFamilyName() = text
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		val offset = editor.caretModel.offset
		val element = findElement(file, offset)
		return element != null
	}
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		val offset = editor.caretModel.offset
		val element = findElement(file, offset) ?: return
		val text = element.value
		CopyPasteManager.getInstance().setContents(StringSelection(text))
	}
	
	private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
		return file.findElementAt(offset) { it.parentOfType() }
	}
	
	override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
	
	override fun startInWriteAction() = false
}

