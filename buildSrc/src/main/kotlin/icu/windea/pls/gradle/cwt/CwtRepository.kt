@file:Suppress("unused")

package icu.windea.pls.gradle.cwt

/**
 * 一个 CWT 配置仓库。
 */
data class CwtRepository(
    val repoDir: String,
    val gameTypeId: String,
    val owner: String = "DragonKnightOfBreeze",
    val branch: String = "master",
    val downloadable: Boolean = true,
) {
    val zipUrl: String = "https://github.com/$owner/$repoDir/archive/refs/heads/$branch.zip"
}
