package icu.windea.pls.dev.cwt

import com.fasterxml.jackson.dataformat.csv.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*
import java.io.*

/**
 * 用于从`on_actions.csv`生成`on_actions.cwt`
 */
class CwtOnActionConfigGenerator(
    val gameType: ParadoxGameType,
    val csvPath: String,
    val cwtPath: String,
) {
    class OnActonInfo(
        val key: String,
        val scopes: String,
        val event: String,
        val comment: String? = null
    )
    
    val schema = CsvSchema.builder()
        .addColumn("key").addColumn("scopes").addColumn("event").addColumn("comment").build()
        .withHeader()
        .withQuoteChar('"')
    
    fun generate() {
        val csvFile = File(csvPath)
        val cwtFile = File(cwtPath)
        val infos = csvMapper.readerFor(OnActonInfo::class.java).with(schema).readValues<OnActonInfo>(csvFile).readAll()
        val text = buildString { 
            append("on_actions = {\n")
            var isFirst = true
            infos.forEach { info ->
                if(isFirst) {
                    isFirst = false
                } else {
                    append("\n\n")
                }
                val comment = info.comment?.takeIfNotEmpty()
                comment?.split("\\n")?.forEach { append(it).append("\n") }
                val scopes = info.scopes.trim()
                append("replace_scopes = { ").append(scopes).append(" }")
                val name = info.key
                val event = info.event
                append("$name = $event\n")
            }
            append("}\n")
        }
        cwtFile.writeText(text)
    }
}