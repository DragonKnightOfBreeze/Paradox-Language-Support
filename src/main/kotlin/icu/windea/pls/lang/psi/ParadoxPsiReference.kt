package icu.windea.pls.lang.psi

import com.intellij.psi.*

interface ParadoxPsiReference : PsiReference {
    /**
     * 这个方法用来判断某个引用是否可以解析，但是最终需要的解析结果不一定是这个方法得到的解析结果。
     */
    fun resolveFast(): PsiElement?
}