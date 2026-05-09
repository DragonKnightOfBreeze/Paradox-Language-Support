package icu.windea.pls.ep.analysis

import icu.windea.pls.lang.analysis.ParadoxRootMetadataUtil
import icu.windea.pls.model.analysis.ParadoxDescriptorModBasedModMetadata
import icu.windea.pls.model.analysis.ParadoxMetadataJsonBasedModMetadata
import icu.windea.pls.model.analysis.ParadoxRootMetadata
import java.nio.file.Path

/**
 * 参见：[Mod structure - Victoria 3 Wiki](https://vic3.paradoxwikis.com/index.php?title=Mod_structure)
 */
class ParadoxMetadataJsonBasedModMetadataProvider : ParadoxRootMetadataProvider {
    override fun get(rootPath: Path): ParadoxRootMetadata? {
        // 尝试在根目录的 `.metadata` 子目录中查找 `metadata.json`

        val infoPath = ParadoxRootMetadataUtil.getMetadataJsonPath(rootPath) ?: return null
        val info = ParadoxRootMetadataUtil.getMetadataJsonInfo(infoPath) ?: return null
        return ParadoxMetadataJsonBasedModMetadata(rootPath, infoPath, info)
    }
}

class ParadoxDescriptorModBasedModMetadataProvider : ParadoxRootMetadataProvider {
    override fun get(rootPath: Path): ParadoxRootMetadata? {
        // 尝试在根目录中查找 `descriptor.mod`

        val infoPath = ParadoxRootMetadataUtil.getDescriptorModPath(rootPath) ?: return null
        val info = ParadoxRootMetadataUtil.getDescriptorModInfo(infoPath) ?: return null
        return ParadoxDescriptorModBasedModMetadata(rootPath, infoPath, info)
    }
}
