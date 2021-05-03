package com.windea.plugin.idea.pls.config

import com.intellij.openapi.application.*
import com.intellij.openapi.vfs.*
import com.windea.plugin.idea.pls.*
import org.yaml.snakeyaml.*
import java.io.*
import java.util.concurrent.*


class ParadoxRuleGroupProvider {
	private val yaml = Yaml()
	
	internal val ruleGroupsCache: ParadoxRuleGroupsCache
	
	init {
		ruleGroupsCache = ReadAction.compute<ParadoxRuleGroupsCache,Exception> { 
			val ruleGroups = initRuleGroups()
			ParadoxRuleGroupsCache(ruleGroups)
		}
	}
	
	@Synchronized
	private fun initRuleGroups(): Map<String, ParadoxRuleGroup> {
		val ruleGroups = ConcurrentHashMap<String, ParadoxRuleGroup>()
		val rulesUrl = "/rules".toUrl(locationClass)
		val rulesFile = VfsUtil.findFileByURL(rulesUrl) ?: error("Paradox rules path '$rulesUrl' is not exist.")
		val coreGroup = ConcurrentHashMap<String, Map<String, Any>>()
		val coreGroupName = "core"
		for(child in rulesFile.children) {
			if(child.isDirectory) {
				val group = ConcurrentHashMap<String, Map<String, Any>>()
				val groupName = child.name
				for(file in child.children) {
					if(file.extension == "yml") {
						val rule = getRule(file.inputStream)
						group.putAll(rule)
					}
				}
				ruleGroups[groupName] = ParadoxRuleGroup(group)
			} else {
				val ruleFile = child
				if(ruleFile.extension == "yml") {
					val rule = getRule(ruleFile.inputStream)
					coreGroup.putAll(rule)
				}
			}
		}
		ruleGroups[coreGroupName] = ParadoxRuleGroup(coreGroup)
		return ruleGroups
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