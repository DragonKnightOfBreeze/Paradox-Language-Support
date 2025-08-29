package icu.windea.pls.tool

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import icu.windea.pls.core.util.ObjectMappers

val ObjectMappers.csvMapper by lazy {
    CsvMapper().apply {
        findAndRegisterModules()
        enable(CsvParser.Feature.TRIM_SPACES)
        enable(CsvParser.Feature.SKIP_EMPTY_LINES)
    }
}
