package icu.windea.pls.ep.tools.exporter

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.edtWriteAction
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.ObjectMappers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

/**
 * 使用 JSON 文件作为数据文件的模组导出器。
 */
abstract class ParadoxJsonBasedModExporter : ParadoxModExporter {
    override val icon = AllIcons.FileTypes.Json

    protected suspend fun writeData(filePath: Path, data: Any) {
        withContext(Dispatchers.IO) {
            edtWriteAction {
                doWriteData(filePath, data)
            }
        }
    }

    private fun doWriteData(filePath: Path, data: Any) {
        try {
            ObjectMappers.jsonMapper.writeValue(filePath.toFile(), data)
        } catch (e: Exception) {
            throw IllegalStateException(PlsBundle.message("mod.exporter.error.data", filePath), e)
        }
    }
}
