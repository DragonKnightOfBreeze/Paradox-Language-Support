package icu.windea.pls.lang.hierarchy.call

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

//com.intellij.ide.hierarchy.call.JavaCallHierarchyProvider

/**
 * 提供调用层级视图。（封装变量/定义/本地化）
 *
 * * 忽略直接位于游戏或模组入口目录下的文件。
 */
class ParadoxCallHierarchyProvider : HierarchyProvider {
    override fun getTarget(dataContext: DataContext): PsiElement? {
        run {
            val element = dataContext.getData(CommonDataKeys.PSI_ELEMENT) ?: return@run
            when {
                element is ParadoxScriptScriptedVariable -> return element
                element is ParadoxScriptDefinitionElement && element.definitionInfo != null -> return element
                element is ParadoxLocalisationProperty && element.localisationInfo != null -> return element
            }
        }
        run {
            val project = dataContext.getData(CommonDataKeys.PROJECT) ?: return@run
            val editor = dataContext.getData(CommonDataKeys.EDITOR) ?: return@run
            val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
            if (file == null || file.language !is ParadoxBaseLanguage) return@run
            val offset = editor.caretModel.offset

            run r@{
                val findOptions = ParadoxPsiManager.FindScriptedVariableOptions.run { DEFAULT or BY_REFERENCE }
                val result = ParadoxPsiManager.findScriptVariable(file, offset, findOptions) ?: return@r
                return result
            }
            run r@{
                val findOptions = ParadoxPsiManager.FindDefinitionOptions.run { DEFAULT or BY_REFERENCE }
                val result = ParadoxPsiManager.findDefinition(file, offset, findOptions) ?: return@r
                return result
            }
            run r@{
                val findOptions = ParadoxPsiManager.FindLocalisationOptions.run { DEFAULT or BY_REFERENCE }
                val result = ParadoxPsiManager.findLocalisation(file, offset, findOptions) ?: return@r
                return result
            }
        }
        return null
    }

    override fun createHierarchyBrowser(target: PsiElement): HierarchyBrowser {
        return ParadoxCallHierarchyBrowser(target.project, target)
    }

    override fun browserActivated(hierarchyBrowser: HierarchyBrowser) {
        hierarchyBrowser as ParadoxCallHierarchyBrowser
        hierarchyBrowser.changeView(CallHierarchyBrowserBase.getCallerType())
    }
}
