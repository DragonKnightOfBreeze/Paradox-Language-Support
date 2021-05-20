package icu.windea.pls.localisation.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.localisation.*

open class ParadoxLocalisationStubElementImpl<T : StubElement<*>> : StubBasedPsiElementBase<T> {
	constructor(stub: T, nodeType: IStubElementType<*, *>?) : super(stub, nodeType!!)
	constructor(node: ASTNode) : super(node)
	
	override fun getLanguage() = ParadoxLocalisationLanguage
}
