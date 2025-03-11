package icu.windea.pls.ep.index

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.psi.*
import java.io.*

interface ParadoxIndexInfoSupport<T : ParadoxIndexInfo> {
    val id: Byte

    val type: Class<T>

    fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {}

    fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {}

    fun indexLocalisationExpression(element: ParadoxLocalisationExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {}

    fun compressData(value: List<T>): List<T> = value

    fun writeData(storage: DataOutput, info: T, previousInfo: T?, gameType: ParadoxGameType)

    fun readData(storage: DataInput, previousInfo: T?, gameType: ParadoxGameType): T

    fun <T : ParadoxIndexInfo> addToFileData(info: T, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        val list = fileData.getOrPut(id.toString()) { mutableListOf() } as MutableList
        list.add(info)
    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxIndexInfoSupport<*>>("icu.windea.pls.infoIndexSupport")
    }
}
