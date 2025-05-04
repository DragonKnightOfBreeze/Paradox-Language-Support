package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.lang.hierarchy.*
import icu.windea.pls.script.psi.*
import java.text.*
import java.util.function.*
import javax.swing.*
import icu.windea.pls.lang.hierarchy.type.ParadoxDefinitionHierarchyType as Type

class ParadoxDefinitionHierarchyBrowser(project: Project, element: PsiElement) : HierarchyBrowserBaseEx(project, element) {
    var type: Type = Type.Type
    val element: PsiElement? get() = hierarchyBase

    override fun createTrees(trees: MutableMap<in String, in JTree>) {
        Type.entries.forEach { type ->
            val tree = createTree(true)
            PopupHandler.installPopupMenu(tree, PlsActions.DefinitionHierarchyPopupMenu, ActionPlaces.TYPE_HIERARCHY_VIEW_POPUP)
            val baseOnThisAction = BaseOnThisAction()
            baseOnThisAction.registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_TYPE_HIERARCHY).shortcutSet, tree)
            trees.put(type.text, tree)
        }
    }

    override fun createHierarchyTreeStructure(type: String, psiElement: PsiElement): HierarchyTreeStructure? {
        val definitionInfo = psiElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo ?: return null
        val typeConfig = definitionInfo.typeConfig
        val typeElement = typeConfig.pointer.element ?: return null
        val finalType = this.type
        if (!finalType.predicate(definitionInfo)) return null
        val finalNodeType = ParadoxDefinitionHierarchyNodeType.Type
        val baseDescriptor = ParadoxDefinitionHierarchyNodeDescriptor(project, null, typeElement, false, typeConfig.name, finalType, finalNodeType)
        return ParadoxDefinitionHierarchyTreeStructure(myProject, baseDescriptor, psiElement, typeConfig, finalType)
    }

    override fun isApplicableElement(element: PsiElement): Boolean {
        return element is CwtProperty || element is ParadoxScriptDefinitionElement
    }

    override fun getContentDisplayName(typeName: String, element: PsiElement): String? {
        val definitionInfo = element.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo ?: return null
        val type = definitionInfo.type
        return MessageFormat.format(typeName, type)
    }

    override fun getElementFromDescriptor(descriptor: HierarchyNodeDescriptor): PsiElement? {
        return descriptor.psiElement
    }

    override fun getComparator(): Comparator<NodeDescriptor<*>>? {
        return ParadoxHierarchyManager.getComparator(myProject)
    }

    override fun getPresentableNameMap(): MutableMap<String, Supplier<String>> {
        val map = mutableMapOf<String, Supplier<String>>()
        Type.entries.forEach { type ->
            map.put(type.text) { type.text }
        }
        return map
    }

    override fun createLegendPanel(): JPanel? {
        return null
    }

    override fun getActionPlace(): String {
        return ActionPlaces.TYPE_HIERARCHY_VIEW_TOOLBAR
    }

    override fun prependActions(actionGroup: DefaultActionGroup) {
        actionGroup.add(ParadoxDefinitionHierarchyActions.ViewDefinitionHierarchyAction())
        actionGroup.add(ParadoxDefinitionHierarchyActions.ViewDefinitionHierarchyWithSubtypesAction())
        actionGroup.add(ParadoxDefinitionHierarchyActions.ViewEventTreeInvokerAction())
        actionGroup.add(ParadoxDefinitionHierarchyActions.ViewEventTreeInvokedAction())
        actionGroup.add(ParadoxDefinitionHierarchyActions.ViewTechTreePreAction())
        actionGroup.add(ParadoxDefinitionHierarchyActions.ViewTechTreePostAction())
        actionGroup.add(AlphaSortAction())
        actionGroup.add(ParadoxHierarchyActions.ChangeScopeTypeAction(this, getHierarchySettings()))
        actionGroup.add(ParadoxHierarchyActions.ChangeGroupingStrategyAction(this))
    }

    override fun getPreviousOccurenceActionName(): String {
        return prevOccurenceActionNameImpl
    }

    override fun getPrevOccurenceActionNameImpl(): String {
        return when (type) {
            Type.EventTreeInvoker, Type.EventTreeInvoked -> PlsBundle.message("hierarchy.eventTree.prev.occurrence.name")
            Type.TechTreePre, Type.TechTreePost -> PlsBundle.message("hierarchy.techTree.prev.occurrence.name")
            else -> PlsBundle.message("hierarchy.definition.prev.occurrence.name")
        }
    }

    override fun getNextOccurenceActionName(): String {
        return nextOccurenceActionNameImpl
    }

    override fun getNextOccurenceActionNameImpl(): String {
        return when (type) {
            Type.EventTreeInvoker, Type.EventTreeInvoked -> PlsBundle.message("hierarchy.eventTree.next.occurrence.name")
            Type.TechTreePre, Type.TechTreePost -> PlsBundle.message("hierarchy.techTree.next.occurrence.name")
            else -> PlsBundle.message("hierarchy.definition.next.occurrence.name")
        }
    }

    private fun getHierarchySettings(): ParadoxDefinitionHierarchyBrowserSettings {
        return ParadoxDefinitionHierarchyBrowserSettings.getInstance(myProject)
    }

    private class BaseOnThisAction : BaseOnThisElementAction(LanguageTypeHierarchy.INSTANCE)
}

