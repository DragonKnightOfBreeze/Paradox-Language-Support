package icu.windea.pls.lang.config

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*

/**
 * CWT规则上下文。
 * 
 * 用于后续获取对应的所有可能的CWT规则以及匹配的CWT规则，从而提供高级语言功能。例如代码高亮、引用解析、代码补全。
 * 
 * 可以获取CWT规则上下文不意味着可以获取对应的所有可能的CWT规则。
 *
 * @property type 对应的位置是直接在文件中，还是直接在子句中，还是属性的值。
 * @property fileInfo 所在文件的文件信息。
 * @property definitionInfo 所在定义的定义信息。
 * @property elementPath 相对于所在文件的元素路径。如果在一个定义中，则是相对于这个定义的元素路径。
 * 
 * @see ParadoxConfigContextProvider
 */
class ParadoxConfigContext(
    val type: Type,
    val fileInfo: ParadoxFileInfo? = null,
    val definitionInfo: ParadoxDefinitionInfo? = null,
    val elementPath: ParadoxElementPath? = null,
): UserDataHolderBase() {
    enum class Type {
        InFile,
        InClause,
        PropertyValue
    }
    
    object Keys
}

val ParadoxConfigContext.Keys.provider by lazy { Key.create<ParadoxConfigContextProvider>("paradox.configContext.provider") }

var ParadoxConfigContext.provider by ParadoxConfigContext.Keys.provider