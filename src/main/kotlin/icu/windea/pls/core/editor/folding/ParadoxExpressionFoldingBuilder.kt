package icu.windea.pls.core.editor.folding

import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.setting.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

@WithCwtSetting("folding_settings.pls.cwt", CwtFoldingSetting::class)
abstract class ParadoxExpressionFoldingBuilder: FoldingBuilderEx() {
	abstract fun getGroupName(): String
	
	abstract fun getFoldingGroup(): FoldingGroup?
	
	override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
		if(quick) return FoldingDescriptor.EMPTY_ARRAY
		if(!root.language.isKindOf(ParadoxScriptLanguage)) return FoldingDescriptor.EMPTY_ARRAY
		val project = root.project
		val gameType = selectGameType(root) ?: return FoldingDescriptor.EMPTY_ARRAY
		val configGroup = getCwtConfig(project).get(gameType)
		val foldingSettings = configGroup.foldingSettings
		if(foldingSettings.isEmpty()) return FoldingDescriptor.EMPTY_ARRAY
		val settings = foldingSettings.get(getGroupName()) ?: return FoldingDescriptor.EMPTY_ARRAY
		val foldingGroup = getFoldingGroup()
		val allDescriptors = mutableListOf<FoldingDescriptor>()
		root.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
			override fun visitElement(element: PsiElement) {
				if(element is ParadoxScriptProperty) visitProperty(element)
				if(element.isExpressionOrMemberContext()) super.visitElement(element)
			}
            
            private fun visitProperty(element: ParadoxScriptProperty) {
                val configs = ParadoxConfigHandler.getPropertyConfigs(element)
                if(configs.isEmpty()) return  //must match
                val propertyKey = element.name
                val setting = settings.get(propertyKey) ?: return
                //property key is ignore case, properties must be kept in order (declared by keys)
                val propertyValue = element.propertyValue ?: return
                val elementsToKeep: List<PsiElement> = when {
                    setting.key != null && propertyValue !is ParadoxScriptBlock -> {
                        propertyValue.toSingletonListOrEmpty()
                    }
                    setting.keys != null && propertyValue is ParadoxScriptBlock -> {
                        var i = -1
                        val r = mutableListOf<ParadoxScriptProperty>()
                        propertyValue.processProperty(conditional = false) {
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
                val descriptors = mutableListOf<FoldingDescriptor>()
                val list = setting.placeholder.split('$')
                val keys = setting.key?.toSingletonList() ?: setting.keys ?: emptyList()
                for((index, s) in list.withIndex()) {
                    if(index % 2 == 0) {
                        //'{ k = v }' will be folded by ParadoxScriptFoldingBuilder
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
