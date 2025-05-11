package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.impl.*

class ParadoxScriptFileStubElementType : ILightStubFileElementType<PsiFileStub<*>>(ParadoxScriptLanguage) {
    override fun getExternalId() = "paradoxScript.FILE"

    override fun getStubVersion() = VERSION

    override fun getBuilder(): LightStubBuilder {
        return Builder()
    }

    override fun shouldBuildStubFor(file: VirtualFile): Boolean {
        try {
            //不索引内存中的文件
            if (ParadoxFileManager.isLightFile(file)) return false
            //仅索引有根目录的文件
            return file.fileInfo != null
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            return false
        }
    }

    override fun indexStub(stub: PsiFileStub<*>, sink: IndexSink) {
        //尝试在这里进行索引是没有效果的，考虑使用FileTypeIndex
        super.indexStub(stub, sink)
    }

    override fun serialize(stub: PsiFileStub<*>, dataStream: StubOutputStream) {
        if (stub is ParadoxScriptFileStub) {
            dataStream.writeName(stub.name)
            dataStream.writeName(stub.type)
            val subtypes = stub.subtypes
            if (subtypes == null) {
                dataStream.writeInt(-1)
            } else {
                dataStream.writeInt(subtypes.size)
                subtypes.forEach { subtype -> dataStream.writeName(subtype) }
            }
            dataStream.writeByte(stub.gameType.optimizeValue())
        }
        super.serialize(stub, dataStream)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): PsiFileStub<*> {
        val name = dataStream.readNameString().orEmpty()
        val type = dataStream.readNameString().orEmpty()
        val subtypesSize = dataStream.readInt()
        val subtypes = if (subtypesSize == -1) null else MutableList(subtypesSize) { dataStream.readNameString().orEmpty() }
        val gameType = dataStream.readByte().deoptimizeValue<ParadoxGameType>()
        return ParadoxScriptFileStubImpl(null, name, type, subtypes, gameType)
    }

    class Builder : LightStubBuilder() {
        override fun createStubForFile(file: PsiFile, tree: LighterAST): StubElement<*> {
            return ParadoxDefinitionManager.createStubForFile(file, tree) ?: super.createStubForFile(file, tree)
        }

        override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
            //包括：scripted_variable、property
            val type = node.elementType
            val parentType = parent.elementType
            return when {
                type == ROOT_BLOCK -> false
                type == SCRIPTED_VARIABLE -> false
                parentType == SCRIPTED_VARIABLE -> true
                type == PROPERTY -> false
                type == BLOCK -> false
                else -> true
            }
        }

        override fun skipChildProcessingWhenBuildingStubs(tree: LighterAST, parent: LighterASTNode, node: LighterASTNode): Boolean {
            //包括：scripted_variable、property
            val type = node.tokenType
            val parentType = parent.tokenType
            return when {
                type == ROOT_BLOCK -> false
                type == SCRIPTED_VARIABLE -> false
                parentType == SCRIPTED_VARIABLE -> true
                type == PROPERTY -> false
                type == BLOCK -> false
                else -> true
            }
        }
    }

    companion object {
        private const val VERSION = 62 //1.4.0

        @JvmField
        val INSTANCE = ParadoxScriptFileStubElementType()
    }
}
