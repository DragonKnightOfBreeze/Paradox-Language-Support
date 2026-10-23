package icu.windea.pls.core.psi

import com.intellij.psi.PsiElement

/**
 * 作为文件根的（没有左右边界的）块。
 *
 * 通常来说，普通节点、注释、空白等都会直接位于其中，而非直接位于作为其父节点的文件中。
 */
interface PsiRootBlock : PsiElement
