package icu.windea.pls.core

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.csv.*
import com.fasterxml.jackson.module.kotlin.*

val jsonMapper by lazy {
    jacksonObjectMapper().apply {
        disable(SerializationFeature.INDENT_OUTPUT)
    }
}

val csvMapper by lazy {
    CsvMapper().apply {
        findAndRegisterModules()
        enable(CsvParser.Feature.TRIM_SPACES)
        enable(CsvParser.Feature.SKIP_EMPTY_LINES)
    }
}