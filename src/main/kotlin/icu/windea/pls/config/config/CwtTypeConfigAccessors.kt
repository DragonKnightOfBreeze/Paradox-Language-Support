package icu.windea.pls.config.config

import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*

val CwtTypeConfig.possibleRootKeys: Set<String> by createKeyDelegate(CwtTypeConfig.Keys) {
    caseInsensitiveStringSet().apply {
        typeKeyFilter?.takeIfTrue()?.let { addAll(it) }
        subtypes.values.forEach { subtype -> subtype.typeKeyFilter?.takeIfTrue()?.let { addAll(it) } }
    }
}

val CwtTypeConfig.possibleSwappedTypeRootKeys by createKeyDelegate(CwtTypeConfig.Keys) {
    caseInsensitiveStringSet().apply {
        configGroup.swappedTypes.values.forEach f@{ swappedTypeConfig ->
            val baseType = swappedTypeConfig.baseType ?: return@f
            val baseTypeName = baseType.substringBefore('.')
            if(baseTypeName != name) return@f
            val rootKey = swappedTypeConfig.typeKeyFilter?.takeIfTrue()?.singleOrNull() ?: return@f
            add(rootKey)
        }
    }
}

val CwtTypeConfig.possibleNestedTypeRootKeys by createKeyDelegate(CwtTypeConfig.Keys) {
    caseInsensitiveStringSet().apply {
        addAll(possibleSwappedTypeRootKeys)
    }
}
