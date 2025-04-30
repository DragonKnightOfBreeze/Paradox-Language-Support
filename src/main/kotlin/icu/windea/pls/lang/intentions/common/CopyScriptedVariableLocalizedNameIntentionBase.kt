package icu.windea.pls.lang.intentions.common

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*
import java.awt.datatransfer.*

/**
 * 复制封装变量的本地化名字到剪贴板。
 */
abstract class CopyScriptedVariableLocalizedNameIntentionBase : IntentionAction {
    override fun getText() = PlsBundle.message("intention.copyScriptedVariableLocalizedName")

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

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptScriptedVariable? {
        val allOptions = ParadoxPsiManager.FindScriptedVariableOptions
        val options = allOptions.DEFAULT or allOptions.BY_REFERENCE
        return ParadoxPsiManager.findScriptVariable(file, offset, options)
    }

    private fun getLocalizedName(file: PsiFile, offset: Int): String? {
        val element = findElement(file, offset) ?: return null
        val name = element.name?.orNull() ?: return null
        return ParadoxScriptedVariableManager.getHintFromExtendedConfig(name, file)?.orNull()
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun startInWriteAction() = false
}
