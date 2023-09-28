package icu.windea.pls.lang.expressionIndex.impl

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.model.expression.*
import java.io.*

//private val compressComparator = compareBy<ParadoxLocalisationParameterInfo>({ it.localisationName }, { it.name })
private val compressComparator = compareBy<ParadoxLocalisationParameterInfo> { it.localisationName }

class ParadoxLocalisationParameterIndexSupport : ParadoxExpressionIndexSupport<ParadoxLocalisationParameterInfo> {
    override fun id() = ParadoxExpressionIndexIds.LocalisationParameter
    
    override fun type() = ParadoxLocalisationParameterInfo::class.java
    
    override fun indexElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        val constraint = ParadoxResolveConstraint.LocalisationParameter
        if(!constraint.canResolveReference(element)) return
        element.references.forEachFast f@{ reference ->
            if(!constraint.canResolve(reference)) return@f
            val resolved = reference.resolve()
            if(resolved !is ParadoxLocalisationParameterElement) return@f
            val info = ParadoxLocalisationParameterInfo(resolved.name, resolved.localisationName, element.startOffset, resolved.gameType)
            addToFileData(info, fileData)
        }
    }
    
    override fun compress(value: List<ParadoxLocalisationParameterInfo>): List<ParadoxLocalisationParameterInfo> {
        return value.sortedWith(compressComparator)
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxLocalisationParameterInfo, previousInfo: ParadoxLocalisationParameterInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.localisationName }, { storage.writeUTFFast(it) })
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxLocalisationParameterInfo?, gameType: ParadoxGameType): ParadoxLocalisationParameterInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val localisationName = storage.readOrReadFrom(previousInfo, { it.localisationName }, { storage.readUTFFast() })
        val elementOffset = storage.readIntFast()
        return ParadoxLocalisationParameterInfo(name, localisationName, elementOffset, gameType)
    }
}