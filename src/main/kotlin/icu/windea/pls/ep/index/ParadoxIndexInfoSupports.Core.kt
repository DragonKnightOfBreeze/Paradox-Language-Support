package icu.windea.pls.ep.index

import com.intellij.codeInsight.highlighting.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.psi.*
import java.io.*

class ParadoxComplexEnumValueIndexInfoSupport : ParadoxIndexInfoSupport<ParadoxComplexEnumValueIndexInfo> {
    private val compressComparator = compareBy<ParadoxComplexEnumValueIndexInfo>({ it.enumName }, { it.name })

    override val id = ParadoxIndexInfoType.ComplexEnumValue.id

    override val type  = ParadoxComplexEnumValueIndexInfo::class.java

    override fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        if (element !is ParadoxScriptStringExpressionElement) return
        if (!element.isExpression()) return
        val info = ParadoxComplexEnumValueManager.getInfo(element) ?: return
        addToFileData(info, fileData)
    }

    override fun compressData(value: List<ParadoxComplexEnumValueIndexInfo>): List<ParadoxComplexEnumValueIndexInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxComplexEnumValueIndexInfo, previousInfo: ParadoxComplexEnumValueIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.enumName }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.optimizeValue())
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxComplexEnumValueIndexInfo?, gameType: ParadoxGameType): ParadoxComplexEnumValueIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val enumName = storage.readOrReadFrom(previousInfo, { it.enumName }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().deoptimizeValue<ReadWriteAccessDetector.Access>()
        val elementOffset = storage.readIntFast()
        return ParadoxComplexEnumValueIndexInfo(name, enumName, readWriteAccess, elementOffset, gameType)
    }
}

class ParadoxDynamicValueIndexInfoSupport : ParadoxIndexInfoSupport<ParadoxDynamicValueIndexInfo> {
    private val compressComparator = compareBy<ParadoxDynamicValueIndexInfo>({ it.dynamicValueType }, { it.name })

    override val id = ParadoxIndexInfoType.DynamicValue.id

    override val type = ParadoxDynamicValueIndexInfo::class.java

    override fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        val constraint = ParadoxResolveConstraint.DynamicValue
        if (!constraint.canResolveReference(element)) return
        //use expression references only for expression elements to optimize indexing performance
        val references = when {
            element is ParadoxExpressionElement -> ParadoxExpressionManager.getExpressionReferences(element)
            else -> element.references
        }
        references.forEach f@{ reference ->
            if (!constraint.canResolve(reference)) return@f
            val resolved = reference.resolve()
            if (resolved !is ParadoxDynamicValueElement) return@f
            resolved.dynamicValueTypes.forEach { dynamicValueType ->
                val info = ParadoxDynamicValueIndexInfo(resolved.name, dynamicValueType, resolved.readWriteAccess, resolved.parent.startOffset, resolved.gameType)
                addToFileData(info, fileData)
            }
        }
    }

    override fun indexLocalisationExpression(element: ParadoxLocalisationExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        val constraint = ParadoxResolveConstraint.DynamicValue
        if (!constraint.canResolveReference(element)) return
        //use expression references only for expression elements to optimize indexing performance
        val references = ParadoxExpressionManager.getExpressionReferences(element)
        references.forEach f@{ reference ->
            if (!constraint.canResolve(reference)) return@f
            val resolved = reference.resolve()
            if (resolved !is ParadoxDynamicValueElement) return@f
            resolved.dynamicValueTypes.forEach { dynamicValueType ->
                val info = ParadoxDynamicValueIndexInfo(resolved.name, dynamicValueType, resolved.readWriteAccess, resolved.parent.startOffset, resolved.gameType)
                addToFileData(info, fileData)
            }
        }
    }

    override fun compressData(value: List<ParadoxDynamicValueIndexInfo>): List<ParadoxDynamicValueIndexInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxDynamicValueIndexInfo, previousInfo: ParadoxDynamicValueIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.dynamicValueType }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.optimizeValue())
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxDynamicValueIndexInfo?, gameType: ParadoxGameType): ParadoxDynamicValueIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val dynamicValueType = storage.readOrReadFrom(previousInfo, { it.dynamicValueType }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().deoptimizeValue<ReadWriteAccessDetector.Access>()
        val elementOffset = storage.readIntFast()
        return ParadoxDynamicValueIndexInfo(name, dynamicValueType, readWriteAccess, elementOffset, gameType)
    }
}

class ParadoxParameterIndexInfoSupport : ParadoxIndexInfoSupport<ParadoxParameterIndexInfo> {
    private val compressComparator = compareBy<ParadoxParameterIndexInfo>({ it.contextKey }, { it.name })

    override val id = ParadoxIndexInfoType.Parameter.id

    override val type = ParadoxParameterIndexInfo::class.java

    override fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        val constraint = ParadoxResolveConstraint.Parameter
        if (!constraint.canResolveReference(element)) return
        //use expression references only for expression elements to optimize indexing performance
        val references = when {
            element is ParadoxExpressionElement -> ParadoxExpressionManager.getExpressionReferences(element)
            else -> element.references
        }
        references.forEach f@{ reference ->
            if (!constraint.canResolve(reference)) return@f
            val resolved = reference.resolve()
            if (resolved !is ParadoxParameterElement) return@f
            //note that element.startOffset may not equal to actual parameterElement.startOffset (e.g. in a script value expression)
            val info = ParadoxParameterIndexInfo(resolved.name, resolved.contextKey, resolved.readWriteAccess, element.startOffset, resolved.gameType)
            addToFileData(info, fileData)
        }
    }

    override fun compressData(value: List<ParadoxParameterIndexInfo>): List<ParadoxParameterIndexInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxParameterIndexInfo, previousInfo: ParadoxParameterIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.contextKey }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.optimizeValue())
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxParameterIndexInfo?, gameType: ParadoxGameType): ParadoxParameterIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val contextKey = storage.readOrReadFrom(previousInfo, { it.contextKey }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().deoptimizeValue<ReadWriteAccessDetector.Access>()
        val elementOffset = storage.readIntFast()
        return ParadoxParameterIndexInfo(name, contextKey, readWriteAccess, elementOffset, gameType)
    }
}

class ParadoxLocalisationParameterIndexInfoSupport : ParadoxIndexInfoSupport<ParadoxLocalisationParameterIndexInfo> {
    private val compressComparator = compareBy<ParadoxLocalisationParameterIndexInfo>({ it.localisationName }, { it.name })

    override val id = ParadoxIndexInfoType.LocalisationParameter.id

    override val type = ParadoxLocalisationParameterIndexInfo::class.java

    override fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        val constraint = ParadoxResolveConstraint.LocalisationParameter
        if (!constraint.canResolveReference(element)) return
        //use expression references only for expression elements to optimize indexing performance
        val references = when {
            element is ParadoxExpressionElement -> ParadoxExpressionManager.getExpressionReferences(element)
            else -> element.references
        }
        references.forEach f@{ reference ->
            if (!constraint.canResolve(reference)) return@f
            val resolved = reference.resolve()
            if (resolved !is ParadoxLocalisationParameterElement) return@f
            val info = ParadoxLocalisationParameterIndexInfo(resolved.name, resolved.localisationName, element.startOffset, resolved.gameType)
            addToFileData(info, fileData)
        }
    }

    override fun compressData(value: List<ParadoxLocalisationParameterIndexInfo>): List<ParadoxLocalisationParameterIndexInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxLocalisationParameterIndexInfo, previousInfo: ParadoxLocalisationParameterIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.localisationName }, { storage.writeUTFFast(it) })
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxLocalisationParameterIndexInfo?, gameType: ParadoxGameType): ParadoxLocalisationParameterIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val localisationName = storage.readOrReadFrom(previousInfo, { it.localisationName }, { storage.readUTFFast() })
        val elementOffset = storage.readIntFast()
        return ParadoxLocalisationParameterIndexInfo(name, localisationName, elementOffset, gameType)
    }
}
