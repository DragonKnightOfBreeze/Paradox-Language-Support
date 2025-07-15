package icu.windea.pls.csv.psi

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.*
import icu.windea.pls.csv.psi.impl.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

class ParadoxCsvRowStubElementType : ILightStubElementType<ParadoxCsvRowStub, ParadoxCsvRow>("ROW", ParadoxCsvLanguage) {
    override fun getExternalId() = "paradoxCsv.ROW"

    override fun createPsi(stub: ParadoxCsvRowStub): ParadoxCsvRow {
        return ParadoxCsvRowImpl(stub, this)
    }

    override fun createStub(psi: ParadoxCsvRow, parentStub: StubElement<*>): ParadoxCsvRowStub {
        return ParadoxCsvManager.createStub(psi, parentStub) ?: createDefaultStub(parentStub)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxCsvRowStub {
        return ParadoxCsvManager.createStub(tree, node, parentStub) ?: createDefaultStub(parentStub)
    }

    private fun createDefaultStub(parentStub: StubElement<*>): ParadoxCsvRowStub {
        return ParadoxCsvRowStub.Dummy(parentStub)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        return true
    }

    override fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
        return true
    }

    override fun indexStub(stub: ParadoxCsvRowStub, sink: IndexSink) {
        //TODO 2.0.1.dev
    }

    override fun serialize(stub: ParadoxCsvRowStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        dataStream.writeByte(stub.gameType.optimizeValue())
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxCsvRowStub {
        val name = dataStream.readNameString().orEmpty()
        val gameType = dataStream.readByte().deoptimizeValue<ParadoxGameType>()
        return ParadoxCsvRowStub.Impl(parentStub, name, gameType)
    }

    override fun isAlwaysLeaf(root: StubBase<*>): Boolean {
        return true
    }

    companion object {
        @JvmField
        val INSTANCE = ParadoxCsvRowStubElementType()
    }
}
