@file:Suppress("unused")

package icu.windea.pls.core.select

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType

fun <T : PsiElement> Sequence<T>.oneBy(elementType: IElementType?): T? {
    return find { elementType == null || it.elementType == elementType }
}

fun <T : PsiElement> Sequence<T>.oneBy(tokenSet: TokenSet?): T? {
    return find { tokenSet == null || it.elementType in tokenSet }
}

fun <T : PsiElement> Sequence<T>.listBy(elementType: IElementType?): List<T> {
    return filter { elementType == null || it.elementType == elementType }.toList()
}

fun <T : PsiElement> Sequence<T>.listBy(tokenSet: TokenSet?): List<T> {
    return filter { tokenSet == null || it.elementType in tokenSet }.toList()
}
