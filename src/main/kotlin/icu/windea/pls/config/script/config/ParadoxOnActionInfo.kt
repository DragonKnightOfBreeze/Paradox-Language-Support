package icu.windea.pls.config.script.config

import com.fasterxml.jackson.dataformat.csv.*
import icu.windea.pls.core.*

data class ParadoxOnActionInfo(
	val key: String,
	val scopes: String,
	val event: String,
	val comment: String? = null
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
		ParadoxScopeContext.resolve(map)
	}
	
	companion object{
		val schema = CsvSchema.builder()
			.addColumn("key").addColumn("scopes").addColumn("event").addColumn("comment").build()
			.withHeader()
			.withQuoteChar('"')
	}
}