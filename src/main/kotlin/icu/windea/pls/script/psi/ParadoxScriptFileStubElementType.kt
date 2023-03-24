package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptFileStubElementType : IStubFileElementType<PsiFileStub<*>>(ParadoxScriptLanguage) {
    private const val externalId = "paradoxScript.file"
    private const val stubVersion = 9 //0.9.2
    
    override fun getExternalId() = externalId
    
    override fun getStubVersion() = stubVersion
    
    override fun getBuilder(): DefaultStubBuilder {
        return Builder()
    }
    
    override fun shouldBuildStubFor(file: VirtualFile): Boolean {
        return ParadoxCoreHandler.shouldIndexFile(file)
    }
    
    override fun indexStub(stub: PsiFileStub<*>, sink: IndexSink) {
        if(stub is ParadoxScriptFileStub) {
            stub.name?.takeIfNotEmpty()?.let { name -> sink.occurrence(ParadoxDefinitionNameIndex.KEY, name) }
            stub.type?.takeIfNotEmpty()?.let { type -> sink.occurrence(ParadoxDefinitionTypeIndex.KEY, type) }
        }
        super.indexStub(stub, sink)
    }
    
    override fun serialize(stub: PsiFileStub<*>, dataStream: StubOutputStream) {
        if(stub is ParadoxScriptFileStub) {
            dataStream.writeName(stub.name)
            dataStream.writeName(stub.type)
            dataStream.writeName(stub.gameType?.id)
        }
        super.serialize(stub, dataStream)
    }
    
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): PsiFileStub<*> {
        val name = dataStream.readNameString()
        val type = dataStream.readNameString()
        val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
        return ParadoxScriptFileStubImpl(null, name, type, gameType)
    }
    
    class Builder : DefaultStubBuilder() {
        override fun createStubForFile(file: PsiFile): StubElement<*> {
            val psiFile = file as? ParadoxScriptFile ?: return super.createStubForFile(file)
            val definitionInfo = psiFile.definitionInfo?.takeIf { it.isGlobal }
            val name = definitionInfo?.name
            val type = definitionInfo?.type
            val gameType = definitionInfo?.gameType
            return ParadoxScriptFileStubImpl(psiFile, name, type, gameType)
        }
        
        //override fun createStubForFile(file: PsiFile, tree: LighterAST): StubElement<*> {
        //    val psiFile = file as? ParadoxScriptFile ?: return super.createStubForFile(file, tree)
        //    val definitionInfo = psiFile.definitionInfo?.takeIf { it.isGlobal }
        //    val name = definitionInfo?.name
        //    val type = definitionInfo?.type
        //    val gameType = definitionInfo?.gameType
        //    return ParadoxScriptFileStubImpl(psiFile, name, type, gameType)
        //}
        
        override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
            //需要包括scripted_variable, property, key/string (作为：complexEnum, valueSetValue)
            val type = node.elementType
            return when {
                type == PARAMETER || type == PARAMETER_CONDITION -> true
                type == INLINE_MATH || type == INLINE_MATH_PARAMETER -> true
                type == BOOLEAN || type == INT || type == FLOAT || type == COLOR -> true
                else -> false
            }
        }
        
        //override fun skipChildProcessingWhenBuildingStubs(tree: LighterAST, parent: LighterASTNode, node: LighterASTNode): Boolean {
        //    //需要包括scripted_variable, property, key/string (作为：complexEnum, valueSetValue)
        //    val type = node.tokenType
        //    return when {
        //        type == PARAMETER || type == PARAMETER_CONDITION -> true
        //        type == INLINE_MATH || type == INLINE_MATH_PARAMETER -> true
        //        type == BOOLEAN || type == INT || type == FLOAT || type == COLOR -> true
        //        else -> false
        //    }
        //}
    }
    
    //override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode? {
    //	val fileInfo = psi.fileInfo
    //	val project = psi.project
    //	val language = ParadoxScriptLanguage
    //	val context = ParadoxScriptParsingContext(project, fileInfo)
    //	val lexer = ParadoxScriptLexerAdapter(context)
    //	val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, language, chameleon.chars)
    //	val parser = ParadoxScriptParser()
    //	val node = parser.parse(this, builder)
    //	return node.firstChildNode
    //}
}
