package com.windea.plugin.idea.pls.config

class CwtConfigGroupsCache(
	configGroups:Map<String,Map<String, CwtConfig>>
){
	val cwtConfigGroups = configGroups
	
	/*
	TODO
	 * flat config
	   * types - types
	   * enums - enums
	   * definitions - *
	   * alias - alias
	   * root declarations - * (in root directory)
	 */
}