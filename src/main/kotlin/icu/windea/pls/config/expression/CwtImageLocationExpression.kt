package icu.windea.pls.config.expression

import com.google.common.cache.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*

/**
 * CWT图片位置表达式。用于定位定义的相关图片。
 *
 * 如果包含占位符`$`，将其替换成定义的名字后，尝试得到对应路径的图片。
 * 否则尝试得到对应表达式路径的值对应的图片。
 *
 * 示例：`"gfx/interface/icons/modifiers/mod_$.dds"`, "GFX_$", `"icon"`, "icon|p1,p2"`
 *
 * @property placeholder 占位符文本。其中的`"$"`会在解析时被替换成定义的名字。
 * @property path 表达式路径。用于获取图片的引用文本。
 * @property framePaths 用于获取帧数的一组表达式路径。帧数用于后续切分图片。
 */
interface CwtImageLocationExpression : CwtExpression {
    val placeholder: String?
    val path: String?
    val framePaths: Set<String>?

    fun resolvePlaceholder(name: String): String?

    data class ResolveResult(
        val nameOrFilePath: String,
        val element: PsiElement?,
        val frameInfo: ImageFrameInfo? = null,
        val message: String? = null
    )

    data class ResolveAllResult(
        val nameOrFilePath: String,
        val elements: Set<PsiElement>,
        val frameInfo: ImageFrameInfo? = null,
        val message: String? = null
    )

    companion object Resolver {
        val EmptyExpression: CwtImageLocationExpression = doResolveEmpty()

        fun resolve(expressionString: String): CwtImageLocationExpression {
            if (expressionString.isEmpty()) return EmptyExpression
            return cache.get(expressionString)
        }
    }
}

//Implementations (cached & not interned)

private val cache = CacheBuilder.newBuilder().buildCache<String, CwtImageLocationExpression> { doResolve(it) }

private fun doResolveEmpty() = CwtImageLocationExpressionImpl("", path = "")

private fun doResolve(expressionString: String): CwtImageLocationExpression {
    return when {
        expressionString.isEmpty() -> CwtImageLocationExpression.EmptyExpression
        expressionString.contains('$') -> {
            val placeholder = expressionString
            CwtImageLocationExpressionImpl(expressionString, placeholder = placeholder)
        }
        else -> {
            val path = expressionString.substringBefore('|')
            val framePaths = expressionString.substringAfter('|', "").orNull()
                ?.toCommaDelimitedStringSet()
            CwtImageLocationExpressionImpl(expressionString, path = path, framePaths = framePaths)
        }
    }
}

private class CwtImageLocationExpressionImpl(
    override val expressionString: String,
    override val placeholder: String? = null,
    override val path: String? = null,
    override val framePaths: Set<String>? = null
) : CwtImageLocationExpression {
    override fun resolvePlaceholder(name: String): String? {
        if (placeholder == null) return null
        return buildString { for (c in placeholder) if (c == '$') append(name) else append(c) }
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtImageLocationExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int {
        return expressionString.hashCode()
    }

    override fun toString(): String {
        return expressionString
    }
}
