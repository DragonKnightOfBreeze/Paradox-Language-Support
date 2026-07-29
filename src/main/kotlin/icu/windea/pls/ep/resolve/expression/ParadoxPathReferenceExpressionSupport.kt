package icu.windea.pls.ep.resolve.expression

import com.intellij.openapi.extensions.ExtensionPointListener
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.collections.findFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.lang.index.ParadoxFilePathIndex

/**
 * 提供对路径引用表达式的支持。
 *
 * 用于实现如何匹配、解析脚本文件中使用的路径表达式，以及如何基于文件路径索引进行代码补全等功能。
 *
 * @see CwtDataExpression
 * @see CwtDataType
 * @see CwtDataTypeSets.PathReference
 * @see ParadoxFilePathIndex
 */
interface ParadoxPathReferenceExpressionSupport {
    fun supports(dataType: CwtDataType): Boolean

    /**
     * 判断指定的文件路径表达式是否匹配另一个相对于入口目录的路径。
     */
    fun matches(configExpression: CwtDataExpression, element: PsiElement?, filePath: String): Boolean

    /**
     * 根据指定的文件路径表达式，从精确路径中提取出需要的作为值的字符串。即脚本文件中使用的路径表达式。
     *
     * @param configExpression 对应的规则表达式。拥有数种写法的文件路径表达式。
     * @param ignoreCase 匹配时是否需要忽略大小写。
     */
    fun extract(configExpression: CwtDataExpression, element: PsiElement?, filePath: String, ignoreCase: Boolean = false): String?

    /**
     * 是否可用于解析指定的路径引用。这不意味着可以成功解析为文件路径、文件名或者最终的文件。
     *
     * @param configExpression 对应的规则表达式。拥有数种写法的文件路径表达式。
     * @param pathReference 指定的路径引用。即脚本文件中使用的路径引用表达式。
     */
    fun canResolve(configExpression: CwtDataExpression, pathReference: String): Boolean

    /**
     * 解析指定的路径引用，得到文件路径。如果返回 `null` 则表示无法仅基于这些参数得到完整的文件路径。
     *
     * @param configExpression 对应的规则表达式。拥有数种写法的文件路径表达式。
     * @param pathReference 指定的路径引用。即脚本文件中使用的路径引用表达式。
     */
    fun resolvePath(configExpression: CwtDataExpression, pathReference: String): Set<String>?

    /**
     * 解析指定的路径引用，得到文件名。
     *
     * @param configExpression 对应的规则表达式。拥有数种写法的文件路径表达式。
     * @param pathReference 指定的路径引用。即脚本文件中使用的路径引用表达式。
     */
    fun resolveFileName(configExpression: CwtDataExpression, pathReference: String): Set<String>?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxPathReferenceExpressionSupport>("icu.windea.pls.pathReferenceExpressionSupport")
        @JvmField val CACHE = LazyValue<Map<CwtDataType, ParadoxPathReferenceExpressionSupport>>()

        fun get(dataType: CwtDataType): ParadoxPathReferenceExpressionSupport? = CACHE.get()?.get(dataType)

        // region Implementations

        init {
            computeCache()
            addListener()
        }

        private fun computeCache() {
            CACHE.reinitialize {
                val result = mutableMapOf<CwtDataType, ParadoxPathReferenceExpressionSupport>()
                val eps = EP_NAME.extensionList
                CwtDataType.entries.values.forEach { dataType -> eps.findFast { ep -> ep.supports(dataType) }?.let { result[dataType] = it } }
                result.optimized()
            }
        }

        private fun addListener() {
            EP_NAME.addExtensionPointListener(object : ExtensionPointListener<ParadoxPathReferenceExpressionSupport> {
                override fun extensionAdded(extension: ParadoxPathReferenceExpressionSupport, pluginDescriptor: PluginDescriptor) = computeCache()
                override fun extensionRemoved(extension: ParadoxPathReferenceExpressionSupport, pluginDescriptor: PluginDescriptor) = computeCache()
            })
        }

        // endregion
    }
}
