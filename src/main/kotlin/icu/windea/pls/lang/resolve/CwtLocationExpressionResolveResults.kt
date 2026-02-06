package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import icu.windea.pls.config.configExpression.CwtImageLocationExpression
import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * @see CwtLocalisationLocationExpression
 */
class CwtLocalisationLocationResolveResult(
    val name: String,
    val message: String? = null,
    resolveAction: () -> ParadoxLocalisationProperty? = { null },
    resolveAllAction: () -> Collection<ParadoxLocalisationProperty> = { emptySet() },
) {
    val element: ParadoxLocalisationProperty? by lazy { resolveAction() }
    val elements: Collection<ParadoxLocalisationProperty> by lazy { resolveAllAction() }
}

/**
 * @see CwtImageLocationExpression
 */
class CwtImageLocationResolveResult(
    val nameOrFilePath: String,
    val frameInfo: ImageFrameInfo? = null,
    val message: String? = null,
    resolveAction: () -> PsiElement? = { null },
    resolveAllAction: () -> Collection<PsiElement> = { emptySet() },
) {
    val element: PsiElement? by lazy { resolveAction() }
    val elements: Collection<PsiElement> by lazy { resolveAllAction() }
}
