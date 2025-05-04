package icu.windea.pls.lang.intentions.common

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*
import java.awt.datatransfer.*

/**
 * 复制定义的本地化名字到剪贴板。
 */
abstract class CopyDefinitionLocalizedNameIntentionBase : IntentionAction {
    override fun getText() = PlsBundle.message("intention.copyDefinitionLocalizedName")

    override fun getFamilyName() = text

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false
        val offset = editor.caretModel.offset
        return getLocalizedName(file, offset) != null
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return
        val offset = editor.caretModel.offset
        val text = getLocalizedName(file, offset) ?: return
        CopyPasteManager.getInstance().setContents(StringSelection(text))
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptDefinitionElement? {
        val allOptions = ParadoxPsiManager.FindDefinitionOptions
        val options = allOptions.DEFAULT or allOptions.BY_REFERENCE
        return ParadoxPsiManager.findDefinition(file, offset, options)
    }

    private fun getLocalizedName(file: PsiFile, offset: Int): String? {
        val element = findElement(file, offset) ?: return null
        return ParadoxDefinitionManager.getLocalizedNames(element).firstOrNull()
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun startInWriteAction() = false
}

