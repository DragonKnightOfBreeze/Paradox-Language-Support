package icu.windea.pls.tool

import com.fasterxml.jackson.dataformat.csv.*
import icu.windea.pls.core.util.*

val ObjectMappers.csvMapper by lazy {
    CsvMapper().apply {
        findAndRegisterModules()
        enable(CsvParser.Feature.TRIM_SPACES)
        enable(CsvParser.Feature.SKIP_EMPTY_LINES)
    }
}
