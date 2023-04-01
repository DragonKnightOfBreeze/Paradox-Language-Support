package icu.windea.pls.core.index

import com.intellij.util.io.*
import icu.windea.pls.lang.model.*
import java.io.*

object ParadoxFilePathKeyDescriptor : KeyDescriptor<ParadoxFilePathInfo> {
    override fun getHashCode(value: ParadoxFilePathInfo): Int {
        return value.hashCode()
    }
    
    override fun isEqual(val1: ParadoxFilePathInfo, val2: ParadoxFilePathInfo): Boolean {
        return val1 == val2
    }
    
    override fun save(storage: DataOutput, value: ParadoxFilePathInfo) {
        IOUtil.writeUTF(storage, value.path)
        storage.writeByte(value.gameType.toByte())
    }
    
    override fun read(storage: DataInput): ParadoxFilePathInfo {
        val path = IOUtil.readUTF(storage)
        val gameType = storage.readByte().toGameType()
        return ParadoxFilePathInfo(path, gameType)
    }
    
    private fun ParadoxGameType.toByte() = this.ordinal
    
    private fun Byte.toGameType() = ParadoxGameType.values[this.toInt()]
}