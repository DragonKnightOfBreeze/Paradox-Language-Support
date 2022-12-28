package icu.windea.pls.config.script.config

import com.fasterxml.jackson.dataformat.csv.*
import icu.windea.pls.config.script.*

data class ParadoxOnActionInfo(
	val key: String,
	val scopes: String,
	val event: String,
	val comment: String? = null
) {
	val scopeContext by lazy {
		val map = buildMap {
			scopesRegex.findAll(scopes).forEach {
				val k = it.groupValues.get(1)
				val v = it.groupValues.get(2)
				put(k, ParadoxScopeConfigHandler.getScopeId(v))
			}
		}
		ParadoxScopeContext.resolve(map)
	}
	
	companion object {
		private val scopesRegex = """(\w+)\s*=\s*(\w+)""".toRegex()
		
		val schema = CsvSchema.builder()
			.addColumn("key").addColumn("scopes").addColumn("event").addColumn("comment").build()
			.withHeader()
			.withQuoteChar('"')
	}
}