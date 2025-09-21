package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.HierarchyBrowser
import com.intellij.ide.hierarchy.HierarchyProvider
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 提供定义的类型层级视图。（定义类型/定义子类型 > 定义）
 *
 * * 忽略直接位于游戏或模组入口目录下的文件。
 */
class ParadoxDefinitionHierarchyProvider : HierarchyProvider {
    override fun getTarget(dataContext: DataContext): PsiElement? {
        run {
            val element = dataContext.getData(CommonDataKeys.PSI_ELEMENT)
            if (element is ParadoxScriptDefinitionElement && element.definitionInfo != null) return element
        }
        run {
            val project = dataContext.getData(CommonDataKeys.PROJECT) ?: return@run
            val editor = dataContext.getData(CommonDataKeys.EDITOR) ?: return@run
            val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
            if (file == null || file.language !is ParadoxBaseLanguage) return@run
            val offset = editor.caretModel.offset

            run r@{
                val result = ParadoxPsiFinder.findDefinition(file, offset) { DEFAULT or BY_REFERENCE } ?: return@r
                return result
            }
        }
        return null
    }

    override fun createHierarchyBrowser(target: PsiElement): HierarchyBrowser {
        return ParadoxDefinitionHierarchyBrowser(target.project, target)
    }

    override fun browserActivated(hierarchyBrowser: HierarchyBrowser) {
        hierarchyBrowser as ParadoxDefinitionHierarchyBrowser
        hierarchyBrowser.changeView(ParadoxDefinitionHierarchyType.Type.text)
    }
}
