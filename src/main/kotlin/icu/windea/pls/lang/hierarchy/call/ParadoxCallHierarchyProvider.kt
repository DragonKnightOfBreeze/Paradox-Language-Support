package icu.windea.pls.lang.hierarchy.call

import com.intellij.ide.hierarchy.CallHierarchyBrowserBase
import com.intellij.ide.hierarchy.HierarchyBrowser
import com.intellij.ide.hierarchy.HierarchyProvider
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.localisationInfo
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

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

            ParadoxPsiFinder.findScriptedVariable(file, offset) { DEFAULT or BY_REFERENCE }?.let { return it }
            ParadoxPsiFinder.findDefinition(file, offset) { DEFAULT or BY_REFERENCE }?.let { return it }
            ParadoxPsiFinder.findLocalisation(file, offset) { DEFAULT or BY_REFERENCE }?.let { return it }
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
