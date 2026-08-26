package icu.windea.pls.core.psi

import com.intellij.psi.PsiElement

/**
 * 作为文件根的（没有左右边界的）块。
 *
 * 通常来说，普通节点、注释、空白等都会直接位于这之中，而非直接位于文件中。
 */
interface PsiRootBlock: PsiElement
