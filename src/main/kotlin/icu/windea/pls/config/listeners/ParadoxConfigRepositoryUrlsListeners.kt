package icu.windea.pls.config.listeners

import icu.windea.pls.config.util.CwtConfigRepositoryManager

/**
 * 当更改远程规则仓库地址后，进行同步。如果远程规则目录未配置，将会被直接跳过。
 */
class CwtSyncOnConfigRepositoryUrlsChangedListener: CwtConfigRepositoryUrlsListener {
    override fun onChange() {
        doSync()
    }

    private fun doSync() {
        CwtConfigRepositoryManager.syncFromUrls()
    }
}
