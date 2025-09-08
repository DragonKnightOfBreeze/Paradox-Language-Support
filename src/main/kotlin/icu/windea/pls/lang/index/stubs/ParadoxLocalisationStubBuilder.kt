package icu.windea.pls.lang.index.stubs

import com.intellij.lang.ASTNode
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.LightStubBuilder
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY_LIST
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationFileStub
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLocalisationType

class ParadoxLocalisationStubBuilder : LightStubBuilder() {
    override fun createStubForFile(file: PsiFile, tree: LighterAST): StubElement<*> {
        if (file !is ParadoxLocalisationFile) return super.createStubForFile(file, tree)
        val type = ParadoxLocalisationType.resolve(file) ?: ParadoxLocalisationType.Normal
        val gameType = selectGameType(file) ?: ParadoxGameType.Core
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
