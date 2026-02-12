package icu.windea.pls.lang.index

import com.intellij.psi.stubs.AbstractStubIndex
import com.intellij.util.io.KeyDescriptor
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.model.index.ParadoxDefineVariableKey
import icu.windea.pls.script.psi.ParadoxScriptProperty
import java.io.DataInput
import java.io.DataOutput

/**
 * 定值变量的索引。基于命名空间和变量名。
 */
class ParadoxDefineVariableIndex : AbstractStubIndex<ParadoxDefineVariableKey, ParadoxScriptProperty>() {
    // com.intellij.util.io.EnumeratorStringDescriptor
    private val keyDescriptor = object : KeyDescriptor<ParadoxDefineVariableKey> {
        override fun isEqual(val1: ParadoxDefineVariableKey, val2: ParadoxDefineVariableKey) = val1 == val2

        override fun getHashCode(value: ParadoxDefineVariableKey) = value.hashCode()

        override fun save(storage: DataOutput, value: ParadoxDefineVariableKey) {
            storage.writeUTFFast(value.namespace)
            storage.writeUTFFast(value.variable)
        }

        override fun read(storage: DataInput): ParadoxDefineVariableKey {
            val namespace = storage.readUTFFast()
            val variable = storage.readUTFFast()
            return ParadoxDefineVariableKey(namespace, variable)
        }
    }

    override fun getKey() = PlsIndexKeys.DefineVariable

    override fun getVersion() = PlsIndexVersions.ScriptStub

    override fun getKeyDescriptor() = keyDescriptor
}
