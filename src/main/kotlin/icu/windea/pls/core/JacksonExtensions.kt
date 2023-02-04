package icu.windea.pls.core

import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.csv.*
import com.fasterxml.jackson.module.kotlin.*

val jsonMapper by lazy {
    jacksonObjectMapper().apply {
        disable(SerializationFeature.INDENT_OUTPUT)
        enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
        enable(JsonParser.Feature.IGNORE_UNDEFINED)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }
}

val csvMapper by lazy {
    CsvMapper().apply {
        findAndRegisterModules()
        enable(CsvParser.Feature.TRIM_SPACES)
        enable(CsvParser.Feature.SKIP_EMPTY_LINES)
    }
}