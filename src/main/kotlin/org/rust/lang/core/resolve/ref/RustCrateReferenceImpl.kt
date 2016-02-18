package org.rust.lang.core.resolve.ref

import com.intellij.psi.PsiReferenceBase
import org.rust.lang.core.psi.RustExternCrateItem
import org.rust.lang.core.psi.RustModItem
import org.rust.lang.core.psi.RustNamedElement
import org.rust.lang.core.psi.util.parentRelativeRange
import org.rust.lang.core.resolve.RustResolveEngine

class RustCrateReferenceImpl(crateDecl: RustExternCrateItem)
    : PsiReferenceBase<RustExternCrateItem>(crateDecl, crateDecl.identifier.parentRelativeRange)
    , RustReference {

    override fun resolve(): RustModItem? =
        RustResolveEngine.resolveCrate(element).element as? RustModItem

    override fun getVariants(): Array<out Any> = emptyArray()
}
