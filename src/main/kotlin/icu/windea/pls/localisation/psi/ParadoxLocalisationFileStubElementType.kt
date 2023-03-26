package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

object ParadoxLocalisationFileStubElementType : ILightStubFileElementType<PsiFileStub<*>>(ParadoxLocalisationLanguage) {
    private const val externalId = "paradoxLocalisation.file"
    private const val stubVersion = 11 //0.9.3
    
    override fun getExternalId() = externalId
    
    override fun getStubVersion() = stubVersion
    
    override fun getBuilder(): LightStubBuilder {
        return Builder()
    }
    
    override fun shouldBuildStubFor(file: VirtualFile): Boolean {
        return ParadoxCoreHandler.shouldIndexFile(file)
    }
    
    class Builder : LightStubBuilder() {
        override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
            //仅包括propertyList和property
            val type = node.elementType
            return when {
                type == LOCALE -> true
                type == PROPERTY_LIST -> false
                type == PROPERTY -> false
                parent.elementType == PROPERTY -> true
                else -> true
            }
        }
        
        override fun skipChildProcessingWhenBuildingStubs(tree: LighterAST, parent: LighterASTNode, node: LighterASTNode): Boolean {
            //仅包括propertyList和property
            val type = node.tokenType
            return when {
                type == LOCALE -> true
                type == PROPERTY_LIST -> false
                type == PROPERTY -> false
                parent.tokenType == PROPERTY -> true
                else -> true
            }
        }
    }
    
    //override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode? {
    //	val fileInfo = psi.fileInfo
    //	val project = psi.project
    //	val language = ParadoxLocalisationLanguage
    //	val context = ParadoxLocalisationParsingContext(project, fileInfo)
    //	val lexer = ParadoxLocalisationLexerAdapter(context)
    //	val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, language, chameleon.chars)
    //	val parser = ParadoxLocalisationParser()
    //	val node = parser.parse(this, builder)
    //	return node.firstChildNode
    //}
}
