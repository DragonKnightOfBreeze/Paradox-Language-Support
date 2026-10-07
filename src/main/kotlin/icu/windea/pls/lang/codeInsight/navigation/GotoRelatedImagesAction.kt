package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 导航到当前目标的相关图片。
 *
 * 支持的目标：
 * - 定义（来自类型键或名字）
 * - 修正（来自引用解析）
 */
class GotoRelatedImagesAction : GotoActionBase() {
    private val handler = GotoRelatedImagesHandler()

    override fun getHandler() = handler

    override fun isVisible(file: PsiFile, editor: Editor): Boolean {
        // 忽略直接位于游戏或模组的根目录下的文件
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun isEnabled(file: PsiFile, editor: Editor): Boolean {
        run {
            if (file !is ParadoxDefinitionElement) return@run
            if (file.definitionInfo != null) return true
        }
        val offset = editor.caretModel.offset
        run {
            if (file !is ParadoxScriptFile) return@run
            val element = ParadoxPsiFileService.findScriptedVariable(file, offset) { BY_NAME } ?: return@run
            if (ParadoxPsiMatchService.isScriptedVariable(element)) return true
        }
        run {
            if (file !is ParadoxScriptFile) return@run
            val element = ParadoxPsiFileService.findScriptExpression(file, offset) ?: return@run
            if (element.isDefinitionTypeKeyOrName()) return true
        }
        run {
            if (file !is ParadoxScriptFile) return@run
            val resolved = file.findReferenceAt(offset)?.resolve() ?: return@run
            if (ParadoxPsiMatchService.isModifierElement(resolved)) return true
        }
        return false
    }
}
