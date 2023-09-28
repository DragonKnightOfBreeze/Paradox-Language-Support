package icu.windea.pls.lang.expressionIndex.impl

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.model.expression.*
import java.io.*

//private val compressComparator = compareBy<ParadoxParameterInfo>({ it.contextKey }, { it.name })
private val compressComparator = compareBy<ParadoxParameterInfo>{ it.contextKey }

class ParadoxParameterIndexSupport : ParadoxExpressionIndexSupport<ParadoxParameterInfo> {
    override fun id() = ParadoxExpressionIndexId.Parameter.id
    
    override fun type() = ParadoxParameterInfo::class.java
    
    override fun indexElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        val constraint = ParadoxResolveConstraint.Parameter
        if(!constraint.canResolveReference(element)) return
        element.references.forEachFast f@{ reference ->
            if(!constraint.canResolve(reference)) return@f
            val resolved = reference.resolve()
            if(resolved !is ParadoxParameterElement) return@f
            //note that element.startOffset may not equal to actual parameterElement.startOffset (e.g. in a script value expression)
            val info = ParadoxParameterInfo(resolved.name, resolved.contextKey, resolved.readWriteAccess, element.startOffset, resolved.gameType)
            addToFileData(info, fileData)
        }
    }
    
    override fun compress(value: List<ParadoxParameterInfo>): List<ParadoxParameterInfo> {
        return value.sortedWith(compressComparator)
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxParameterInfo, previousInfo: ParadoxParameterInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.contextKey }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.toByte())
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxParameterInfo?, gameType: ParadoxGameType): ParadoxParameterInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val contextKey = storage.readOrReadFrom(previousInfo, { it.contextKey }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().toReadWriteAccess()
        val elementOffset = storage.readIntFast()
        return ParadoxParameterInfo(name, contextKey, readWriteAccess, elementOffset, gameType)
    }
}