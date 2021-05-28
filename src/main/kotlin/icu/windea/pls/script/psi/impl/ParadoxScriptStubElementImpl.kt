package icu.windea.pls.script.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.script.*

open class ParadoxScriptStubElementImpl<T : StubElement<*>> : StubBasedPsiElementBase<T> {
	constructor(stub: T, nodeType: IStubElementType<*, *>?) : super(stub, nodeType!!) {}
	constructor(node: ASTNode) : super(node) {}
	override fun getLanguage() = ParadoxScriptLanguage
}
