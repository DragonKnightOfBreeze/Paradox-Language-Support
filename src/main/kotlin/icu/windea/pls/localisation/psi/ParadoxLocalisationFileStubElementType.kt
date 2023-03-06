package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

object ParadoxLocalisationFileStubElementType : IStubFileElementType<PsiFileStub<*>>(ParadoxLocalisationLanguage) {
    private const val externalId = "paradoxLocalisation.file"
    private const val stubVersion = 8 //0.7.13
    
    override fun getExternalId() = externalId
    
    override fun getStubVersion() = stubVersion
    
    override fun getBuilder(): StubBuilder {
        return Builder()
    }
    
    override fun shouldBuildStubFor(file: VirtualFile): Boolean {
        return ParadoxCoreHandler.shouldIndexFile(file)
    }
    
    class Builder : DefaultStubBuilder() {
        override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
            //仅包括propertyList和property
            return when {
                node.elementType == LOCALE -> true
                node.elementType == PROPERTY_LIST -> false
                node.elementType == PROPERTY -> false
                parent.elementType == PROPERTY -> true
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
