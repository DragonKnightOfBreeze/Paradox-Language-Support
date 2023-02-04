package icu.windea.pls

import com.fasterxml.jackson.dataformat.csv.CsvSchema
import icu.windea.pls.lang.model.*
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
		val readValues = csvMapper.readerFor(ParadoxOnActionInfo::class.java).with(schemaFor)
			.readValues<ParadoxOnActionInfo>(file)
		val r = readValues.readAll()
		r
	}
}