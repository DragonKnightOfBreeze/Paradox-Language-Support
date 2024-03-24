package icu.windea.pls.script.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.localisation.*
import icu.windea.pls.localisation.psi.*
import java.awt.datatransfer.*

/**
 * 复制本地化文本到剪贴板。（复制的是处理后的纯文本）
 */
class CopyLocalisationTextAsPlainIntention : IntentionAction {
    override fun getText() = PlsBundle.message("core.intention.copyLocalisationTextAsPlain")
    
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
        val allOptions = ParadoxPsiManager.FindLocalisationOptions
        val options = allOptions.DEFAULT or allOptions.BY_REFERENCE
        return ParadoxPsiManager.findLocalisation(file, offset, options)
    }
    
    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
    
    override fun startInWriteAction() = false
}
