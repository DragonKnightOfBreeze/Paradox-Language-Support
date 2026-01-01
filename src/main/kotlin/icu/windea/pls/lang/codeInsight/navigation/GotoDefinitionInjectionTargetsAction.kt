package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 导航到当前定义注入的作为目标的所有定义声明。
 */
class GotoDefinitionInjectionTargetsAction : BaseCodeInsightAction() {
    private val handler = GotoDefinitionInjectionTargetsHandler()

    override fun getHandler(): CodeInsightActionHandler {
        return handler
    }

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (file !is ParadoxScriptFile) return
        val fileInfo = file.fileInfo ?: return
        if (fileInfo.path.length <= 1) return // 忽略直接位于游戏或模组入口目录下的文件
        val gameType = fileInfo.rootInfo.gameType
        if (!ParadoxDefinitionInjectionManager.isSupported(gameType)) return // 忽略游戏类型不支持的情况
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        val info = ParadoxDefinitionInjectionManager.getInfo(element) ?: return
        if (info.target.isNullOrEmpty()) return // 排除目标为空的情况
        if (info.type.isNullOrEmpty()) return // 排除目标定义的类型为空的情况
        presentation.isEnabledAndVisible = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptProperty? {
        return ParadoxPsiFileManager.findScriptProperty(file, offset)
    }
}
