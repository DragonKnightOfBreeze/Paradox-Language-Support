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
import icu.windea.pls.core.tool.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import java.awt.datatransfer.*

/**
 * 复制本地化文本作为富文本（即本地化的文档中使用到的HTML文本）到剪贴板的意向。
 */
class CopyLocalisationRichTextIntention : IntentionAction {
	override fun getText() = PlsBundle.message("localisation.intention.copyLocalisationRichText")
	
	override fun getFamilyName() = text
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		if(file.language != ParadoxLocalisationLanguage) return false
		val offset = editor.caretModel.offset
		val element = findElement(file, offset)
		return element != null
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		if(file.language != ParadoxLocalisationLanguage) return
		val offset = editor.caretModel.offset
		val element = findElement(file, offset) ?: return
		val text = ParadoxLocalisationTextRenderer.render(element)
		CopyPasteManager.getInstance().setContents(StringSelection(text))
	}
	
	private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
		return file.findElementAtCaret(offset) { it.parentOfType() }
	}
	
	override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
		return IntentionPreviewInfo.EMPTY
	}
	
	override fun startInWriteAction() = false
}

