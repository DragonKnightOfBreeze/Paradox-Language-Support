package icu.windea.pls

import javassist.*
import org.junit.*

class JavassistTest {
    @Test
    fun test1() {
        //需要加上以下jvm参数（IDEA在启动时会默认加上前者）
        //--add-opens java.base/java.lang=ALL-UNNAMED
        
        val pool = ClassPool.getDefault()
        pool.importPackage("icu.windea.pls")
        val classPathList = System.getProperty("java.class.path")
        val separator = if(System.getProperty("os.name")?.contains("linux") == true) ':' else ';'
        classPathList.split(separator).forEach {
            pool.appendClassPath(it)
        }
        val weaponClass = pool.get("icu.windea.pls.Weapon")
        val attackMethod = weaponClass.getDeclaredMethod("attack")
        attackMethod.setBody("{ new JavassistTest().hack();}")
        weaponClass.toClass()
        
        val weapon = Weapon()
        weapon.attack()
    }
    
    @Test(expected = LinkageError::class)
    fun test2() {
        //Caused by: java.lang.LinkageError: loader 'app' attempted duplicate class definition for icu.windea.pls.Weapon. (icu.windea.pls.Weapon is in unnamed module of loader 'app')
        
        //需要加上以下jvm参数（IDEA在启动时会默认加上前者）
        //--add-opens java.base/java.lang=ALL-UNNAMED
        
        val weapon1 = Weapon()
        
        val pool = ClassPool.getDefault()
        val classPathList = System.getProperty("java.class.path")
        val separator = if(System.getProperty("os.name")?.contains("linux") == true) ':' else ';'
        classPathList.split(separator).forEach {
            pool.appendClassPath(it)
        }
        val weaponClass = pool.get("icu.windea.pls.Weapon")
        val attackMethod = weaponClass.getDeclaredMethod("attack")
        attackMethod.setBody("{icu.windea.pls.JavassistTest().hack();}")
        weaponClass.toClass()
        
        val weapon = Weapon()
        weapon.attack()
    }
    
    @Test(expected = LinkageError::class)
    fun test3() {
        //Caused by: java.lang.LinkageError: loader 'app' attempted duplicate class definition for icu.windea.pls.Weapon. (icu.windea.pls.Weapon is in unnamed module of loader 'app')
        
        //需要加上以下jvm参数（IDEA在启动时会默认加上前者）
        //--add-opens java.base/java.lang=ALL-UNNAMED
        
        val weaponClassName = Weapon::class.java.name
        
        val pool = ClassPool.getDefault()
        val classPathList = System.getProperty("java.class.path")
        val separator = if(System.getProperty("os.name")?.contains("linux") == true) ':' else ';'
        classPathList.split(separator).forEach {
            pool.appendClassPath(it)
        }
        val weaponClass = pool.get("icu.windea.pls.Weapon")
        val attackMethod = weaponClass.getDeclaredMethod("attack")
        attackMethod.setBody("{icu.windea.pls.JavassistTest().hack();}")
        weaponClass.toClass()
        
        val weapon = Weapon()
        weapon.attack()
    }
    
    fun hack() {
        println("attack!!!")
    }
}

private class Weapon {
    fun attack() {
        println("attack!")
    }
}