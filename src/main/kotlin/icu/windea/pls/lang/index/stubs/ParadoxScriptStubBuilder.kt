package icu.windea.pls.lang.index.stubs

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.stubs.*

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
