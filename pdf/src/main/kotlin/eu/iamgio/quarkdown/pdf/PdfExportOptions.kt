package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.pdf.html.NodeJsWrapper

/**
 * Options for exporting PDF files.
 */
data class PdfExportOptions(
    val nodeJsPath: String = NodeJsWrapper.DEFAULT_PATH,
)
