package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.orNull
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.indexInfo.ParadoxDefineIndexInfo
import icu.windea.pls.model.optimizeValue
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.properties
import java.io.DataInput
import java.io.DataOutput

/**
 * 用于索引预定义的命名空间与变量。
 */
class ParadoxDefineIndex : ParadoxFileBasedIndex<Map<String, ParadoxDefineIndexInfo>>() {
    override fun getName() = ParadoxIndexKeys.Define

    override fun getVersion() = 72 // VERSION for 2.0.2

    override fun indexData(file: PsiFile, fileData: MutableMap<String, Map<String, ParadoxDefineIndexInfo>>) {
        val gameType = selectGameType(file) ?: return
        if (file !is ParadoxScriptFile) return
        file.properties().forEach f1@{ prop1 ->
            val prop1Block = prop1.propertyValue?.castOrNull<ParadoxScriptBlock>() ?: return@f1

            val namespace = prop1.name.takeIf { it.isNotEmpty() && !it.isParameterized() } ?: return@f1
            val map = fileData.getOrPut(namespace) { mutableMapOf() } as MutableMap
            val info1 = map.getOrPut("") { ParadoxDefineIndexInfo(namespace, null, sortedSetOf(), gameType) }
            (info1.elementOffsets as MutableSet) += prop1.startOffset

            prop1Block.properties().forEach f2@{ prop2 ->
                val variable = prop2.name.takeIf { it.isNotEmpty() && !it.isParameterized() } ?: return@f2
                val info2 = map.getOrPut(variable) { ParadoxDefineIndexInfo(namespace, variable, sortedSetOf(), gameType) }
                (info2.elementOffsets as MutableSet) += prop2.startOffset
            }
        }
    }

    override fun writeData(storage: DataOutput, value: Map<String, ParadoxDefineIndexInfo>) {
        storage.writeIntFast(value.size)
        value.forEach { (_, info) ->
            storage.writeUTFFast(info.namespace)
            storage.writeUTFFast(info.variable.orEmpty())
            storage.writeIntFast(info.elementOffsets.size)
            info.elementOffsets.forEach { storage.writeIntFast(it) }
            storage.writeByte(info.gameType.optimizeValue())
        }
    }

    override fun readData(storage: DataInput): Map<String, ParadoxDefineIndexInfo> {
        val map = mutableMapOf<String, ParadoxDefineIndexInfo>()
        val size = storage.readIntFast()
        repeat(size) {
            val namespace = storage.readUTFFast()
            val variable = storage.readUTFFast().orNull()
            val elementOffsetsSize = storage.readIntFast()
            val elementOffsets = if (elementOffsetsSize != 0) sortedSetOf<Int>().apply { repeat(elementOffsetsSize) { this += storage.readIntFast() } } else emptySet()
            val gameType = storage.readByte().deoptimizeValue<ParadoxGameType>()
            map.put(variable.orEmpty(), ParadoxDefineIndexInfo(namespace, variable, elementOffsets, gameType))
        }
        return map
    }

    override fun filterFile(file: VirtualFile): Boolean {
        return ParadoxDefineManager.isDefineFile(file)
    }
}
