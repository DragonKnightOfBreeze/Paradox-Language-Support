package icu.windea.pls.localisation.intentions

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
 * 将本地化文本作为纯文本复制到剪贴板的意向。
 */
class CopyLocalisationPlainTextIntention : IntentionAction {
    override fun getText() = PlsBundle.message("localisation.intention.copyLocalisationPlainText")
    
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
        val text = ParadoxLocalisationTextRenderer.render(element)
        CopyPasteManager.getInstance().setContents(StringSelection(text))
    }
    
    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return ParadoxPsiFinder.findLocalisationProperty(file, offset, false)
    }
    
    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
    
    override fun startInWriteAction() = false
}
