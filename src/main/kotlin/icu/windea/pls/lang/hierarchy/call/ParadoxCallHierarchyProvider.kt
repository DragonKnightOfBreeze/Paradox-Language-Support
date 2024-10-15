package icu.windea.pls.lang.hierarchy.call

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

//com.intellij.ide.hierarchy.call.JavaCallHierarchyProvider

/**
 * 提供调用层级视图。（定义/封装变量）
 *
 * * 忽略直接位于游戏或模组入口目录下的文件。
 */
class ParadoxCallHierarchyProvider : HierarchyProvider {
    override fun getTarget(dataContext: DataContext): PsiElement? {
        val element = dataContext.getData(CommonDataKeys.PSI_ELEMENT) ?: return null
        //定义
        if (element is ParadoxScriptDefinitionElement && element.definitionInfo != null) return element
        //封装变量
        if (element is ParadoxScriptScriptedVariable) return element
        //本地化
        if (element is ParadoxLocalisationProperty && element.localisationInfo != null) return element
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
