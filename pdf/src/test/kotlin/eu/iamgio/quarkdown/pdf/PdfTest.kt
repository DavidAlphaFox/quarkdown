package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.pdf.html.HtmlToPdfExporter
import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import eu.iamgio.quarkdown.pdf.html.executable.NpmWrapper
import org.junit.Assume.assumeTrue
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for the PDF module.
 */
class PdfTest {
    private val directory: File =
        kotlin.io.path
            .createTempDirectory()
            .toFile()

    @BeforeTest
    fun setup() {
        directory.deleteRecursively()
        directory.mkdirs()
    }

    @Test
    fun `corresponding exporter`() {
        val html = QuarkdownFlavor.rendererFactory.html(MutableContext(QuarkdownFlavor))
        val htmlToPdf = PdfExporters.getForRenderingTarget(html.postRenderer, PdfExportOptions())

        assertIs<HtmlToPdfExporter>(htmlToPdf)
    }

    @Test
    fun `bare script on simple html`() {
        assumeTrue(NodeJsWrapper(workingDirectory = directory).isValid)
        assumeTrue(NpmWrapper().isValid)

        val html = File(directory, "index.html")
        html.writeText(
            """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Test</title>
            </head>
            <body>
                <h1>Hello, Quarkdown!</h1>
                <script>function isReady() { return true; }</script>
            </body>
            </html>
            """.trimIndent(),
        )

        val out = File(directory, "out.pdf")
        HtmlToPdfExporter(PdfExportOptions()).export(directory, out)
        Thread.sleep(1000)

        assertTrue(out.exists())
        assertFalse(File(directory, "pdf.js").exists())
        assertFalse(File(directory, "package.json").exists())
        assertFalse(File(directory, "package-lock.json").exists())
        assertFalse(File(directory, "node_modules").exists())
    }
}
