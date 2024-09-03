package icu.windea.pls.cwt.psi

import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.model.*

/**
 * @property type 类型。基于PSI的类型。
 * @property configType 规则类型。
 */
interface CwtTypedElement : PsiElement {
    val type: CwtType? get() = null
    val configType: CwtConfigType? get() = null
}
