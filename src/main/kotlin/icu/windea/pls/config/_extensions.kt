@file:Suppress("unused")

package icu.windea.pls.config

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.CwtFilePathMatchableConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigExpressionManager
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.collections.toListOrThis
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removePrefixOrNull

val CwtMemberConfig<*>.documentation: String? get() = CwtConfigManager.getDocumentation(this)

val CwtFilePathMatchableConfig.filePathPatterns: Set<String> get() = CwtConfigManager.getFilePathPatterns(this)

val CwtFilePathMatchableConfig.filePathPatternsForPriority: Set<String> get() = CwtConfigManager.getFilePathPatternsForPriority(this)

inline fun <T> Collection<T>.sortedByPriority(crossinline expressionProvider: (T) -> CwtDataExpression?, crossinline configGroupProvider: (T) -> CwtConfigGroup): List<T> {
    if (size <= 1) return toListOrThis()
    return sortedByDescending s@{
        val expression = expressionProvider(it) ?: return@s Double.MAX_VALUE
        val configGroup = configGroupProvider(it)
        CwtConfigExpressionManager.getPriority(expression, configGroup)
    }
}

/**
 * 判断两个规则对象是否指向同一 [PsiElement]。
 */
infix fun CwtConfig<*>?.isSamePointer(other: CwtConfig<*>?): Boolean {
    if (this == null || other == null) return false
    // NOTE 2.1.1 reference equals can be used here & empty pointers are never same
    return pointer === other.pointer && pointer !== emptyPointer<PsiElement>()
}

/**
 * 尝试解析当前规则指向的 [PsiElement]，并绑定当前规则。
 */
fun <T : PsiElement> CwtConfig<T>?.resolveElementWithConfig(): T? {
    val element = this?.pointer?.element ?: return null
    element.putUserData(CwtConfigManager.Keys.config, this)
    return element
}

// in order to be compatible with eu5 config files
private val pathPrefixes = arrayOf("game/", "game/in_game/", "game/main_menu/", "game/loading_screen/")

fun String.optimizedPath(): String {
    val r = pathPrefixes.firstNotNullOfOrNull { removePrefixOrNull(it) } ?: this
    return r.normalizePath().optimized()
}

fun String.optimizedPathExtension(): String {
    val r = removePrefix(".")
    return r.optimized()
}
