package org.rust.cargo

import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.vfs.VirtualFile

object Crates {
    fun getExternCrateByName(module: Module, crateName: String): VirtualFile? {
        var result: VirtualFile? = null
        ModuleRootManager.getInstance(module).orderEntries().forEachLibrary { library ->
            if (library.name?.startsWith("Cargo: $crateName ") ?: false) {
                result = library.getFiles(OrderRootType.SOURCES).first()?.findChild("src/lib.rs")
                false
            } else {
                true
            }
        }
        return result
    }
}
