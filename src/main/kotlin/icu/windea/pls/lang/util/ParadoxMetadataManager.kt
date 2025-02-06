package icu.windea.pls.lang.util

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.data.*
import icu.windea.pls.lang.util.data.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

object ParadoxMetadataManager {
    val metadataFileNames = setOf(
        "launcher-settings.json",
        "descriptor.mod",
        "metadata.json",
    )

    fun getLauncherSettingsFile(rootFile: VirtualFile): VirtualFile? {
        //relative paths:
        //* launcher-settings.json
        //* launcher/launcher-settings.json

        if (rootFile.name == "launcher") return null
        rootFile.findChild("launcher-settings.json")
            ?.takeIf { it.isFile }?.let { return it }
        rootFile.findFileByRelativePath("launcher/launcher-settings.json")
            ?.takeIf { it.isFile }?.let { return it }
        return null
    }

    fun getLauncherSettingsInfo(file: VirtualFile): ParadoxLauncherSettingsInfo? {
        try {
            return doGetLauncherSettingsInfo(file)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            return null
        }
    }

    private fun doGetLauncherSettingsInfo(file: VirtualFile): ParadoxLauncherSettingsInfo {
        return jsonMapper.readValue(file.inputStream)
    }

    fun getModDescriptorFile(rootFile: VirtualFile): VirtualFile? {
        //relative paths:
        //* descriptor.mod

        rootFile.findChild("descriptor.mod")
            ?.takeIf { it.isFile }?.let { return it }
        return null
    }

    fun getModDescriptorInfo(file: VirtualFile): ParadoxModDescriptorInfo? {
        try {
            return runReadAction { doGetModDescriptorInfo(file) }
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            return null
        }
    }

    private fun doGetModDescriptorInfo(file: VirtualFile): ParadoxModDescriptorInfo {
        //val psiFile = file.toPsiFile<ParadoxScriptFile>(getDefaultProject()) ?: return null //会导致StackOverflowError
        val psiFile = ParadoxScriptElementFactory.createDummyFile(getDefaultProject(), file.inputStream.reader().readText())
        val data = ParadoxScriptDataResolver.resolve(psiFile)
        val name = data?.getData("name")?.value?.stringValue() ?: file.parent?.name ?: "" //如果没有name属性，则使用根目录名
        val version = data?.getData("version")?.value?.stringValue()
        val picture = data?.getData("picture")?.value?.stringValue()
        val tags = data?.getAllData("tags")?.mapNotNull { it.value?.stringValue() }?.toSet().orEmpty()
        val supportedVersion = data?.getData("supported_version")?.value?.stringValue()
        val remoteFileId = data?.getData("remote_file_id")?.value?.stringValue()
        val path = data?.getData("path")?.value?.stringValue()
        return ParadoxModDescriptorInfo(name, version, picture, tags, supportedVersion, remoteFileId, path)
    }

    fun getModMetadataFile(rootFile: VirtualFile): VirtualFile? {
        //relative paths:
        //* .metadata/metadata.json

        rootFile.findFileByRelativePath(".metadata/metadata.json")
            ?.takeIf { it.isFile }?.let { return it }
        return null
    }

    fun getModMetadataInfo(file: VirtualFile): ParadoxModMetadataInfo? {
        try {
            return doGetModMetadataInfo(file)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            return null
        }
    }

    private fun doGetModMetadataInfo(file: VirtualFile): ParadoxModMetadataInfo {
        return jsonMapper.readValue(file.inputStream)
    }
}
