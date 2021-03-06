package org.rust.ide.documentation

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.openapi.util.io.FileUtil
import org.assertj.core.api.Assertions.assertThat
import org.rust.lang.RustTestCaseBase
import java.io.File

class RustDocumentationProviderTest : RustTestCaseBase() {

    override val dataPath = "org/rust/ide/documentation/fixtures"

    private fun doTest(expected: String) {
        myFixture.configureByFile(fileName)
        val originalElement = myFixture.elementAtCaret
        val element = DocumentationManager.getInstance(project).findTargetElement(myFixture.editor, myFixture.file)
        val doc = RustDocumentationProvider().getQuickNavigateInfo(element, originalElement)
        assertThat(doc).isEqualTo(expected);
    }

    private fun doFileTest() {
        val text = FileUtil.loadFile(File(testDataPath + "/" + fileName.replace(".rs", ".html"))).trim()
        doTest(text)
    }

    fun testVariable1() = doFileTest()
    fun testVariable2() = doFileTest()
    fun testNestedFunction() = doFileTest()
}
