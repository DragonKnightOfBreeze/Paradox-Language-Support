package icu.windea.pls

import com.fasterxml.jackson.dataformat.csv.CsvSchema
import icu.windea.pls.config.script.config.*
import icu.windea.pls.core.*
import org.junit.Test
import java.io.*

class CsvTest {
	@Test
	fun test(){
		val file = File("cwt/cwtools-stellaris-config/config/Stellaris on_actions.csv")
		val schemaFor = CsvSchema.builder()
			.addColumn("key").addColumn("scopes").addColumn("event").addColumn("comment").build()
			.withHeader()
		csvMapper
		val readValues = csvMapper.readerFor(ParadoxOnActionConfig::class.java).with(schemaFor)
			.readValues<ParadoxOnActionConfig>(file)
		val r = readValues.readAll()
		r
	}
}