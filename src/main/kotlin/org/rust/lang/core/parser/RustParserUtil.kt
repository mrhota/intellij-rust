package org.rust.lang.core.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.openapi.util.Key
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.rust.lang.core.psi.RustTokenElementTypes
import org.rust.lang.core.lexer.containsEOL
import org.rust.lang.utils.Cookie
import org.rust.lang.utils.using

object RustParserUtil : GeneratedParserUtilBase() {

    private val STRUCT_ALLOWED: Key<Boolean> = Key("org.rust.STRUCT_ALLOWED")

    private fun PsiBuilder.getStructAllowed(): Boolean {
        return getUserData(STRUCT_ALLOWED) ?: true
    }

    private fun PsiBuilder.setStructAllowed(value: Boolean): Boolean {
        val r = getStructAllowed()
        putUserData(STRUCT_ALLOWED, value)
        return r
    }


    //
    // Helpers
    //

    @JvmStatic fun injectInto(b: PsiBuilder, level: Int, s: Parser, t: Parser) : Boolean {
        fun done(m: PsiBuilder.Marker, tt: IElementType)    { m.done(tt) }
        fun drop(m: PsiBuilder.Marker)                      { m.drop() }

        val m = b.mark()
        var r = false

        try {

            r = s.parse(b, level)
            r = r && t.parse(b, level)

        } catch (t: Throwable) {
            drop(m)
            throw t
        }

        if (r) {
            // This is the one that should be done
            // by `t`
            b.latestDoneMarker?.let { p ->
                done(m, p.tokenType)
                drop(p as PsiBuilder.Marker)
            }
            return true
        }

        drop(m)
        return false;
    }

    @JvmStatic fun checkStructAllowed(b: PsiBuilder, level: Int) : Boolean = b.getStructAllowed()

    @JvmStatic fun checkBraceAllowed(b: PsiBuilder, level: Int) : Boolean {
        return b.getStructAllowed() || b.tokenType != RustTokenElementTypes.LBRACE
    }

    @JvmStatic fun withoutStructLiterals(b: PsiBuilder, level: Int, parser: GeneratedParserUtilBase.Parser): Boolean {
        return using (Cookie(b, { this.setStructAllowed(it) }, false)) {
            parser.parse(b, level)
        }
    }

    @JvmStatic fun withStructLiterals(b: PsiBuilder, level: Int, parser: GeneratedParserUtilBase.Parser): Boolean {
        return using (Cookie(b, { this.setStructAllowed(it) }, true)) {
            parser.parse(b, level)
        }
    }

    @JvmStatic fun skipUntilEOL(b: PsiBuilder, level: Int): Boolean {
        while (!b.eof()) {
            if (b.tokenType == TokenType.WHITE_SPACE
            ||  b.tokenText?.containsEOL() != null) return true;

            b.advanceLexer();
        }

        return false;
    }

    @JvmStatic fun unpairedToken(b: PsiBuilder, level: Int): Boolean {
        when (b.tokenType) {
            RustTokenElementTypes.LBRACE, RustTokenElementTypes.RBRACE -> return false
            RustTokenElementTypes.LPAREN, RustTokenElementTypes.RPAREN -> return false
            RustTokenElementTypes.LBRACK, RustTokenElementTypes.RBRACK -> return false
            else -> {
                b.advanceLexer();
                return true
            }
        }
    }
}

