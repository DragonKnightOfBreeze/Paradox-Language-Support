package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptFileStubElementType : IStubFileElementType<PsiFileStub<*>>(ParadoxScriptLanguage) {
	private const val externalId = "paradoxScript.file"
	private const val stubVersion = 6 //0.7.7
	
	override fun getExternalId() = externalId
	
	override fun getStubVersion() = stubVersion
	
	override fun getBuilder(): StubBuilder {
		return Builder()
	}
	
	override fun shouldBuildStubFor(file: VirtualFile?): Boolean {
		//仅为合法的paradox文件创建索引
		try {
			return file?.fileInfo?.rootType != null
		} catch(e: Exception) {
			return false
		}
	}
	
	override fun indexStub(stub: PsiFileStub<*>, sink: IndexSink) {
		if(stub is ParadoxScriptFileStub) {
			stub.name?.takeIfNotEmpty()?.let { name -> sink.occurrence(ParadoxDefinitionNameIndex.key, name) }
			stub.type?.takeIfNotEmpty()?.let { type -> sink.occurrence(ParadoxDefinitionTypeIndex.key, type) }
		}
		super.indexStub(stub, sink)
	}
	
	override fun serialize(stub: PsiFileStub<*>, dataStream: StubOutputStream) {
		if(stub is ParadoxScriptFileStub) {
			dataStream.writeName(stub.name)
			dataStream.writeName(stub.type)
			dataStream.writeName(stub.subtypes?.toCommaDelimitedString())
			dataStream.writeName(stub.gameType?.id)
		}
		super.serialize(stub, dataStream)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): PsiFileStub<*> {
		val name = dataStream.readNameString()
		val type = dataStream.readNameString()
		val subtypes = dataStream.readNameString()?.toCommaDelimitedStringList()
		val gameType = dataStream.readNameString()?.let { ParadoxGameType.resolve(it) }
		return ParadoxScriptFileStubImpl(null, name, type, subtypes, gameType)
	}
	
	class Builder : DefaultStubBuilder() {
		override fun createStubForFile(file: PsiFile): StubElement<*> {
			val psiFile = file as? ParadoxScriptFile ?: return super.createStubForFile(file)
			val definitionInfo = psiFile.definitionInfo?.takeIf { it.shouldIndex }
			val name = definitionInfo?.name
			val type = definitionInfo?.type
			val subtypes = definitionInfo?.subtypes
			val gameType = definitionInfo?.gameType
			return ParadoxScriptFileStubImpl(psiFile, name, type, subtypes, gameType)
		}
		
		override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
			//需要包括scripted_variable, property, key/string (作为：complexEnum, valueSetValue)
			val elementType = node.elementType
			return when {
				elementType == PARAMETER || elementType == PARAMETER_CONDITION -> true
				elementType == INLINE_MATH || elementType == INLINE_MATH_PARAMETER -> true
				elementType == BOOLEAN || elementType == INT || elementType == FLOAT || elementType == COLOR -> true
				else -> false
			}
		}
	}
	
	override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode? {
		//这里需要基于上下文来解析本地化文本的语法
		val fileInfo = psi.fileInfo
		val project = psi.project
		val language = ParadoxScriptLanguage
		val context = ParadoxScriptParsingContext(project, fileInfo)
		val lexer = ParadoxScriptLexerAdapter(context)
		val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, language, chameleon.chars)
		val parser = ParadoxScriptParser()
		val node = parser.parse(this, builder)
		return node.firstChildNode
	}
}
