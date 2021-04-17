package com.windea.plugin.idea.paradox.util

import com.windea.plugin.idea.paradox.*
import org.yaml.snakeyaml.*
import java.io.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*

/**
 * Paradox规则组映射的提供器。
 */
object ParadoxRuleGroupProvider {
	private val yaml = Yaml()
	private val ruleGroups = ConcurrentHashMap<String, ParadoxRuleGroup>()
	
	@Volatile private var shouldLoad = true
	
	init{
		addRuleGroups()
	}
	
	@Synchronized
	fun getRuleGroups(): Map<String, ParadoxRuleGroup> {
		if(shouldLoad) {
			shouldLoad = false
			addRuleGroups()
		}
		return ruleGroups
	}
	
	private fun addRuleGroups() {
		val jarFile = "rules".toJarFile()
		val entryGroup = jarFile.toJarDirectoryEntryMap("rules/",".yml")
		//添加规则组
		for((name,entries) in entryGroup) {
			try {
				//添加规则组
				val groupName = if(name.isEmpty()) "core" else name
				val group = mutableMapOf<String, Map<String, Any>>()
				for(entry in entries) {
					val rule = getRule(jarFile.getInputStream(entry))
					group.putAll(rule)
				}
				ruleGroups[groupName] = ParadoxRuleGroup(group)
			} catch(e: Exception) {
				e.printStackTrace()
			}
		}
	}
	
	private fun getRule(inputStream: InputStream): Map<String, Map<String, Any>> {
		try {
			return extractRule(inputStream)
		} catch(e: Exception) {
			return emptyMap()
		}
	}
	
	private fun extractRule(inputStream: InputStream): Map<String, Map<String,Any>> {
		return yaml.load(inputStream) ?: emptyMap()
	}
}