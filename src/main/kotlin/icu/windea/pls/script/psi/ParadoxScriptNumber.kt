// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.impl.*

interface ParadoxScriptNumber : ParadoxScriptValue, PsiLiteralValue {
	override val valueType: ParadoxValueType get() = ParadoxScriptPsiImplUtil.getValueType(this)
}