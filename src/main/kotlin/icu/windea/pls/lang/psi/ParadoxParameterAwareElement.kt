package icu.windea.pls.lang.psi

import com.intellij.psi.*

/**
 * 表示此PsiElement可以带有参数（[ParadoxParameter]）。
 *
 * 注意：实际上，脚本文件与本地化文件（中的本地化文本）中的任何地方都能使用参数。
 */
interface ParadoxParameterAwareElement : PsiElement
