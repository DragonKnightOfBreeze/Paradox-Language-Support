package icu.windea.pls.core.hierarchy.call

import com.intellij.ide.hierarchy.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.hierarchy.*
import icu.windea.pls.core.hierarchy.type.*
import icu.windea.pls.script.psi.*
import java.text.*
import javax.swing.*

class ParadoxCallHierarchyBrowser(project: Project, target: PsiElement) : CallHierarchyBrowserBase(project, target) {
    override fun prependActions(actionGroup: DefaultActionGroup) {
        actionGroup.add(ViewCallerHierarchyAction())
        actionGroup.add(ViewCalleeHierarchyAction())
        actionGroup.add(AlphaSortAction())
        actionGroup.add(ChangeScopeTypeAction(this, getHierarchySettings()))
    }
    
    override fun createTrees(trees: MutableMap<in String, in JTree>) {
        val tree1 = createTree(false)
        PopupHandler.installPopupMenu(tree1, IdeActions.GROUP_CALL_HIERARCHY_POPUP, ActionPlaces.CALL_HIERARCHY_VIEW_POPUP)
        val baseOnThisMethodAction = BaseOnThisMethodAction()
        baseOnThisMethodAction.registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_CALL_HIERARCHY).shortcutSet, tree1)
        trees.put(getCalleeType(), tree1)
        
        val tree2 = createTree(false)
        PopupHandler.installPopupMenu(tree2, IdeActions.GROUP_CALL_HIERARCHY_POPUP, ActionPlaces.CALL_HIERARCHY_VIEW_POPUP)
        baseOnThisMethodAction.registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_CALL_HIERARCHY).shortcutSet, tree2)
        trees.put(getCallerType(), tree2)
    }
    
    override fun getContentDisplayName(typeName: String, element: PsiElement): String? {
        val name = when {
            element is ParadoxScriptScriptedVariable -> element.name
            element is ParadoxScriptDefinitionElement -> element.definitionInfo?.name.orAnonymous()
            else -> return null
        }
        return MessageFormat.format(typeName, name)
    }
    
    override fun getElementFromDescriptor(descriptor: HierarchyNodeDescriptor): PsiElement? {
        if(descriptor !is ParadoxDefinitionHierarchyNodeDescriptor) return null
        return descriptor.psiElement
    }
    
    override fun isApplicableElement(element: PsiElement): Boolean {
        return element is ParadoxScriptDefinitionElement || element is ParadoxScriptScriptedVariable
    }
    
    override fun createHierarchyTreeStructure(type: String, psiElement: PsiElement): HierarchyTreeStructure? {
        return when(type) {
            getCallerType() -> {
                ParadoxCallerHierarchyTreeStructure(myProject, psiElement)
            }
            getCalleeType() -> {
                ParadoxCalleeHierarchyTreeStructure(myProject, psiElement)
            }
            else -> null
        }
    }
    
    override fun getComparator(): Comparator<NodeDescriptor<*>> {
        return ParadoxHierarchyHandler.getComparator(myProject)
    }
    
    private fun getHierarchySettings() = ParadoxCallHierarchyBrowserSettings.getInstance(myProject)
    
    private class BaseOnThisMethodAction : CallHierarchyBrowserBase.BaseOnThisMethodAction()
}
