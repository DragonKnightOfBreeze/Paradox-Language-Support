package icu.windea.pls.lang

import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileWithoutContent
import com.intellij.util.application
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.util.ParadoxCoreManager
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.PlsConstants

/**
 * 文件类型重载器。
 *
 * 基于文件的扩展名以及相对于入口目录的路径，将符合的文件重载为脚本文件或本地化文件。
 */
class ParadoxFileTypeOverrider : FileTypeOverrider {
    override fun getOverriddenFileType(file: VirtualFile): FileType? {
        if (file.isDirectory) return null
        if (file is VirtualFileWithoutContent) return null

        runCatchingCancelable r@{
            val injectedFileInfo = file.getUserData(PlsKeys.injectedFileInfo) ?: return@r
            return ParadoxFileManager.getFileType(injectedFileInfo.fileType)
        }

        runCatchingCancelable r@{
            val fileInfo = file.getUserData(PlsKeys.fileInfo)?.castOrNull<ParadoxFileInfo>() ?: return@r
            return ParadoxFileManager.getFileType(fileInfo.fileType)
        }

        if (file is VirtualFileWindow) {
            val fileInfo = ParadoxCoreManager.getFileInfo(file) ?: return null
            return ParadoxFileManager.getFileType(fileInfo.fileType)
        }

        val possibleFileType = ParadoxFileType.resolvePossible(file.name)
        if (possibleFileType == ParadoxFileType.Other) return null

        // Inject user data for test data files
        runCatchingCancelable r@{
            if (!application.isUnitTestMode) return@r

            // t_test.yml -> inject file type `ParadoxLocalisationFileType`
            // t_test_stellaris.yml -> also inject game type `stellaris`

            val s1 = file.name.removePrefixOrNull(PlsConstants.testDataFileNamePrefix)
            if (s1 == null) return@r
            val s2 = ParadoxGameType.entries.find { s1.contains("_" + it.id + ".") }
            if (s2 != null) file.putUserData(PlsKeys.injectedGameType, s2)
            return ParadoxFileManager.getFileType(possibleFileType)
        }

        val fileInfo = ParadoxCoreManager.getFileInfo(file) ?: return null
        return ParadoxFileManager.getFileType(fileInfo.fileType)
    }
}
