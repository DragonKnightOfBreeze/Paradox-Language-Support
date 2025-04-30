package icu.windea.pls.lang.hierarchy.call

import com.intellij.ide.hierarchy.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.lang.hierarchy.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import java.text.*
import javax.swing.*

class ParadoxCallHierarchyBrowser(project: Project, target: PsiElement) : CallHierarchyBrowserBase(project, target) {
    override fun prependActions(actionGroup: DefaultActionGroup) {
        actionGroup.add(ParadoxCallHierarchyActions.ViewCallerHierarchyAction())
        actionGroup.add(ParadoxCallHierarchyActions.ViewCalleeHierarchyAction())
        actionGroup.add(AlphaSortAction())
        actionGroup.add(ParadoxHierarchyActions.ChangeScopeTypeAction(this, getHierarchySettings()))
    }

    override fun createTrees(trees: MutableMap<in String, in JTree>) {
        val tree1 = createTree(false)
        PopupHandler.installPopupMenu(tree1, PlsActions.CallHierarchyPopupMenu, ActionPlaces.CALL_HIERARCHY_VIEW_POPUP)
        val baseOnThisMethodAction = BaseOnThisMethodAction()
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
        val baseDescriptor = ParadoxCallHierarchyNodeDescriptor(project, null, element, true, false)
        val baseDefinitionInfo = psiElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
        return when (type) {
            getCallerType() -> ParadoxCallerHierarchyTreeStructure(myProject, baseDescriptor, baseDefinitionInfo)
            getCalleeType() -> ParadoxCalleeHierarchyTreeStructure(myProject, baseDescriptor, baseDefinitionInfo)
            else -> null
        }
    }

    override fun getComparator(): Comparator<NodeDescriptor<*>>? {
        return ParadoxHierarchyManager.getComparator(myProject)
    }

    private fun getHierarchySettings(): ParadoxCallHierarchyBrowserSettings {
        return ParadoxCallHierarchyBrowserSettings.getInstance(myProject)
    }

    private class BaseOnThisMethodAction : CallHierarchyBrowserBase.BaseOnThisMethodAction()
}
