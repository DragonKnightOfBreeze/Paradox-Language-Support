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
	private var shouldLoad = true
	private val ruleGroups = ConcurrentHashMap<String, ParadoxRuleGroup>()
	
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
		val jarEntries = jarFile.toJarDirectoryEntryMap("rules/")
		val concurrent = jarEntries.size
		//添加规则组
		val executor = Executors.newFixedThreadPool(concurrent)
		val countDown = CountDownLatch(concurrent)
		for((name,entries) in jarEntries) {
			try {
				executor.submit {
					//添加规则组
					val groupName = if(name.isEmpty()) "core" else name
					val group = mutableMapOf<String, Map<String, Any>>()
					for(entry in entries) {
						val fileName = entry.name.substringAfter('/')
						if(fileName.endsWith(".yml")) {
							val ruleName = fileName.substringBeforeLast('.')
							val rule = getRule(jarFile.getInputStream(entry))
							group[ruleName] = rule
						}
					}
					ruleGroups[groupName] = ParadoxRuleGroup(group)
					countDown.countDown()
				}
			} catch(e: Exception) {
				e.printStackTrace()
			}
		}
		countDown.await()
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