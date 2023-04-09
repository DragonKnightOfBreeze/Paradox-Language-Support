package icu.windea.pls.core.hierarchy

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 提供定义的层级视图。（定义类型/定义子类型 > 定义）
 *
 * * 当当前鼠标位置位于定义声明中启用。
 * * 忽略直接位于游戏或模组入口目录下的文件。
 */
class ParadoxDefinitionHierarchyProvider : HierarchyProvider {
    private fun findElement(psiFile: PsiFile, offset: Int): ParadoxScriptDefinitionElement? {
        return psiFile.findElementAt(offset)
            ?.parents(withSelf = false)
            ?.find { it is ParadoxScriptDefinitionElement && it.definitionInfo != null }
            ?.castOrNull()
    }
    
    override fun getTarget(dataContext: DataContext): PsiElement? {
        val project = dataContext.getData(CommonDataKeys.PROJECT) ?: return null
        val editor = dataContext.getData(CommonDataKeys.EDITOR) ?: return null
        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        if(file !is ParadoxScriptFile) return null
        val fileInfo = file.fileInfo ?: return null
        if(fileInfo.entryPath.length <= 1) return null //忽略直接位于游戏或模组入口目录下的文件
        val offset = editor.caretModel.offset
        val definition = findElement(file, offset) ?: return null
        return definition
    }
    
    override fun createHierarchyBrowser(target: PsiElement): HierarchyBrowser {
        return ParadoxDefinitionHierarchyBrowser(target.project, target)
    }
    
    override fun browserActivated(hierarchyBrowser: HierarchyBrowser) {
        hierarchyBrowser as ParadoxDefinitionHierarchyBrowser
        hierarchyBrowser.changeView(ParadoxDefinitionHierarchyBrowser.getDefinitionHierarchyType1())
    }
}