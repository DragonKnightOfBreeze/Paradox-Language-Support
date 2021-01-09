package com.windea.plugin.idea.paradox.localisation.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.stubs.*
import com.windea.plugin.idea.paradox.localisation.*

open class ParadoxLocalisationStubElementImpl<T : StubElement<*>> : StubBasedPsiElementBase<T> {
	constructor(stub: T, nodeType: IStubElementType<*, *>?) : super(stub, nodeType!!)
	constructor(node: ASTNode) : super(node)
	
	override fun getLanguage() = ParadoxLocalisationLanguage
}
