package icu.windea.pls.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

interface CwtConfig{
	val pointer: SmartPsiElementPointer<CwtProperty>?
}