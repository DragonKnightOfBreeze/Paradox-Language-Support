package icu.windea.pls.config

import com.google.common.cache.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.CwtConfigMatcher.Options
import icu.windea.pls.lang.config.*
import icu.windea.pls.model.*
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
 *
 * @see CwtConfigContextProvider
 */
class CwtConfigContext(
    val element: ParadoxScriptMemberElement,
    val fileInfo: ParadoxFileInfo?,
    val elementPath: ParadoxElementPath?,
    val gameType: ParadoxGameType,
    val configGroup: CwtConfigGroup,
) : UserDataHolderBase() {
    fun getConfigs(matchOptions: Int = Options.Default): List<CwtMemberConfig<*>> {
        val rootFile = selectRootFile(element) ?: return emptyList()
        val project = configGroup.project
        val cache = project.configsCache.value.get(rootFile)
        val cachedKey = doGetCacheKey(matchOptions) ?: return emptyList()
        val cached = cache.getOrPut(cachedKey) {
            doGetConfigs(matchOptions)
        }
        //some configs cannot be cached (e.g. from overridden configs)
        if(PlsContext.overrideConfigStatus.get() == true) cache.invalidate(cachedKey)
        PlsContext.overrideConfigStatus.remove()
        return cached
    }
    
    private fun doGetCacheKey(matchOptions: Int): String? {
        return provider!!.getCacheKey(this, matchOptions)
    }
    
    private fun doGetConfigs(matchOptions: Int): List<CwtMemberConfig<*>> {
        return provider!!.getConfigs(this, matchOptions).orEmpty()
    }
    
    fun skipMissingExpressionCheck(): Boolean {
        return provider!!.skipMissingExpressionCheck(this)
    }
    
    fun skipTooManyExpressionCheck(): Boolean {
        return provider!!.skipTooManyExpressionCheck(this)
    }
    
    object Keys : KeysAware
}

//project -> rootFile -> cacheKey -> configs
//depends on (definition, localisation, etc.) indices
//use soft values to optimize memory
private val PlsKeys.configsCache by createCachedValueKey("paradox.configContext.configs") {
    NestedCache<VirtualFile, _, _, _> { CacheBuilder.newBuilder().buildCache<String, List<CwtMemberConfig<*>>>() }
        .withDependencyItems(PsiModificationTracker.MODIFICATION_COUNT)
}
private val Project.configsCache by PlsKeys.configsCache

val CwtConfigContext.Keys.definitionInfo by createKey<ParadoxDefinitionInfo>("paradox.configContext.definitionInfo")
val CwtConfigContext.Keys.elementPathFromRoot by createKey<ParadoxElementPath>("paradox.configContext.elementPathFromRoot")
val CwtConfigContext.Keys.provider by createKey<CwtConfigContextProvider>("paradox.configContext.provider")

var CwtConfigContext.definitionInfo by CwtConfigContext.Keys.definitionInfo
var CwtConfigContext.elementPathFromRoot by CwtConfigContext.Keys.elementPathFromRoot
var CwtConfigContext.provider by CwtConfigContext.Keys.provider

fun CwtConfigContext.isDefinition(): Boolean {
    return definitionInfo != null && elementPathFromRoot.let { it != null && it.isEmpty() }
}

fun CwtConfigContext.isRootOrMember(): Boolean {
    return elementPathFromRoot != null
}
