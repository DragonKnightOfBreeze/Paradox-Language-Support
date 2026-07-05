package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.editor
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 导航到当前定义注入的作为目标的所有定义声明。
 */
class GotoDefinitionInjectionTargetsAction : BaseCodeInsightAction() {
    private val handler = GotoDefinitionInjectionTargetsHandler()

    override fun getHandler() = handler

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val project = e.project ?: return
        val editor = e.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return // 忽略直接位于游戏或模组的根目录下的文件
        if (!ParadoxPsiFileMatchService.isScriptFile(file, ParadoxPathConstraint.AcceptDefinitionInjection)) return
        val gameType = selectGameType(file)
        if (!ParadoxDefinitionInjectionManager.isSupported(gameType)) return // 忽略游戏类型不支持的情况
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return // 只要向上能找到符合条件的属性就行
        val info = element.definitionInjectionInfo ?: return
        if (!info.isTargetValid()) return // 排除目标或目标类型为空的情况
        e.presentation.isEnabledAndVisible = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptProperty? {
        return ParadoxPsiFileService.findScriptProperty(file, offset)
    }
}
