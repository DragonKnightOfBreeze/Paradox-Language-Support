package icu.windea.pls.lang.index.stubs

import com.intellij.lang.ASTNode
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.LightStubBuilder
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.BLOCK
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.ROOT_BLOCK
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SCRIPTED_VARIABLE
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.stubs.ParadoxScriptFileStub

class ParadoxScriptStubBuilder : LightStubBuilder() {
    override fun createStubForFile(file: PsiFile, tree: LighterAST): StubElement<*> {
        if (file !is ParadoxScriptFile) return super.createStubForFile(file, tree)
        val gameType = selectGameType(file) ?: ParadoxGameType.placeholder()
        return ParadoxScriptFileStub.Impl(file, gameType)
    }

    override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
        return skip(node.elementType)
    }

    override fun skipChildProcessingWhenBuildingStubs(tree: LighterAST, parent: LighterASTNode, node: LighterASTNode): Boolean {
        return skip(node.tokenType)
    }

    private fun skip(elementType: IElementType): Boolean {
        return when {
            elementType == ROOT_BLOCK -> false
            elementType == BLOCK -> false
            elementType == SCRIPTED_VARIABLE -> false
            elementType == PROPERTY -> false
            else -> true
        }
    }
}
