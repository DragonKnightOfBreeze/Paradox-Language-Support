package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.psi.*
import java.io.*

/**
 * 用于索引预定义的命名空间与变量。
 */
class ParadoxDefineIndex : ParadoxFileBasedIndex<Map<String, ParadoxDefineIndexInfo.Compact>>() {
    companion object {
        private const val VERSION = 70 //2.0.0-dev
    }

    override fun getName() = ParadoxIndexManager.DefineName

    override fun getVersion() = VERSION

    override fun indexData(file: PsiFile, fileData: MutableMap<String, Map<String, ParadoxDefineIndexInfo.Compact>>) {
        val gameType = selectGameType(file) ?: return
        file.castOrNull<ParadoxScriptFile>()?.processProperty(conditional = false, inline = false) p1@{ prop1 ->
            val prop1Block = prop1.propertyValue?.castOrNull<ParadoxScriptBlock>() ?: return@p1 true

            val namespace = prop1.name.takeIf { it.isNotEmpty() && !it.isParameterized() } ?: return@p1 true
            val map = fileData.getOrPut(namespace) { mutableMapOf() } as MutableMap
            val info1 = map.getOrPut("") { ParadoxDefineIndexInfo.Compact(namespace, null, sortedSetOf(), gameType) }
            (info1.elementOffsets as MutableSet) += prop1.startOffset

            prop1Block.processProperty(conditional = false, inline = false) p2@{ prop2 ->
                val variable = prop2.name.takeIf { it.isNotEmpty() && !it.isParameterized() } ?: return@p2 true
                val info2 = map.getOrPut(variable) { ParadoxDefineIndexInfo.Compact(namespace, variable, sortedSetOf(), gameType) }
                (info2.elementOffsets as MutableSet) += prop2.startOffset
                true
            }
        }
    }

    override fun writeData(storage: DataOutput, value: Map<String, ParadoxDefineIndexInfo.Compact>) {
        storage.writeIntFast(value.size)
        value.forEach { (_, info) ->
            storage.writeUTFFast(info.namespace)
            storage.writeUTFFast(info.variable.orEmpty())
            storage.writeIntFast(info.elementOffsets.size)
            info.elementOffsets.forEach { storage.writeIntFast(it) }
            storage.writeByte(info.gameType.optimizeValue())
        }
    }

    override fun readData(storage: DataInput): Map<String, ParadoxDefineIndexInfo.Compact> {
        val map = mutableMapOf<String, ParadoxDefineIndexInfo.Compact>()
        val size = storage.readIntFast()
        repeat(size) {
            val namespace = storage.readUTFFast()
            val variable = storage.readUTFFast().orNull()
            val elementOffsetsSize = storage.readIntFast()
            val elementOffsets = if (elementOffsetsSize != 0) sortedSetOf<Int>().apply { repeat(elementOffsetsSize) { this += storage.readIntFast() } } else emptySet()
            val gameType = storage.readByte().deoptimizeValue<ParadoxGameType>()
            map.put(variable.orEmpty(), ParadoxDefineIndexInfo.Compact(namespace, variable, elementOffsets, gameType))
        }
        return map
    }

    override fun filterFile(file: VirtualFile): Boolean {
        return ParadoxDefineManager.isDefineFile(file)
    }
}
