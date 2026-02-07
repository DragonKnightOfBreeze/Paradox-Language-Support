package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.orNull
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.model.forGameType
import icu.windea.pls.model.index.ParadoxDefineIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.propertyValue
import java.io.DataInput
import java.io.DataOutput

/**
 * 定值的命名空间和变量的索引。
 *
 * @see ParadoxDefineIndexInfo
 */
class ParadoxDefineIndex : IndexInfoAwareFileBasedIndex<Map<String, ParadoxDefineIndexInfo>>() {
    override fun getName() = PlsIndexKeys.Define

    override fun getVersion() = PlsIndexVersions.Define

    override fun filterFile(file: VirtualFile): Boolean {
        return ParadoxDefineManager.isDefineFile(file)
    }

    override fun indexData(psiFile: PsiFile): Map<String, Map<String, ParadoxDefineIndexInfo>> {
        return buildMap { buildData(psiFile, this) }
    }

    private fun buildData(psiFile: PsiFile, fileData: MutableMap<String, Map<String, ParadoxDefineIndexInfo>>) {
        if (psiFile !is ParadoxScriptFile) return
        val gameType = selectGameType(psiFile) ?: return
        psiFile.properties().forEach f1@{ prop1 ->
            val prop1Block = prop1.propertyValue<ParadoxScriptBlock>() ?: return@f1

            val namespace = prop1.name.takeIf { it.isNotEmpty() && !it.isParameterized() } ?: return@f1
            val map = fileData.getOrPut(namespace) { mutableMapOf() } as MutableMap
            map.putIfAbsent("", ParadoxDefineIndexInfo(namespace, null, prop1.startOffset, gameType))

            prop1Block.properties().forEach f2@{ prop2 ->
                val variable = prop2.name.takeIf { it.isNotEmpty() && !it.isParameterized() } ?: return@f2
                map.putIfAbsent(variable, ParadoxDefineIndexInfo(namespace, variable, prop2.startOffset, gameType))
            }
        }
    }

    override fun saveValue(storage: DataOutput, value: Map<String, ParadoxDefineIndexInfo>) {
        storage.writeIntFast(value.size)
        value.forEach { (_, info) ->
            storage.writeUTFFast(info.namespace)
            storage.writeUTFFast(info.variable.orEmpty())
            storage.writeIntFast(info.elementOffset)
            storage.writeByte(info.gameType.optimized(OptimizerRegistry.forGameType()))
        }
    }

    override fun readValue(storage: DataInput): Map<String, ParadoxDefineIndexInfo> {
        val map = mutableMapOf<String, ParadoxDefineIndexInfo>()
        val size = storage.readIntFast()
        repeat(size) {
            val namespace = storage.readUTFFast()
            val variable = storage.readUTFFast().orNull()
            val elementOffset = storage.readIntFast()
            val gameType = storage.readByte().deoptimized(OptimizerRegistry.forGameType())
            map.put(variable.orEmpty(), ParadoxDefineIndexInfo(namespace, variable, elementOffset, gameType))
        }
        return map
    }
}
