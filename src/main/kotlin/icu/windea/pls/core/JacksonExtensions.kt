package icu.windea.pls.core

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.module.kotlin.*

val jsonMapper by lazy { jacksonObjectMapper() }

val csvMapper by lazy { CsvMapper().apply {
	findAndRegisterModules()
	enable(CsvParser.Feature.TRIM_SPACES)
	enable(CsvParser.Feature.SKIP_EMPTY_LINES)
} }