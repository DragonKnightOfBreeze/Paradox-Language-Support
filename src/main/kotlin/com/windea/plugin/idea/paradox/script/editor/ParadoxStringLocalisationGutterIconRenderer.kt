package com.windea.plugin.idea.paradox.script.editor

import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.awt.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import java.awt.event.*

class ParadoxStringLocalisationGutterIconRenderer(
	private val name: String,
	private val properties: Array<ParadoxLocalisationProperty>
): GutterIconRenderer(), DumbAware {
	companion object{
		private val _title = message("paradox.localisation.gutterIcon.localisation.title")
		private fun _tooltip(name:String) = message("paradox.localisation.gutterIcon.localisation.tooltip", name)
	}
	
	private val tooltip = _tooltip(name.escapeXml())
	
	override fun getIcon() = stringLocalisationPropertyGutterIcon
	override fun getTooltipText() = tooltip
	override fun getClickAction() = NavigateAction(properties)
	override fun isNavigateAction() = true
	override fun equals(other: Any?) = other is ParadoxStringLocalisationGutterIconRenderer && name == other.name
	override fun hashCode() = name.hashCode()
	
	@Suppress("ComponentNotRegistered")
	class NavigateAction(
		private val elements: Array<out NavigatablePsiElement>,
	) : AnAction() {
		override fun actionPerformed(e: AnActionEvent) {
		//如果只有一个，则直接导航，否则弹出popup再导航
			return when(elements.size) {
				0 -> return
				1 -> OpenSourceUtil.navigate(true, elements.first())
				else -> NavigationUtil.getPsiElementPopup(elements, _title).show(RelativePoint(e.inputEvent as MouseEvent))
			}
		}
	}
}