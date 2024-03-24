package icu.windea.pls.lang.hierarchy.call

import com.intellij.ide.hierarchy.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.hierarchy.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import java.text.*
import javax.swing.*

class ParadoxCallHierarchyBrowser(project: Project, target: PsiElement) : CallHierarchyBrowserBase(project, target) {
    override fun prependActions(actionGroup: DefaultActionGroup) {
        actionGroup.add(icu.windea.pls.lang.hierarchy.call.ViewCallerHierarchyAction())
        actionGroup.add(icu.windea.pls.lang.hierarchy.call.ViewCalleeHierarchyAction())
        actionGroup.add(AlphaSortAction())
        actionGroup.add(icu.windea.pls.lang.hierarchy.ChangeScopeTypeAction(this, getHierarchySettings()))
    }
    
    override fun createTrees(trees: MutableMap<in String, in JTree>) {
        val tree1 = createTree(false)
        PopupHandler.installPopupMenu(tree1, PlsActions.CallHierarchyPopupMenu, ActionPlaces.CALL_HIERARCHY_VIEW_POPUP)
        val baseOnThisMethodAction = icu.windea.pls.lang.hierarchy.call.ParadoxCallHierarchyBrowser.BaseOnThisMethodAction()
        baseOnThisMethodAction.registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_CALL_HIERARCHY).shortcutSet, tree1)
        trees.put(getCalleeType(), tree1)
        
        val tree2 = createTree(false)
        PopupHandler.installPopupMenu(tree2, PlsActions.CallHierarchyPopupMenu, ActionPlaces.CALL_HIERARCHY_VIEW_POPUP)
        baseOnThisMethodAction.registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_CALL_HIERARCHY).shortcutSet, tree2)
        trees.put(getCallerType(), tree2)
    }
    
    override fun getContentDisplayName(typeName: String, element: PsiElement): String? {
        val name = when {
            element is ParadoxScriptScriptedVariable -> element.name
            element is ParadoxScriptDefinitionElement -> element.definitionInfo?.name.orAnonymous()
            element is ParadoxLocalisationProperty -> element.localisationInfo?.name.orAnonymous()
            else -> return null
        }
        return MessageFormat.format(typeName, name)
    }
    
    override fun getElementFromDescriptor(descriptor: HierarchyNodeDescriptor): PsiElement? {
        return descriptor.psiElement
    }
    
    override fun isApplicableElement(element: PsiElement): Boolean {
        return element is ParadoxScriptScriptedVariable || element is ParadoxScriptDefinitionElement || element is ParadoxLocalisationProperty
    }
    
    override fun createHierarchyTreeStructure(type: String, psiElement: PsiElement): HierarchyTreeStructure? {
        return when(type) {
            getCallerType() -> {
                val definitionInfo = psiElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
                icu.windea.pls.lang.hierarchy.call.ParadoxCallerHierarchyTreeStructure(myProject, psiElement, definitionInfo)
            }
            getCalleeType() -> {
                val definitionInfo = psiElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
                icu.windea.pls.lang.hierarchy.call.ParadoxCalleeHierarchyTreeStructure(myProject, psiElement, definitionInfo)
            }
            else -> null
        }
    }
    
    override fun getComparator(): Comparator<NodeDescriptor<*>>? {
        return icu.windea.pls.lang.hierarchy.ParadoxHierarchyHandler.getComparator(myProject)
    }
    
    private fun getHierarchySettings() = icu.windea.pls.lang.hierarchy.call.ParadoxCallHierarchyBrowserSettings.Companion.getInstance(myProject)
    
    private class BaseOnThisMethodAction : CallHierarchyBrowserBase.BaseOnThisMethodAction()
}
