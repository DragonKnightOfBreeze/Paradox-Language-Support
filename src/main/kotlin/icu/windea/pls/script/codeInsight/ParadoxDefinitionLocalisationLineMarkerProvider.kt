package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import com.intellij.ui.awt.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionLocalisationLineMarkerProvider : LineMarkerProviderDescriptor() {
	companion object {
		private val _name = message("paradox.script.gutterIcon.definitionLocalisation")
		private val _title = message("paradox.script.gutterIcon.definitionLocalisation.title")
	}
	
	override fun getName() = _name
	
	override fun getIcon() = definitionLocalisationGutterIcon
	
	override fun getLineMarkerInfo(element: PsiElement): LineMarker? {
		return when(element) {
			//必须是scriptProperty，且对应一个definition
			is ParadoxScriptProperty -> {
				val definition = element.paradoxDefinitionInfo ?: return null
				if(definition.localisation.isEmpty()) return null //没有localisation时不加上gutterIcon
				LineMarker(element, definition)
			}
			else -> null
		}
	}
	
	class LineMarker(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) : LineMarkerInfo<PsiElement>(
		element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId!! },
		element.textRange,
		definitionLocalisationGutterIcon,
		{
			buildString {
				val localisation = definitionInfo.localisation
				var isFirst = true
				for((n, kn) in localisation) {
					if(isFirst) isFirst = false else appendBr()
					append("(definition localisation) ").append(n).append(" = <b>").appendPsiLink("#", kn).append("</b>")
				}
			}
		},
		{ mouseEvent, _ ->
			val keyNames = definitionInfo.localisationKeyNames
			val project = element.project
			val elements = findLocalisations(keyNames, null, project, hasDefault = true, keepOrder = true).toTypedArray()
			when(elements.size) {
				0 -> { }
				1 -> OpenSourceUtil.navigate(true, elements.first())
				else -> NavigationUtil.getPsiElementPopup(elements, _title).show(RelativePoint(mouseEvent))
			}
		},
		GutterIconRenderer.Alignment.RIGHT,
		{ _name }
	)
}