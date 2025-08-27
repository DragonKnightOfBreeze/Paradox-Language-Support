package icu.windea.pls.config.configExpression

import com.intellij.psi.PsiElement
import icu.windea.pls.config.configExpression.impl.CwtImageLocationExpressionResolverImpl
import icu.windea.pls.model.ImageFrameInfo

/**
 * CWT图片位置表达式。用于定位定义的相关图片。
 *
 * 语法与约定：
 * - 以 `|` 分隔参数：`<location>|<args...>`。
 * - 以 `$` 开头的参数表示从指定路径读取“名称文本”以替换占位符（支持逗号分隔多路径），映射到 [namePaths]。
 * - 其他参数表示帧数来源路径（支持逗号分隔多路径），映射到 [framePaths]。
 * - 当 [location] 含 `$` 时表示存在占位符，需要在后续步骤以“定义名或属性值”等替换。
 *
 * 示例：
 *
 * * `"gfx/interface/icons/modifiers/mod_$.dds"` -> 用当前定义的名字替换占位符，解析为图片路径。
 * * `"gfx/interface/icons/modifiers/mod_$.dds|$name"` -> 以上改为从指定路径（`name`）的属性值替换占位符（多路径逗号分隔）。
 * * `"GFX_$"` -> 用当前定义的名字替换占位符，解析为 sprite 名，再解析到图片路径。
 * * `"icon"` -> 读取定义声明的 `icon` 属性，解析为图片路径、sprite 名或定义名；若为定义名则继续解析其最相关图片。
 * * `"icon|p1,p2"` -> 以上并从 `p1`/`p2` 路径读取帧数用于后续切分。
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
