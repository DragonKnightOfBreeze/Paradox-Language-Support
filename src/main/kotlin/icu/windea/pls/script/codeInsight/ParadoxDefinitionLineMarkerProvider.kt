package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionLineMarkerProvider : RelatedItemLineMarkerProvider() {
	companion object {
		private val _name = message("paradox.script.gutterIcon.definition")
		private val _title = message("paradox.script.gutterIcon.definition.title")
	}
	
	override fun getName() = _name
	
	override fun getIcon() = definitionGutterIcon
	
	override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
		//如果是definition，则添加definition的gutterIcon
		if(element is ParadoxScriptProperty) {
			val definitionInfo = element.paradoxDefinitionInfo ?: return
			val lineMarkerInfo = createMarker(definitionInfo, element)
			result.add(lineMarkerInfo)
		}
	}
	
	private fun createMarker(definitionInfo: ParadoxDefinitionInfo, element: ParadoxScriptProperty): RelatedItemLineMarkerInfo<PsiElement> {
		val icon = definitionGutterIcon
		val tooltip = buildString {
			val name = definitionInfo.name
			val typeText = definitionInfo.typeText
			append("(definition) <b>").append(name.escapeXmlOrAnonymous()).append("</b>: ").append(typeText)
		}
		val project = element.project
		val targets = findDefinitions(definitionInfo.name, definitionInfo.type, project)
		val targetElement = element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId!! }
		return NavigationGutterIconBuilder.create(icon)
			.setTooltipText(tooltip)
			.setPopupTitle(_title)
			.setTargets(targets)
			.setAlignment(GutterIconRenderer.Alignment.RIGHT)
			.setNamer { _name }
			.createLineMarkerInfo(targetElement)
	}
	
	//override fun getLineMarkerInfo(element: PsiElement): LineMarker? {
	//	return when(element) {
	//		//必须是scriptProperty，且必须是definition
	//		is ParadoxScriptProperty -> {
	//			val definition = element.paradoxDefinitionInfo ?: return null
	//			LineMarker(element, definition)
	//		}
	//		else -> null
	//	}
	//}
	//
	//
	//class LineMarker(
	//	element: ParadoxScriptProperty,
	//	definitionInfo: ParadoxDefinitionInfo
	//) : RelatedItemLineMarkerInfo<PsiElement>(
	//	element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId!! },
	//	element.textRange,
	//	definitionGutterIcon,
	//	{
	//		buildString {
	//			val name = definitionInfo.name
	//			val typeText = definitionInfo.typeText
	//			append("(definition) <b>").append(name.escapeXmlOrAnonymous()).append("</b>: ").append(typeText)
	//		}
	//	},
	//	{ mouseEvent, _ ->
	//		val project = element.project
	//		val elements = findDefinitions(definitionInfo.name,definitionInfo.type,project).toTypedArray()
	//		when(elements.size) {
	//			0 -> {}
	//			1 -> OpenSourceUtil.navigate(true, elements.first())
	//			else -> NavigationUtil.getPsiElementPopup(elements, _title).show(RelativePoint(mouseEvent))
	//		}
	//	},
	//	GutterIconRenderer.Alignment.RIGHT,
	//	{ _name }
	//)
}