package icu.windea.pls.script

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.script.psi.*

//region tag
internal fun ParadoxScriptProperty.resolveTagConfigs(): Map<String, CwtTagConfig>? {
	val definitionInfo = definitionInfo ?: return null
	return definitionInfo.configGroup.tagMap[definitionInfo.type]
}

internal fun ParadoxScriptBlock.resolveTagConfigs(): Map<String, CwtTagConfig>? {
	val propertyValueElement = parent.castOrNull<ParadoxScriptPropertyValue>() ?: return null
	val propertyElement = propertyValueElement.parent.castOrNull<ParadoxScriptProperty>() ?: return null
	val definitionInfo = propertyElement.definitionInfo ?: return null
	return definitionInfo.configGroup.tagMap[definitionInfo.type]
}

internal fun ParadoxScriptString.resolveTagConfig(): CwtTagConfig? {
	val tagConfigs = resolveTagConfigs() ?: return null
	return doResolveTagConfig(tagConfigs)
}

internal fun ParadoxScriptString.completeTagConfig(result: CompletionResultSet) {
	val tagConfigs = resolveTagConfigs(excludeExist = true) ?: return
	if(tagConfigs.isEmpty()) return
	val icon = PlsIcons.scriptTagIcon
	val tailText = " from tags"
	for(tagConfig in tagConfigs.values) {
		val name = tagConfig.name
		val element = tagConfig.pointer.element ?: continue
		val typeText = tagConfig.pointer.containingFile?.name ?: anonymousString
		val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
			.withTailText(tailText, true)
			.withTypeText(typeText, true)
			.withPriority(tagPriority)
		result.addElement(lookupElement)
	}
}

internal fun ParadoxScriptString.resolveTagConfigs(excludeExist: Boolean = false): Map<String, CwtTagConfig>? {
	val parent = parent
	if(parent !is ParadoxScriptBlock) return null
	val parentParent = parent.parent
	if(parentParent !is ParadoxScriptPropertyValue) return null
	val parentParentParent = parentParent.parent
	if(parentParentParent !is ParadoxScriptProperty) return null
	val definitionInfo = parentParentParent.definitionInfo ?: return null
	val tagConfigs = definitionInfo.configGroup.tagMap[definitionInfo.type] ?: return null
	if(tagConfigs.isEmpty()) return null
	//之前的PSI元素不允许有任何variable、property和value，除非可以被识别为tag
	val existTagNames = if(excludeExist) mutableSetOf<String>() else null
	siblings(forward = false, withSelf = false).forEach { prev ->
		if(prev is ParadoxScriptString) {
			val prevTagConfig = prev.doResolveTagConfig(tagConfigs)
			if(prevTagConfig != null) {
				existTagNames?.add(prevTagConfig.name)
				return@forEach
			}
		}
		if(prev is ParadoxScriptVariable || prev is ParadoxScriptProperty || prev is ParadoxScriptValue) return null
	}
	if(existTagNames != null) {
		run {
			siblings(withSelf = false).forEach { next ->
				if(next is ParadoxScriptString) {
					val nextTagConfig = next.doResolveTagConfig(tagConfigs)
					if(nextTagConfig != null) {
						existTagNames.add(nextTagConfig.name)
					}
				}
				if(next is ParadoxScriptVariable || next is ParadoxScriptProperty || next is ParadoxScriptValue) return@run
			}
		}
	}
	if(existTagNames != null) return tagConfigs.filterKeys { it !in existTagNames }
	return tagConfigs
}

internal fun ParadoxScriptString.doResolveTagConfig(tagConfigs: Map<String, CwtTagConfig>): CwtTagConfig? {
	//TODO 标签是否可以用引号包围？ -> 目前认为不可以
	val text = text
	if(text.isQuoted()) return null
	return tagConfigs[text]
}
//endregion