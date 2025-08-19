package icu.windea.pls.localisation.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*

@Suppress("UnstableApiUsage")
open class ParadoxLocalisationStubElementImpl<T : StubElement<*>> : StubBasedPsiElementBase<T> {
    constructor(stub: T, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
    constructor(stub: T, nodeType: IElementType) : super(stub, nodeType)
    constructor(node: ASTNode) : super(node)
}
