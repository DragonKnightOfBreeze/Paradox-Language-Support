package icu.windea.pls.localisation.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType

@Suppress("UnstableApiUsage")
open class ParadoxLocalisationStubElementImpl<T : StubElement<*>> : StubBasedPsiElementBase<T> {
    constructor(stub: T, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
    constructor(stub: T, nodeType: IElementType) : super(stub, nodeType)
    constructor(node: ASTNode) : super(node)
}
