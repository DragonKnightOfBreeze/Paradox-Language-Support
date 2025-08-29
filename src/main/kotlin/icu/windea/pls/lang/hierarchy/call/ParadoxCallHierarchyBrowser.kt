package icu.windea.pls.lang.hierarchy.call

import com.intellij.ide.hierarchy.CallHierarchyBrowserBase
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.ide.hierarchy.LanguageCallHierarchy
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.ui.PopupHandler
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.element
import icu.windea.pls.core.project
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.actions.PlsActions
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.hierarchy.ParadoxHierarchyActions
import icu.windea.pls.lang.hierarchy.ParadoxHierarchyManager
import icu.windea.pls.lang.localisationInfo
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import java.text.MessageFormat
import javax.swing.JTree

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
        val baseOnThisAction = BaseOnThisAction()
        baseOnThisAction.registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_CALL_HIERARCHY).shortcutSet, tree1)
        trees.put(getCalleeType(), tree1)

        val tree2 = createTree(false)
        PopupHandler.installPopupMenu(tree2, PlsActions.CallHierarchyPopupMenu, ActionPlaces.CALL_HIERARCHY_VIEW_POPUP)
        baseOnThisAction.registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_CALL_HIERARCHY).shortcutSet, tree2)
        trees.put(getCallerType(), tree2)
    }

    override fun getContentDisplayName(typeName: String, element: PsiElement): String? {
        val name = when {
            element is ParadoxScriptScriptedVariable -> element.name
            element is ParadoxScriptDefinitionElement -> element.definitionInfo?.name.or.anonymous()
            element is ParadoxLocalisationProperty -> element.localisationInfo?.name.or.anonymous()
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

    private class BaseOnThisAction : BaseOnThisElementAction(LanguageCallHierarchy.INSTANCE)
}
