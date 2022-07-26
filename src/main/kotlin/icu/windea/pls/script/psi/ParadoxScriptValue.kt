package icu.windea.pls.script.psi

import icu.windea.pls.model.*
import icu.windea.pls.script.psi.impl.*
import javax.swing.*

interface ParadoxScriptValue : ParadoxScriptTypedElement {
	override fun getIcon(flags: Int): Icon = ParadoxScriptPsiImplUtil.getIcon(this, flags)
	
	val value: String get() = ParadoxScriptPsiImplUtil.getValue(this)
	
	override val configExpression: String? get() = ParadoxScriptPsiImplUtil.getConfigExpression(this)
	
	override val valueType: ParadoxValueType get() = ParadoxScriptPsiImplUtil.getValueType(this)
}