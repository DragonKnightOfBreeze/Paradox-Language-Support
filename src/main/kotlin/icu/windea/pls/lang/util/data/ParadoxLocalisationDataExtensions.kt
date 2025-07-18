@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.lang.util.data

import icu.windea.pls.core.util.*

inline operator fun ParadoxLocalisationData.Locale.component1() = name

inline operator fun ParadoxLocalisationData.PropertyList.component1() = locale
inline operator fun ParadoxLocalisationData.PropertyList.component2() = items

inline operator fun ParadoxLocalisationData.Property.component1() = key
inline operator fun ParadoxLocalisationData.Property.component2() = value

fun ParadoxLocalisationData.PropertyList.toMap(): Map<String, String> {
    val data = this
    return buildMap {
        data.items.forEach f@{ item ->
            this[item.key] = item.value ?: return@f
        }
    }
}

fun ParadoxLocalisationData.PropertyList.toPairList(): List<Pair<String, String>> {
    val data = this
    return buildList {
        data.items.forEach f@{ item ->
            this += tupleOf(item.key, item.value ?: return@f)
        }
    }
}

fun List<ParadoxLocalisationData.PropertyList>.toMapGroup(): Map<String, Map<String, String>> {
    val allData = this
    return buildMap {
        allData.forEach { data ->
            val localeId = data.locale?.name.orEmpty()
            val map = getOrPut(localeId) { mutableMapOf() } as MutableMap
            data.items.forEach f@{ item ->
                map[item.key] = item.value ?: return@f
            }
        }
    }
}

fun List<ParadoxLocalisationData.PropertyList>.toPairListGroup(): Map<String, List<Pair<String, String>>> {
    val allData = this
    return buildMap {
        allData.forEach { data ->
            val localeId = data.locale?.name.orEmpty()
            val list = getOrPut(localeId) { mutableListOf() } as MutableList
            data.items.forEach f@{ item ->
                list += tupleOf(item.key, item.value ?: return@f)
            }
        }
    }
}
