package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import icu.windea.pls.images.ImageFrameInfo

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
