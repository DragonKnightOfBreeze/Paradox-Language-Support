package icu.windea.pls.core.data

import com.fasterxml.jackson.dataformat.csv.*

val csvMapper by lazy {
    CsvMapper().apply {
        findAndRegisterModules()
        enable(CsvParser.Feature.TRIM_SPACES)
        enable(CsvParser.Feature.SKIP_EMPTY_LINES)
    }
}