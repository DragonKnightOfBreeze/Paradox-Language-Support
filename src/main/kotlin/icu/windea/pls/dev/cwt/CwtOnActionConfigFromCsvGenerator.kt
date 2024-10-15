package icu.windea.pls.dev.cwt

import com.fasterxml.jackson.dataformat.csv.*
import icu.windea.pls.core.*
import icu.windea.pls.core.data.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import java.io.*

/**
 * 用于从`on_actions.csv`生成`on_actions.cwt`
 */
class CwtOnActionConfigFromCsvGenerator(
    val gameType: ParadoxGameType,
    val csvPath: String,
    val cwtPath: String,
) {
    class OnActonInfo(
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
                    put(k.lowercase(), ParadoxScopeManager.getScopeId(v))
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

    fun generate() {
        val csvFile = File(csvPath)
        val cwtFile = File(cwtPath)
        val infos = csvMapper.readerFor(OnActonInfo::class.java).with(OnActonInfo.schema).readValues<OnActonInfo>(csvFile).readAll()
        val text = buildString {
            append("on_actions = {\n")
            var isFirst = true
            infos.forEach { info ->
                val name = info.key
                val event = info.event
                val scopeContext = info.scopeContext
                val comment = info.comment?.orNull()
                if (name.isEmpty() || event.isEmpty() || scopeContext == null) return@forEach
                if (isFirst) {
                    isFirst = false
                } else {
                    append("\n")
                }
                comment?.split("\\n")?.forEach { append("\t### ").append(it).append("\n") }
                append("\t## replace_scopes = { ")
                scopeContext.toScopeMap().forEach { (k, v) ->
                    append(k).append(" = ").append(v).append(" ")
                }
                append("}\n")
                append("\t## event_type = ").append(event).append("\n")
                append("\t").append(name).append(" = {}").append("\n")
            }
            append("}\n")
        }
        cwtFile.writeText(text)
    }
}
