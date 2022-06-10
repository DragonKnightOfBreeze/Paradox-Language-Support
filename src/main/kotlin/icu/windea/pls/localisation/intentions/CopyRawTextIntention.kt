package icu.windea.pls.localisation.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import java.awt.datatransfer.*

/**
 * 复制本地化文本作为原始文本到剪贴板的意向。
 */
class CopyRawTextIntention : IntentionAction {
	override fun startInWriteAction() = false
	
	override fun getText() = PlsBundle.message("localisation.intention.copyRawText")
	
	override fun getFamilyName() = text
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return false
		val element = originalElement.parentOfType<ParadoxLocalisationProperty>()
		return element != null
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return
		val element = originalElement.parentOfType<ParadoxLocalisationProperty>() ?: return
		val text = element.value
		CopyPasteManager.getInstance().setContents(StringSelection(text))
	}
}

