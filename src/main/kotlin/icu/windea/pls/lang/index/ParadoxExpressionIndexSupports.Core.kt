package icu.windea.pls.lang.index

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.psi.*
import java.io.*

class ParadoxComplexEnumValueIndexSupport : ParadoxExpressionIndexSupport<ParadoxComplexEnumValueInfo> {
    private val compressComparator = compareBy<ParadoxComplexEnumValueInfo>({ it.enumName }, { it.name })
    
    override fun id() = ParadoxExpressionIndexId.ComplexEnumValue.id
    
    override fun type() = ParadoxComplexEnumValueInfo::class.java
    
    override fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        if(!element.isExpression()) return
        val info = ParadoxComplexEnumValueHandler.getInfo(element) ?: return
        addToFileData(info, fileData)
    }
    
    override fun compressData(value: List<ParadoxComplexEnumValueInfo>): List<ParadoxComplexEnumValueInfo> {
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

class ParadoxValueSetValueIndexSupport : ParadoxExpressionIndexSupport<ParadoxValueSetValueInfo> {
    private val compressComparator = compareBy<ParadoxValueSetValueInfo>({ it.valueSetName }, { it.name })
    
    override fun id() = ParadoxExpressionIndexId.ValueSetValue.id
    
    override fun type() = ParadoxValueSetValueInfo::class.java
    
    override fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        val constraint = ParadoxResolveConstraint.ValueSetValue
        if(!constraint.canResolveReference(element)) return
        element.references.forEachFast f@{ reference ->
            if(!constraint.canResolve(reference)) return@f
            val resolved = reference.resolve()
            if(resolved !is ParadoxValueSetValueElement) return@f
            resolved.valueSetNames.forEach { valueSetName ->
                val info = ParadoxValueSetValueInfo(resolved.name, valueSetName, resolved.readWriteAccess, resolved.parent.startOffset, resolved.gameType)
                addToFileData(info, fileData)
            }
        }
    }
    
    override fun indexLocalisationCommandIdentifier(element: ParadoxLocalisationCommandIdentifier, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        val constraint = ParadoxResolveConstraint.ValueSetValue
        if(!constraint.canResolveReference(element)) return
        element.references.forEachFast f@{ reference ->
            if(!constraint.canResolve(reference)) return@f
            val resolved = reference.resolve()
            if(resolved !is ParadoxValueSetValueElement) return@f
            resolved.valueSetNames.forEach { valueSetName ->
                val info = ParadoxValueSetValueInfo(resolved.name, valueSetName, resolved.readWriteAccess, resolved.parent.startOffset, resolved.gameType)
                addToFileData(info, fileData)
            }
        }
    }
    
    override fun compressData(value: List<ParadoxValueSetValueInfo>): List<ParadoxValueSetValueInfo> {
        return value.sortedWith(compressComparator)
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxValueSetValueInfo, previousInfo: ParadoxValueSetValueInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.valueSetName }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.toByte())
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxValueSetValueInfo?, gameType: ParadoxGameType): ParadoxValueSetValueInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val valueSetName = storage.readOrReadFrom(previousInfo, { it.valueSetName }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().toReadWriteAccess()
        val elementOffset = storage.readIntFast()
        return ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType)
    }
}

class ParadoxInlineScriptUsageIndexSupport : ParadoxExpressionIndexSupport<ParadoxInlineScriptUsageInfo> {
    private val compressComparator = compareBy<ParadoxInlineScriptUsageInfo> { it.expression }
    
    override fun id() = ParadoxExpressionIndexId.InlineScriptUsage.id
    
    override fun type() = ParadoxInlineScriptUsageInfo::class.java
    
    override fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        if(element !is ParadoxScriptProperty) return
        val info = ParadoxInlineScriptHandler.getUsageInfo(element) ?: return
        addToFileData(info, fileData)
    }
    
    override fun compressData(value: List<ParadoxInlineScriptUsageInfo>): List<ParadoxInlineScriptUsageInfo> {
        return value.sortedWith(compressComparator)
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxInlineScriptUsageInfo, previousInfo: ParadoxInlineScriptUsageInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.expression)
        storage.writeInt(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxInlineScriptUsageInfo?, gameType: ParadoxGameType): ParadoxInlineScriptUsageInfo {
        val expression = storage.readUTFFast()
        val elementOffset = storage.readInt()
        return ParadoxInlineScriptUsageInfo(expression, elementOffset, gameType)
    }
}

class ParadoxParameterIndexSupport : ParadoxExpressionIndexSupport<ParadoxParameterInfo> {
    private val compressComparator = compareBy<ParadoxParameterInfo>({ it.contextKey }, { it.name })
    
    override fun id() = ParadoxExpressionIndexId.Parameter.id
    
    override fun type() = ParadoxParameterInfo::class.java
    
    override fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
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
    
    override fun compressData(value: List<ParadoxParameterInfo>): List<ParadoxParameterInfo> {
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

class ParadoxLocalisationParameterIndexSupport : ParadoxExpressionIndexSupport<ParadoxLocalisationParameterInfo> {
    private val compressComparator = compareBy<ParadoxLocalisationParameterInfo>({ it.localisationName }, { it.name })
    
    override fun id() = ParadoxExpressionIndexId.LocalisationParameter.id
    
    override fun type() = ParadoxLocalisationParameterInfo::class.java
    
    override fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
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
    
    override fun compressData(value: List<ParadoxLocalisationParameterInfo>): List<ParadoxLocalisationParameterInfo> {
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