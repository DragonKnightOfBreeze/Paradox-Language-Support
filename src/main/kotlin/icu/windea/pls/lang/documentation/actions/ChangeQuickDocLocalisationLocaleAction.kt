@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.documentation.actions

import cn.yiiguxing.plugin.translate.util.*
import com.intellij.codeInsight.documentation.*
import com.intellij.codeInsight.hint.*
import com.intellij.lang.documentation.ide.actions.*
import com.intellij.lang.documentation.ide.impl.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.wm.*
import com.intellij.platform.ide.documentation.*
import icu.windea.pls.*

//cn.yiiguxing.plugin.translate.action.ToggleQuickDocTranslationAction 

class ChangeQuickDocLocalisationLocaleAction : AnAction(PlsBundle.message("action.ChangeQuickDocLocalisationLocaleAction.text")), HintManagerImpl.ActionToIgnore {
    init {
        // Enable in hovering documentation popup
        isEnabledInModalContext = true
    }
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    override fun update(e: AnActionEvent) {
        //val project = e.project
        //val activeDocComponent = QuickDocUtil.getActiveDocComponent(project)
        //val editorMouseHoverPopupManager = EditorMouseHoverPopupManager.getInstance()
        //val rdMouseHoverDocComponent = editorMouseHoverPopupManager.documentationComponent
        //    .takeIf { IdeVersion.buildNumber.productCode == "RD" }
        //val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.DOCUMENTATION)
        //
        //e.presentation.isVisible = e.presentation.isVisible && rdMouseHoverDocComponent == null
        //    && activeDocComponent?.element.let { it != null && DocTranslationService.isSupportedForPsiElement(it) }
        //
        //// 当Action在ToolWindow的右键菜单上时，点击菜单项会使得ToolWindow失去焦点，
        //// 此时toolWindow.isActive为false，Action将不启用。
        //// 所以Action在右键菜单上时，直接设为启用状态。
        //val isDocMenuPlace = e.place == ActionPlaces.JAVADOC_TOOLBAR || e.place == "documentation.pane.content.menu"
        //e.presentation.isEnabled =
        //    activeDocComponent != null && (isDocMenuPlace || toolWindow == null || toolWindow.isActive)
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        
    }
}