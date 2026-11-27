package icu.windea.pls.localisation.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType

// org.intellij.plugins.markdown.lang.stubs.MarkdownStubBasedPsiElementBase

abstract class ParadoxLocalisationStubBasedPsiElementBase<Stub : StubElement<*>> : StubBasedPsiElementBase<Stub>, StubBasedPsiElement<Stub> {
    constructor(stub: Stub, nodeType: IElementType) : super(stub, nodeType)
    constructor(node: ASTNode) : super(node)

    final override fun getIElementType(): IElementType {
        return elementTypeImpl
    }

    @Deprecated("Deprecated in Java")
    final override fun getElementType(): IStubElementType<*, *> {
        throw UnsupportedOperationException("Use getIElementType() instead")
    }

    override fun toString(): String {
        return iElementType.toString()
    }
}
