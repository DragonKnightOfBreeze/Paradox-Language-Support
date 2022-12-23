package icu.windea.pls.config.script.config

import icu.windea.pls.core.*

data class ParadoxOnActionConfig(
	val key: String,
	val scopes: String,
	val event: String,
	val comment: String?
) {
	val scopeContext by lazy {
		val map = buildMap {
			for(s in scopes.splitByBlank()) {
				val i = s.indexOf('=')
				val k = s.substring(0, i).trim().lowercase()
				val v = s.substring(i + 1).trim()
				put(k, v)
			}
		}
		ParadoxScopeConfig.resolve(map)
	}
}