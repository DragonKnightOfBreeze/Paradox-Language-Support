package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import icu.windea.pls.config.configExpression.CwtImageLocationExpression
import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.config.configExpression.CwtLocationExpression
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 位置信息的解析结果。
 *
 * @property name 名字。如果为 `null`，则表示无法解析为静态结果。
 * @property element 解析得到的单个目标。
 * @property elements 解析得到的一组目标。
 *
 * @see CwtLocationExpression
 */
interface CwtLocationResolveResult<T> {
    val name: String? get() = null
    val element: T? get() = null
    val elements: Collection<T> get() = emptySet()

    operator fun component1() = name
    operator fun component2() = element
    operator fun component3() = elements
}

/**
 * 本地化的位置信息的解析结果。
 *
 * @property name 本地化的名字。如果为 `null`，则表示无法解析为静态结果。
 *
 * @see CwtLocalisationLocationExpression
 */
sealed class CwtLocalisationLocationResolveResult : CwtLocationResolveResult<ParadoxLocalisationProperty> {
    class Static(
        override val name: String,
        resolveAction: () -> ParadoxLocalisationProperty? = { null },
        resolveAllAction: () -> Collection<ParadoxLocalisationProperty> = { emptySet() },
    ) : CwtLocalisationLocationResolveResult() {
        override val element: ParadoxLocalisationProperty? by lazy { resolveAction() }
        override val elements: Collection<ParadoxLocalisationProperty> by lazy { resolveAllAction() }
    }

    class Dynamic(
        val message: String
    ) : CwtLocalisationLocationResolveResult()
}

/**
 * 图片的位置信息的解析结果。
 *
 * @property name 图片的名字或文件路径。如果为 `null`，则表示无法解析为静态结果。
 *
 * @see CwtImageLocationExpression
 */
sealed class CwtImageLocationResolveResult : CwtLocationResolveResult<PsiElement> {
    class Static(
        override val name: String,
        val frameInfo: ImageFrameInfo? = null,
        resolveAction: () -> PsiElement? = { null },
        resolveAllAction: () -> Collection<PsiElement> = { emptySet() },
    ) : CwtImageLocationResolveResult() {
        override val element: PsiElement? by lazy { resolveAction() }
        override val elements: Collection<PsiElement> by lazy { resolveAllAction() }
    }

    class Dynamic(
        val message: String
    ) : CwtImageLocationResolveResult()
}
