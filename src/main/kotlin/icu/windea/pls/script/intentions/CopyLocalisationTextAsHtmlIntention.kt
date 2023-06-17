package icu.windea.pls.script.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.tool.localisation.*
import java.awt.datatransfer.*

/**
 * 复制本地化文本到剪贴板。（复制的是HTML文本）
 */
class CopyLocalisationTextAsHtmlIntention : IntentionAction {
    override fun getText() = PlsBundle.message("core.intention.copyLocalisationTextAsHtml")
    
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
        val text = ParadoxLocalisationTextHtmlRenderer.render(element)
        CopyPasteManager.getInstance().setContents(StringSelection(text))
    }
    
    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        val allOptions = ParadoxPsiFinder.FindLocalisationOptions
        val options = allOptions.DEFAULT or allOptions.BY_REFERENCE
        return ParadoxPsiFinder.findLocalisation(file, offset, options)
    }
    
    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
    
    override fun startInWriteAction() = false
}

