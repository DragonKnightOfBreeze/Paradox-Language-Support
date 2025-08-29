package icu.windea.pls.lang.index

import icu.windea.pls.core.util.createKey

object ParadoxIndexManager {
    val excludeDirectoriesForFilePathIndex = listOf(
        "_CommonRedist",
        "crash_reporter",
        "curated_save_games",
        "pdx_browser",
        "pdx_launcher",
        "pdx_online_assets",
        "previewer_assets",
        "tweakergui_assets",
        "jomini",
    )

    val indexInfoMarkerKey = createKey<Boolean>("paradox.merged.info.index.marker")
}
