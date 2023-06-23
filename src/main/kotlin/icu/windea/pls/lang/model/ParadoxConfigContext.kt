package icu.windea.pls.lang.model

import com.google.common.cache.*
import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.ParadoxConfigMatcher.Options
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.script.psi.*

/**
 * CWT规则上下文。
 * 
 * 用于后续获取对应的所有可能的CWT规则以及匹配的CWT规则，从而提供高级语言功能。例如代码高亮、引用解析、代码补全。
 * 
 * 可以获取CWT规则上下文不意味着可以获取对应的所有可能的CWT规则。
 *
 * @property fileInfo 所在文件的文件信息。
 * @property definitionInfo 所在定义的定义信息。
 * @property elementPath 相对于所在文件的元素路径。
 * @property elementPathFromDefinition 相对于所在定义的元素路径。如果不在定义声明中，则为null。
 * 
 * @see ParadoxConfigContextProvider
 */
class ParadoxConfigContext(
    val fileInfo: ParadoxFileInfo?,
    val elementPath: ParadoxElementPath?,
    val definitionInfo: ParadoxDefinitionInfo?,
    val elementPathFromDefinition: ParadoxElementPath?,
    val gameType: ParadoxGameType,
    val configGroup: CwtConfigGroup,
    val element: ParadoxScriptMemberElement
): UserDataHolderBase() {
    private val configsCache: Cache<String, List<CwtMemberConfig<*>>> = CacheBuilder.newBuilder().buildCache()
    
    fun getConfigs(matchOptions: Int = Options.Default) : List<CwtMemberConfig<*>> {
        val cachedKey = buildString { 
            append('#').append(matchOptions)
        }
        return configsCache.getOrPut(cachedKey) { provider?.getConfigs(element, this, matchOptions).orEmpty() }
    }
    
    fun isDefinition(): Boolean {
        return elementPathFromDefinition != null && elementPathFromDefinition.isEmpty()
    }
    
    fun isDefinitionMember(): Boolean {
        return elementPathFromDefinition != null
    }
    
    object Keys
}

val ParadoxConfigContext.Keys.provider by lazy { Key.create<ParadoxConfigContextProvider>("paradox.configContext.provider") }

var ParadoxConfigContext.provider by ParadoxConfigContext.Keys.provider