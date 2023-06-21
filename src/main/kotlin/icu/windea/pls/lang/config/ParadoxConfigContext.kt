package icu.windea.pls.lang.config

import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*

/**
 * CWT规则上下文。
 * 
 * 用于后续获取对应的所有可能的CWT规则以及匹配的CWT规则，从而提供高级语言功能。例如代码高亮、引用解析、代码补全。
 *
 * @property fileInfo 上下文文件或目录所在的文件信息。不一定是所在文件的文件信息，而是来自CWT类型规则。
 * @property definitionInfo 所在定义的定义信息。
 * @property elementPath 相对于所在文件的元素路径。如果在一个定义中，则是相对于这个定义的元素路径。
 * @property type 对应的位置是直接在文件中，还是直接在子句中，还是属性的值。
 * 
 * @see ParadoxConfigContextProvider
 */
interface ParadoxConfigContext {
    enum class Type {
        InFile,
        InClause,
        PropertyValue
    }
    
    val fileInfo: ParadoxFileInfo?
    
    val definitionInfo: ParadoxDefinitionInfo?
    
    val elementPath: ParadoxElementPath?
    
    val type: Type get() = Type.InClause
    
    fun getContextConfigs(matchOptions: Int = ParadoxConfigMatcher.Options.Default): List<CwtMemberConfig<*>>?
}