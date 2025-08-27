package icu.windea.pls.config.configExpression

import com.intellij.psi.PsiElement
import icu.windea.pls.config.configExpression.impl.CwtImageLocationExpressionResolverImpl
import icu.windea.pls.model.ImageFrameInfo

/**
 * CWT图片位置表达式。用于定位定义的相关图片。
 *
 * 示例：
 *
 * * `"gfx/interface/icons/modifiers/mod_$.dds"` -> 用当前定义的名字替换占位符，解析为图片的路径。
 * * `"gfx/interface/icons/modifiers/mod_$.dds|$name"` -> 在前者的基础上，改为用指定路径（`name`）的属性的值替换占位符（如果值存在且可以获取），解析为图片的路径。这里的路径可以有多个，逗号分割。
 * * `"GFX_$"` -> 用当前定义的名字替换占位符，解析为sprite的名字，继而解析为图片的路径。
 * * `"icon"` -> 得到当前定义声明中指定路径（`title`）的属性的值，解析为图片的路径、sprite的名字或者定义的名字。如果是定义的名字，则会得到该定义的最相关的图片，继续向下解析。
 * * `"icon|p1,p2"` -> 在前者的基础上，用指定路径（`p1`或`p2`）的属性的值作为帧数，用于后续切分图片。
 *
 * @property namePaths 用于获取名字文本的一组表达式路径。名字文本用于替换占位符。
 * @property framePaths 用于获取帧数的一组表达式路径。帧数用于后续切分图片。
 */
interface CwtImageLocationExpression : CwtLocationExpression {
    val namePaths: Set<String>
    val framePaths: Set<String>

    operator fun component3() = namePaths
    operator fun component4() = framePaths

    class ResolveResult(
        val nameOrFilePath: String,
        val frameInfo: ImageFrameInfo? = null,
        val message: String? = null,
        resolveAction: () -> PsiElement? = { null },
        resolveAllAction: () -> Collection<PsiElement> = { emptySet() },
    ) {
        val element: PsiElement? by lazy { resolveAction() }
        val elements: Collection<PsiElement> by lazy { resolveAllAction() }
    }

    interface Resolver {
        fun resolveEmpty(): CwtImageLocationExpression
        fun resolve(expressionString: String): CwtImageLocationExpression
    }

    companion object : Resolver by CwtImageLocationExpressionResolverImpl()
}
