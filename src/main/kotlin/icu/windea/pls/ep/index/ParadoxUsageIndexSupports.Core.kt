package icu.windea.pls.ep.index

import com.intellij.codeInsight.highlighting.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.model.usageInfo.*
import icu.windea.pls.script.psi.*
import java.io.*

class ParadoxComplexEnumValueUsageIndexSupport : ParadoxUsageIndexSupport<ParadoxComplexEnumValueUsageInfo> {
    private val compressComparator = compareBy<ParadoxComplexEnumValueUsageInfo>({ it.enumName }, { it.name })

    override fun id() = ParadoxUsageIndexType.ComplexEnumValue.id

    override fun type() = ParadoxComplexEnumValueUsageInfo::class.java

    override fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        if (element !is ParadoxScriptStringExpressionElement) return
        if (!element.isExpression()) return
        val info = ParadoxComplexEnumValueManager.getInfo(element) ?: return
        addToFileData(info, fileData)
    }

    override fun compressData(value: List<ParadoxComplexEnumValueUsageInfo>): List<ParadoxComplexEnumValueUsageInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxComplexEnumValueUsageInfo, previousInfo: ParadoxComplexEnumValueUsageInfo?) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.enumName }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.optimizeValue())
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxComplexEnumValueUsageInfo?): ParadoxComplexEnumValueUsageInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val enumName = storage.readOrReadFrom(previousInfo, { it.enumName }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().deoptimizeValue<ReadWriteAccessDetector.Access>()
        val elementOffset = storage.readIntFast()
        return ParadoxComplexEnumValueUsageInfo(name, enumName, readWriteAccess, elementOffset)
    }
}

class ParadoxDynamicValueUsageIndexSupport : ParadoxUsageIndexSupport<ParadoxDynamicValueUsageInfo> {
    private val compressComparator = compareBy<ParadoxDynamicValueUsageInfo>({ it.dynamicValueType }, { it.name })

    override fun id() = ParadoxUsageIndexType.DynamicValue.id

    override fun type() = ParadoxDynamicValueUsageInfo::class.java

    override fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        val constraint = ParadoxResolveConstraint.DynamicValue
        if (!constraint.canResolveReference(element)) return
        element.references.forEach f@{ reference ->
            if (!constraint.canResolve(reference)) return@f
            val resolved = reference.resolve()
            if (resolved !is ParadoxDynamicValueElement) return@f
            resolved.dynamicValueTypes.forEach { dynamicValueType ->
                val info = ParadoxDynamicValueUsageInfo(resolved.name, dynamicValueType, resolved.readWriteAccess, resolved.parent.startOffset)
                addToFileData(info, fileData)
            }
        }
    }

    override fun indexLocalisationCommandText(element: ParadoxLocalisationCommandText, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        val constraint = ParadoxResolveConstraint.DynamicValue
        if (!constraint.canResolveReference(element)) return
        element.references.forEach f@{ reference ->
            if (!constraint.canResolve(reference)) return@f
            val resolved = reference.resolve()
            if (resolved !is ParadoxDynamicValueElement) return@f
            resolved.dynamicValueTypes.forEach { dynamicValueType ->
                val info = ParadoxDynamicValueUsageInfo(resolved.name, dynamicValueType, resolved.readWriteAccess, resolved.parent.startOffset)
                addToFileData(info, fileData)
            }
        }
    }

    override fun compressData(value: List<ParadoxDynamicValueUsageInfo>): List<ParadoxDynamicValueUsageInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxDynamicValueUsageInfo, previousInfo: ParadoxDynamicValueUsageInfo?) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.dynamicValueType }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.optimizeValue())
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxDynamicValueUsageInfo?): ParadoxDynamicValueUsageInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val dynamicValueType = storage.readOrReadFrom(previousInfo, { it.dynamicValueType }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().deoptimizeValue<ReadWriteAccessDetector.Access>()
        val elementOffset = storage.readIntFast()
        return ParadoxDynamicValueUsageInfo(name, dynamicValueType, readWriteAccess, elementOffset)
    }
}

class ParadoxParameterUsageIndexSupport : ParadoxUsageIndexSupport<ParadoxParameterUsageInfo> {
    private val compressComparator = compareBy<ParadoxParameterUsageInfo>({ it.contextKey }, { it.name })

    override fun id() = ParadoxUsageIndexType.Parameter.id

    override fun type() = ParadoxParameterUsageInfo::class.java

    override fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        val constraint = ParadoxResolveConstraint.Parameter
        if (!constraint.canResolveReference(element)) return
        element.references.forEach f@{ reference ->
            if (!constraint.canResolve(reference)) return@f
            val resolved = reference.resolve()
            if (resolved !is ParadoxParameterElement) return@f
            //note that element.startOffset may not equal to actual parameterElement.startOffset (e.g. in a script value expression)
            val info = ParadoxParameterUsageInfo(resolved.name, resolved.contextKey, resolved.readWriteAccess, element.startOffset)
            addToFileData(info, fileData)
        }
    }

    override fun compressData(value: List<ParadoxParameterUsageInfo>): List<ParadoxParameterUsageInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxParameterUsageInfo, previousInfo: ParadoxParameterUsageInfo?) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.contextKey }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.optimizeValue())
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxParameterUsageInfo?): ParadoxParameterUsageInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val contextKey = storage.readOrReadFrom(previousInfo, { it.contextKey }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().deoptimizeValue<ReadWriteAccessDetector.Access>()
        val elementOffset = storage.readIntFast()
        return ParadoxParameterUsageInfo(name, contextKey, readWriteAccess, elementOffset)
    }
}

class ParadoxLocalisationParameterUsageIndexSupport : ParadoxUsageIndexSupport<ParadoxLocalisationParameterUsageInfo> {
    private val compressComparator = compareBy<ParadoxLocalisationParameterUsageInfo>({ it.localisationName }, { it.name })

    override fun id() = ParadoxUsageIndexType.LocalisationParameter.id

    override fun type() = ParadoxLocalisationParameterUsageInfo::class.java

    override fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        val constraint = ParadoxResolveConstraint.LocalisationParameter
        if (!constraint.canResolveReference(element)) return
        element.references.forEach f@{ reference ->
            if (!constraint.canResolve(reference)) return@f
            val resolved = reference.resolve()
            if (resolved !is ParadoxLocalisationParameterElement) return@f
            val info = ParadoxLocalisationParameterUsageInfo(resolved.name, resolved.localisationName, element.startOffset)
            addToFileData(info, fileData)
        }
    }

    override fun compressData(value: List<ParadoxLocalisationParameterUsageInfo>): List<ParadoxLocalisationParameterUsageInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxLocalisationParameterUsageInfo, previousInfo: ParadoxLocalisationParameterUsageInfo?) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.localisationName }, { storage.writeUTFFast(it) })
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxLocalisationParameterUsageInfo?): ParadoxLocalisationParameterUsageInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val localisationName = storage.readOrReadFrom(previousInfo, { it.localisationName }, { storage.readUTFFast() })
        val elementOffset = storage.readIntFast()
        return ParadoxLocalisationParameterUsageInfo(name, localisationName, elementOffset)
    }
}
