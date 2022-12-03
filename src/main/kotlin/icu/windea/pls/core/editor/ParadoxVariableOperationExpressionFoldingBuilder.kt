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
	
	override fun getPlaceholderText(node: ASTNode): String {
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
				doVisitProperty(element, settings)
				super.visitProperty(element)
			}
			
			private fun doVisitProperty(element: ParadoxScriptProperty, settings: Map<String, CwtFoldingSetting>) {
				val configs = ParadoxCwtConfigHandler.resolvePropertyConfigs(element, orDefault = false)
				if(configs.isEmpty()) return  //must match
				val propertyKey = element.name
				val setting = settings.get(propertyKey) ?: return 
				//property key is ignore case, properties must be kept in order (declared by keys)
				val propertyValue = element.propertyValue ?: return
				val elementsToKeep = when {
					setting.key != null && propertyValue !is ParadoxScriptBlock -> {
						propertyValue.toSingletonListOrEmpty()
					}
					setting.keys != null && propertyValue is ParadoxScriptBlock -> {
						var i = -1
						val r = mutableListOf<ParadoxScriptProperty>()
						propertyValue.processProperty {
							i++
							if(it.name.equals(setting.keys.getOrNull(i), true)) {
								r.add(it)
								true
							} else {
								false
							}
						}
						if(setting.keys.size != r.size) return
						r
					}
					else -> {
						emptyList()
					}
				}
				if(elementsToKeep.isEmpty()) return
				
				//references in placeholder must be kept in order (declared by keys)
				val node = element.node
				val descriptorNode = propertyValue.node
				val rootRange = element.textRange
				var startOffset = rootRange.startOffset
				val endOffset = rootRange.endOffset
				var valueRange: TextRange? = null
				val descriptors = SmartList<FoldingDescriptor>()
				val foldingGroup = FoldingGroup.newGroup(foldingGroupName)
				val list = setting.placeholder.split('$')
				val keys = setting.key?.toSingletonList() ?: setting.keys ?: emptyList()
				for((index, s) in list.withIndex()) {
					if(index % 2 == 0) {
						//'{ k = v}' will be folded by ParadoxScriptFoldingBuilder
						//It's necessary to fold divided 'a = {' and ' ' inside 'a = { k = v }', so fold 'a = {' here
						if(index == 0 && propertyValue is ParadoxScriptBlock) {
							val propertyValueRange = propertyValue.textRange
							val textRange = TextRange.create(startOffset, propertyValueRange.startOffset)
							val descriptor = FoldingDescriptor(node, textRange, foldingGroup, emptySet(), false, "", null)
							descriptors.add(descriptor)
							startOffset = propertyValueRange.startOffset
						}
						
						val elementToKeep = elementsToKeep.getOrNull(index / 2)
						valueRange = when(elementToKeep) {
							is ParadoxScriptProperty -> elementToKeep.propertyValue?.textRange
							else -> elementToKeep?.textRange
						}
						if(valueRange == null && index != list.lastIndex) return  //unexpected
						val textStartOffset = startOffset
						val textEndOffset = valueRange?.startOffset ?: endOffset
						val textRange = TextRange.create(textStartOffset, textEndOffset)
						if(textRange.isEmpty) continue
						val descriptor = FoldingDescriptor(descriptorNode, textRange, foldingGroup, emptySet(), false, s, null)
						descriptors.add(descriptor)
					} else {    
						if(s.isEmpty()) return  //invalid
						if(s != keys.getOrNull(index / 2)) return  //invalid
						startOffset = valueRange?.endOffset ?: return  //unexpected
					}
				}
				allDescriptors.addAll(descriptors)
				return
			}
		})
		return allDescriptors.toTypedArray()
	}
}
