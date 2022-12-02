package icu.windea.pls.core.editor

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.components.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.settings.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.script.psi.*

class ParadoxVariableOperationExpressionFoldingBuilder : FoldingBuilderEx() {
	private val foldingGroupName = "variable_operation_expressions"
	
	override fun getPlaceholderText(node: ASTNode): String? {
		return ""
	}
	
	override fun isCollapsedByDefault(node: ASTNode): Boolean {
		return service<ParadoxFoldingSettings>().collapseVariableOperationExpressions
	}
	
	override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
		if(quick) return FoldingDescriptor.EMPTY
		if(root !is ParadoxScriptFile) return FoldingDescriptor.EMPTY
		val project = root.project
		val gameType = ParadoxSelectorUtils.selectGameType(root) ?: return FoldingDescriptor.EMPTY
		val configGroup = getCwtConfig(project).getValue(gameType)
		val foldingSettings = configGroup.foldingSettings
		if(foldingSettings.isEmpty()) return FoldingDescriptor.EMPTY
		val settings = foldingSettings.get(foldingGroupName) ?: return FoldingDescriptor.EMPTY
		val allDescriptors = mutableListOf<FoldingDescriptor>()
		root.acceptChildren(object: ParadoxScriptRecursiveElementWalkingVisitor() {
			override fun visitProperty(element: ParadoxScriptProperty) {
				val result = doVisitProperty(element, settings)
				if(result) super.visitProperty(element)
			}
			
			private fun doVisitProperty(element: ParadoxScriptProperty, settings: Map<String, CwtFoldingSetting>): Boolean {
				val rootBlock = element.block ?: return false
				val configs = ParadoxCwtConfigHandler.resolvePropertyConfigs(element, orDefault = false)
				if(configs.isEmpty()) return true //must match
				val propertyKey = element.name
				val setting = settings.get(propertyKey) ?: return true
				val keys = setting.key?.toSingletonList() ?: setting.keys ?: return true
				val properties = SmartList<ParadoxScriptProperty>()
				//property key is ignore case, properties must be kept in order (declared by keys)
				var i = -1
				val result = rootBlock.processProperty {
					i++
					if(it.name.equals(keys.getOrNull(i), true)) {
						properties.add(it)
						true
					} else {
						false
					}
				}
				if(!result) return true
				if(keys.size != properties.size) return true
				//references in placeholder must be kept in orer (declared by keys)
				val node = element.node
				val rootRange = element.textRange
				var startOffset = rootRange.startOffset
				val endOffset = rootRange.endOffset
				var valueRange: TextRange? = null
				val descriptors = SmartList<FoldingDescriptor>()
				val foldingGroup = FoldingGroup.newGroup(foldingGroupName)
				val list = setting.placeholder.split('$')
				for((index, s) in list.withIndex()) {
					if(index % 2 == 0) {
						valueRange = properties.getOrNull(index / 2)?.propertyValue?.textRange
						if(valueRange == null && index != list.lastIndex) return true //unexpected
						val textRange = TextRange.create(startOffset, valueRange?.startOffset ?: endOffset)
						val descriptor = FoldingDescriptor(node, textRange, foldingGroup, emptySet(), false, s, null)
						descriptors.add(descriptor)
					} else {    
						if(s.isEmpty()) return true //invalid
						if(s != keys.getOrNull(index / 2)) return true //invalid
						startOffset = valueRange?.endOffset ?: return true //unexpected
					}
				}
				allDescriptors.addAll(descriptors)
				return true
			}
		})
		return allDescriptors.toTypedArray()
	}
}
