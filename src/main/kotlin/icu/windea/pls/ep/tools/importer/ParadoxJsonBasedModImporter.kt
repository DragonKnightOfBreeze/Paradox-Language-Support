package icu.windea.pls.ep.tools.importer

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.readAction
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.ObjectMappers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

/**
 * 使用 JSON 文件作为数据文件的模组导入器。
 */
abstract class ParadoxJsonBasedModImporter : ParadoxModImporter {
    override val icon = AllIcons.FileTypes.Json

    protected suspend fun <T> readData(filePath: Path, type: Class<T>): T {
        return withContext(Dispatchers.IO) {
            readAction {
                doReadData(filePath, type)
            }
        }
    }

    private fun <T> doReadData(filePath: Path, type: Class<T>): T {
        return try {
            ObjectMappers.jsonMapper.readValue(filePath.toFile(), type)
        } catch (e: Exception) {
            throw IllegalStateException(PlsBundle.message("mod.importer.error.data", filePath), e)
        }
    }
}
