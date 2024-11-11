package icu.windea.pls.ep.index

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.usageInfo.*
import icu.windea.pls.script.psi.*
import java.io.*

interface ParadoxUsageIndexSupport<T : ParadoxUsageInfo> {
    fun id(): Byte

    fun type(): Class<T>

    fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {}

    fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {}

    fun indexLocalisationCommandText(element: ParadoxLocalisationCommandText, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {}

    fun compressData(value: List<T>): List<T> = value

    fun writeData(storage: DataOutput, info: T, previousInfo: T?, gameType: ParadoxGameType)

    fun readData(storage: DataInput, previousInfo: T?, gameType: ParadoxGameType): T

    fun <T : ParadoxUsageInfo> addToFileData(info: T, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        val list = fileData.getOrPut(id().toString()) { mutableListOf() } as MutableList
        list.add(info)
    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxUsageIndexSupport<*>>("icu.windea.pls.usageIndexSupport")
    }
}
