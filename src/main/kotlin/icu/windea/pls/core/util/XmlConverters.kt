package icu.windea.pls.core.util

import com.intellij.util.xmlb.*
import icu.windea.pls.core.*

class CommaDelimitedStringListConverter : Converter<List<String>>() {
    override fun fromString(value: String): List<String> {
        return value.toCommaDelimitedStringList()
    }
    
    override fun toString(value: List<String>): String {
        return value.toCommaDelimitedString()
    }
}

class CommaDelimitedStringSetConverter : Converter<Set<String>>() {
    override fun fromString(value: String): Set<String> {
        return value.toCommaDelimitedStringSet()
    }
    
    override fun toString(value: Set<String>): String {
        return value.toCommaDelimitedString()
    }
}
