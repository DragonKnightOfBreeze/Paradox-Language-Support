package icu.windea.pls.lang.util.data

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.ep.util.data.ParadoxDefinitionData
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.lang.psi.select.booleanValue
import icu.windea.pls.lang.psi.select.colorValue
import icu.windea.pls.lang.psi.select.floatValue
import icu.windea.pls.lang.psi.select.intValue
import icu.windea.pls.lang.psi.select.stringValue
import icu.windea.pls.lang.psi.select.value
import java.awt.Color
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

class ParadoxScriptDataImpl(
    override val keyElement: ParadoxScriptPropertyKey?,
    override val valueElement: ParadoxScriptValue?,
    override val children: List<ParadoxScriptData>? = null
) : UserDataHolderBase(), ParadoxScriptData {
    override val map: Map<String?, List<ParadoxScriptData>>? by lazy {
        if (children == null) return@lazy null
        val map = mutableMapOf<String?, MutableList<ParadoxScriptData>>()
        for (child in children) {
            val childKeyElement = child.keyElement
            if (childKeyElement == null) {
                map.getOrPut(null) { mutableListOf() }.add(child)
            } else {
                val k = childKeyElement.name.lowercase()
                map.getOrPut(k) { mutableListOf() }.add(child)
            }
        }
        map
    }

    override fun getData(path: String): ParadoxScriptData? {
        val path0 = path.trimStart('/')
        if (path0.isEmpty()) return this
        val pathList = path0.lowercase().split('/')
        var current: ParadoxScriptData? = this
        for (p in pathList) {
            val k = if (p == "-") null else p
            current = current?.map?.get(k)?.firstOrNull()
        }
        if (current == null) return null
        return current
    }

    override fun getAllData(path: String): List<ParadoxScriptData> {
        val path0 = path.trimStart('/')
        if (path0.isEmpty()) return listOf(this)
        val pathList = path0.lowercase().split('/')
        var result: List<ParadoxScriptData> = listOf(this)
        for (p in pathList) {
            val k = if (p == "-") null else p
            result = result.flatMap { it.map?.get(k).orEmpty() }
        }
        return result
    }

    private val cacheKey = createKey<MutableMap<KType, Any?>>("paradox.script.data.value.cache")

    fun getValue(data: ParadoxScriptData, type: KType): Any? {
        return doGetValueFromCache(data, type)
    }

    private fun doGetValueFromCache(data: ParadoxScriptData, type: KType): Any? {
        val propertyValues = data.getOrPutUserData(cacheKey) { mutableMapOf() }
        return propertyValues.getOrPut(type) { doGetValue(data, type) ?: EMPTY_OBJECT }.takeUnless { it === EMPTY_OBJECT }
    }

    private fun doGetValue(data: ParadoxScriptData, type: KType): Any? {
        val value = data.valueElement ?: return null
        val kClass = type.classifier as? KClass<*> ?: return null
        return when {
            kClass == Any::class -> value.value()
            kClass == Boolean::class -> value.booleanValue()
            kClass == Int::class -> value.intValue()
            kClass == Float::class -> value.floatValue()
            kClass == String::class -> value.stringValue()
            kClass == Color::class -> value.colorValue()
            kClass.isSubclassOf(List::class) -> {
                val elementType = type.arguments.first().type ?: return null
                data.children?.mapNotNullTo(mutableListOf()) { doGetValue(it, elementType) }.orEmpty()
            }
            kClass.isSubclassOf(Collection::class) -> {
                val elementType = type.arguments.first().type ?: return null
                data.children?.mapNotNullTo(mutableSetOf()) { doGetValue(it, elementType) }.orEmpty()
            }
            kClass.isSubclassOf(ParadoxDefinitionData::class) -> {
                kClass.java.getConstructor(ParadoxScriptData::class.java).newInstance(data) as ParadoxDefinitionData
            }
            else -> null
        }
    }
}
