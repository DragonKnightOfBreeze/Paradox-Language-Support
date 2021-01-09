package com.windea.plugin.idea.paradox.tool

import com.intellij.util.io.*
import java.io.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import java.util.zip.*
import kotlin.system.*

object ModPackager {
	private const val defaultPackageName = "mod.zip"
	
	/**
	 * 将指定的mod列表目录中的所有mod目录中的文件打包到该mod目录中。
	 */
	fun packageMods(modListPath: String, packageName: String = defaultPackageName, parallelism: Int = 16) {
		val dirs = File(modListPath).listFiles() ?: return
		doPackageMods(dirs, packageName, parallelism)
	}
	
	/**
	 * 将指定的mod目录中的文件打包到该mod目录中。
	 */
	fun packageMod(modPath: String, packageName: String = defaultPackageName) {
		val dir = File(modPath)
		doPackageMod(dir, packageName)
	}
	
	private fun doPackageMods(modDirs: Array<File>, packageName: String, parallelism: Int) {
		try {//并发打包mod
			val executor = Executors.newWorkStealingPool(parallelism)
			val total = modDirs.size
			val done = AtomicInteger(0)
			val countDownLatch = CountDownLatch(total)
			println("Package mods... (total: $total)")
			for(modDir in modDirs) {
				executor.execute {
					println("Package mod '${modDir.path}'... (total: $total, done: $done)")
					val time = measureTimeMillis {
						try {
							doPackage(modDir, packageName)
						} catch(e: Exception) {
							println("Package mods failed. An exception is thrown:")
							e.printStackTrace()
						}
					}.let { formatTime(it) }
					countDownLatch.countDown()
					done.incrementAndGet()
					println("Package mod '${modDir.path}' finished. (total: $total, done: $done, cost: $time)")
				}
			}
			//等待所有的mod打包完毕（可能需要很长时间）
			countDownLatch.await()
			println("Package mods finished. (total: $total)")
		} catch(e: Exception) {
			println("Package mods failed. An exception is thrown:")
			e.printStackTrace()
		}
	}
	
	private fun doPackageMod(modDir: File, packageName: String) {
		try {
			println("Package mod '${modDir.path}' ...")
			val time = measureTimeMillis {
				doPackage(modDir, packageName)
			}.let { formatTime(it) }
			println("Package mod '${modDir.path}' finished. (cost: $time)")
		} catch(e: Exception) {
			println("Package mods failed. An exception is thrown:")
			e.printStackTrace()
		}
	}
	
	private fun doPackage(modDir: File, packageName: String) {
		if(modDir.isDirectory) {
			val children = modDir.listFiles() ?: return
			//删除已存在的压缩包，跳过本身已打包的mod目录
			for(child in children) {
				if(child.name == packageName) {
					child.delete()
				} else if(child.extension == "zip") {
					return
				}
			}
			//打包mod目录中的所有文件和目录，除了zip文件，到mod目录中的mod.zip压缩包
			val zip = modDir.resolve(packageName)
			val fos = FileOutputStream(zip)
			val zos = ZipOutputStream(fos)
			for(child in children) {
				if(child.extension != "zip") {
					ZipUtil.addFileOrDirRecursively(zos, null, child, child.name, null, null)
				}
			}
			zos.close()
		}
	}
	
	private fun formatTime(millisecond: Long): String {
		return "${millisecond / 1000.0}s"
	}
}