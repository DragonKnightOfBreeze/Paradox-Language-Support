package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.lang.util.*

/**
 * 提供定义的层级视图。（定义类型/定义子类型 > 定义）
 *
 * * 忽略直接位于游戏或模组入口目录下的文件。
 */
class ParadoxDefinitionHierarchyProvider : HierarchyProvider {
    override fun getTarget(dataContext: DataContext): PsiElement? {
        val project = dataContext.getData(CommonDataKeys.PROJECT) ?: return null
        val editor = dataContext.getData(CommonDataKeys.EDITOR) ?: return null
        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        if(file !is ParadoxScriptFile) return null
        val fileInfo = file.fileInfo ?: return null
        if(fileInfo.pathToEntry.length <= 1) return null //忽略直接位于游戏或模组入口目录下的文件
        val offset = editor.caretModel.offset
        val definition = ParadoxPsiManager.findDefinition(file, offset) ?: return null
        return definition
    }
    
    override fun createHierarchyBrowser(target: PsiElement): HierarchyBrowser {
        return ParadoxDefinitionHierarchyBrowser(target.project, target)
    }
    
    override fun browserActivated(hierarchyBrowser: HierarchyBrowser) {
        hierarchyBrowser as ParadoxDefinitionHierarchyBrowser
        hierarchyBrowser.changeView(ParadoxDefinitionHierarchyBrowser.getDefinitionHierarchyType())
    }
}