package org.rust.lang.core.resolve.indexes

import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.containers.HashMap
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.KeyDescriptor
import org.rust.lang.RustFileType
import org.rust.lang.core.names.RustFileModuleId
import org.rust.lang.core.names.RustQualifiedName
import org.rust.lang.core.psi.RustModItem
import org.rust.lang.core.psi.RustVisitor
import org.rust.lang.core.psi.impl.RustFileImpl
import org.rust.lang.core.psi.util.canonicalNameInFile
import org.rust.lang.core.psi.util.modDecls
import java.io.DataInput
import java.io.DataOutput

class RustModulesIndexExtension : FileBasedIndexExtension<RustModulePath, RustQualifiedName>() {

    override fun getVersion(): Int = 1

    override fun dependsOnFileContent(): Boolean = true

    override fun getName(): ID<RustModulePath, RustQualifiedName> = RustModulesIndex.ID

    override fun getInputFilter(): FileBasedIndex.InputFilter =
        DefaultFileTypeSpecificInputFilter(RustFileType)

    override fun getKeyDescriptor(): KeyDescriptor<RustModulePath> = myKeyDescriptor

    override fun getValueExternalizer(): DataExternalizer<RustQualifiedName> = myValueExternalizer

    override fun getIndexer(): DataIndexer<RustModulePath, RustQualifiedName, FileContent> = myDataIndexer

    object myKeyDescriptor : KeyDescriptor<RustModulePath> {
        override fun save(out: DataOutput, path: RustModulePath?) {
            path?.let {
                RustModulePath.writeTo(out, it)
            }
        }

        override fun read(`in`: DataInput): RustModulePath? =
            RustModulePath.readFrom(`in`)

        override fun isEqual(one: RustModulePath?, other: RustModulePath?): Boolean =
            one?.equals(other) ?: false

        override fun getHashCode(value: RustModulePath?): Int = value?.hashCode() ?: -1
    }

    object myValueExternalizer : DataExternalizer<RustQualifiedName> {

        override fun save(out: DataOutput, name: RustQualifiedName?) {
            name?.let { RustQualifiedName.writeTo(`out`, it) }
        }

        override fun read(`in`: DataInput): RustQualifiedName? {
            return RustQualifiedName.readFrom(`in`)
        }
    }

    object myDataIndexer: DataIndexer<RustModulePath, RustQualifiedName, FileContent> {
        override fun map(inputData: FileContent): MutableMap<RustModulePath, RustQualifiedName> {
            val map = HashMap<RustModulePath, RustQualifiedName>()

            PsiManager.getInstance(inputData.project).findFile(inputData.file)?.let {
                for ((qualName, targets) in process(it)) {
                    targets.forEach {
                        map.put(RustModulePath.devise(it), qualName)
                    }
                }
            }

            return map
        }

        private fun process(f: PsiFile): Map<RustQualifiedName, List<PsiFile>> {
            val raw = HashMap<RustQualifiedName, List<PsiFile>>()

            f.accept(object: RustVisitor() {

                //
                // TODO(kudinkin): move this to `RustVisitor`
                //
                override fun visitFile(file: PsiFile?) {
                    (file as? RustFileImpl)?.let {
                        it.mod?.accept(this)
                    }
                }

                override fun visitModItem(m: RustModItem) {
                    val resolved = arrayListOf<PsiFile>()

                    m.modDecls.forEach { decl ->
                        decl.reference?.let { ref ->
                            (ref.resolve() as RustModItem?)?.let { mod ->
                                resolved.add(mod.containingFile)
                            }
                        }
                    }

                    if (resolved.size > 0)
                        m.canonicalNameInFile?.let { raw.put(it, resolved) }

                    m.acceptChildren(this)
                }
            })

            return raw
        }
    }
}
