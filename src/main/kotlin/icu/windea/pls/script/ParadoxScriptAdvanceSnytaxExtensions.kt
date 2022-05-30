package icu.windea.pls.script

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.script.psi.*

//region tag
fun ParadoxScriptString.resolveTagConfig(): CwtTagConfig? {
	val tagConfigs = resolveTagConfigs() ?: return null
	return doResolveTagConfig(tagConfigs)
}

private fun ParadoxScriptString.resolveTagConfigs(excludeExist: Boolean = false): Map<String, CwtTagConfig>? {
	val parent = parent
	if(parent !is IParadoxScriptBlock) return null
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

private fun ParadoxScriptString.doResolveTagConfig(tagConfigs: Map<String, CwtTagConfig>): CwtTagConfig? {
	//目前认为不可以用引号包围
	val text = text
	if(text.isQuoted()) return null
	return tagConfigs[text]
}
//endregion