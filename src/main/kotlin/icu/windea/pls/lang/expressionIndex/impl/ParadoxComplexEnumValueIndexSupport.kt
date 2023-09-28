package icu.windea.pls.lang.expressionIndex.impl

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.script.psi.*
import java.io.*

private val compressComparator = compareBy<ParadoxComplexEnumValueInfo>({ it.enumName }, { it.name })

class ParadoxComplexEnumValueIndexSupport : ParadoxExpressionIndexSupport<ParadoxComplexEnumValueInfo> {
    override fun id() = ParadoxExpressionIndexIds.ComplexEnumValue
    
    override fun type() = ParadoxComplexEnumValueInfo::class.java
    
    override fun indexElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        if(!element.isExpression()) return
        val info = ParadoxComplexEnumValueHandler.getInfo(element) ?: return
        addToFileData(info, fileData)
    }
    
    override fun compress(value: List<ParadoxComplexEnumValueInfo>): List<ParadoxComplexEnumValueInfo> {
        return value.sortedWith(compressComparator)
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxComplexEnumValueInfo, previousInfo: ParadoxComplexEnumValueInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.enumName }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.toByte())
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxComplexEnumValueInfo?, gameType: ParadoxGameType): ParadoxComplexEnumValueInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val enumName = storage.readOrReadFrom(previousInfo, { it.enumName }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().toReadWriteAccess()
        val elementOffset = storage.readIntFast()
        return ParadoxComplexEnumValueInfo(name, enumName, readWriteAccess, elementOffset, gameType)
    }
}