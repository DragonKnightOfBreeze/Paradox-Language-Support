package com.windea.plugin.idea.paradox.util

import com.windea.plugin.idea.paradox.*
import org.yaml.snakeyaml.*
import java.io.*
import java.util.concurrent.*

/**
 * Paradox规则组映射的提供器。
 */
object ParadoxRuleGroupProvider {
	@Volatile
	private var shouldLoad = true
	private val ruleGroups: MutableMap<String, ParadoxRuleGroup> = ConcurrentHashMap()
	
	fun getRuleGroups(): Map<String, ParadoxRuleGroup> {
		if(shouldLoad) {
			synchronized(ruleGroups){
				if(shouldLoad) {
					shouldLoad = false
					addRuleGroups()
				}
			}
		}
		return ruleGroups
	}
	
	private fun addRuleGroups() {
		val jarFile = "rules".toJarFile()
		val jarEntries = jarFile.toJarDirectoryEntryMap()
		val concurrent = jarEntries.size
		//并发添加规则组
		val executor = Executors.newFixedThreadPool(concurrent)
		for((name,entries) in jarEntries) {
			try {
				executor.submit {
					//添加规则组
					val groupName = if(name.isEmpty()) "core" else name
					val group = mutableMapOf<String, Map<String, Any>>()
					for(entry in entries) {
						val ruleName = entry.name.substringAfter('/')
						val rule = getRule(jarFile.getInputStream(entry))
						group[ruleName] = rule
					}
					ruleGroups[groupName] = ParadoxRuleGroup(group)
				}.get()
			} catch(e: Exception) {
				e.printStackTrace()
			}
		}
	}
	
	private fun getRule(inputStream: InputStream): Map<String, Map<String, Any>> {
		try {
			return extractRule(inputStream)
		} catch(e: Exception) {
			e.printStackTrace()
			return emptyMap()
		}
	}
	
	private val yaml = Yaml()
	
	private fun extractRule(inputStream: InputStream): Map<String, Map<String,Any>> {
		return yaml.load(inputStream) ?: emptyMap()
	}
}