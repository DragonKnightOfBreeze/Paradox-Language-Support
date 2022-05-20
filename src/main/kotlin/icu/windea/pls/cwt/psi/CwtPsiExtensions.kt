package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*
import icu.windea.pls.localisation.psi.*

val CwtOptionKey.optionKeyToken: PsiElement get() = findRequiredChild(OPTION_KEY_TOKEN)

val CwtPropertyKey.propertyKeyToken: PsiElement get() = findRequiredChild(PROPERTY_KEY_TOKEN)