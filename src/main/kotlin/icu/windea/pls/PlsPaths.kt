package icu.windea.pls

val userHome = System.getProperty("user.home")
const val dataDirectoryName = ".pls"
const val imagesDirectoryName = "images"
const val unknownPngName = "unknown.png"

val userHomePath by lazy { userHome.toPath() }
val dataDirectoryPath by lazy { userHomePath.resolve(dataDirectoryName) }
val imagesDirectoryPath by lazy { dataDirectoryPath.resolve(imagesDirectoryName) }
val unknownPngPath by lazy { imagesDirectoryPath.resolve(unknownPngName) }