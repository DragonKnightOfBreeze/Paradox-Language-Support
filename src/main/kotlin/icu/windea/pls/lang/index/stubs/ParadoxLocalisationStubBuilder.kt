package icu.windea.pls.lang.index.stubs

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.stubs.*
import icu.windea.pls.model.*

class ParadoxLocalisationStubBuilder : LightStubBuilder() {
    override fun createStubForFile(file: PsiFile, tree: LighterAST): StubElement<*> {
        if (file !is ParadoxLocalisationFile) return super.createStubForFile(file, tree)
        val type = ParadoxLocalisationType.resolve(file) ?: ParadoxLocalisationType.Normal
        val gameType = selectGameType(file) ?: ParadoxGameType.placeholder()
        return ParadoxLocalisationFileStub.Impl(file, type, gameType)
    }

    override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
        return skip(node.elementType)
    }

    override fun skipChildProcessingWhenBuildingStubs(tree: LighterAST, parent: LighterASTNode, node: LighterASTNode): Boolean {
        return skip(node.tokenType)
    }

    private fun skip(elementType: IElementType): Boolean {
        return when {
            elementType == PROPERTY_LIST -> false
            elementType == PROPERTY -> false
            else -> true
        }
    }
}
