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
        IOUtil.writeUTF(storage, value.gameType.id)
    }
    
    override fun read(storage: DataInput): ParadoxFilePathInfo {
        val path = IOUtil.readUTF(storage)
        val gameType = IOUtil.readUTF(storage).let { ParadoxGameType.resolve(it) }.orDefault()
        return ParadoxFilePathInfo(path, gameType)
    }
}