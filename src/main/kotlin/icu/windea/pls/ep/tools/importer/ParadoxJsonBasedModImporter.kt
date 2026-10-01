package icu.windea.pls.ep.tools.importer

import com.intellij.icons.AllIcons
import icu.windea.pls.core.data.JsonService
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.decodeFromStream
import java.nio.file.Path
import kotlin.io.path.inputStream

/**
 * 使用 JSON 文件作为数据文件的模组导入器。
 */
abstract class ParadoxJsonBasedModImporter : ParadoxModImporter {
    override val icon get() = AllIcons.FileTypes.Json

    override fun isAvailable(gameType: ParadoxGameType) = true

    protected suspend fun <T> readData(filePath: Path, serializer: KSerializer<T>): T {
        return withContext(Dispatchers.IO) {
            // 这里不需要使用 readAction
            doReadData(filePath, serializer)
        }
    }

    private fun <T> doReadData(filePath: Path, serializer: KSerializer<T>): T {
        return try {
            filePath.inputStream().use { JsonService.json.decodeFromStream(serializer, it) }
        } catch (e: Exception) {
            throw IllegalStateException(ChronicleEpBundle.message("mod.importer.error.data", filePath), e)
        }
    }
}
