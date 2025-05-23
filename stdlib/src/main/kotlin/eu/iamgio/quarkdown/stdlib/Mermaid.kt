package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.quarkdown.block.MermaidDiagram
import eu.iamgio.quarkdown.ast.quarkdown.block.MermaidDiagramFigure
import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.IterableValue
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.Value
import eu.iamgio.quarkdown.function.value.data.EvaluableString
import eu.iamgio.quarkdown.function.value.data.Range
import eu.iamgio.quarkdown.function.value.wrappedAsValue
import eu.iamgio.quarkdown.stdlib.internal.asDouble
import eu.iamgio.quarkdown.util.indent

/**
 * `Mermaid` stdlib module exporter.
 * This module handles generation of Mermaid diagrams.
 */
val Mermaid: Module =
    setOf(
        ::mermaid,
        ::xyChart,
    )

private fun mermaidFigure(
    caption: String?,
    code: String,
) = MermaidDiagramFigure(
    MermaidDiagram(code),
    caption,
).wrappedAsValue()

/**
 * Creates a Mermaid diagram.
 * @param caption optional caption. If a caption is present, the diagram will be numbered as a figure.
 * @param code the Mermaid code of the diagram
 * @return the generated diagram node
 */
fun mermaid(
    caption: String? = null,
    code: EvaluableString,
) = mermaidFigure(caption, code.content)

/**
 * A chart line is a list of its points.
 */
private typealias ChartLine = List<Double>

/**
 * Extracts the lines from the given values.
 * If a value in [values] is a collection of points, then it's a line.
 * Any other value is considered a standalone point, and an additional line is created for them.
 * @param values the values to extract lines from
 * @return a list of chart lines
 */
private fun extractLines(values: Iterable<OutputValue<*>>): List<ChartLine> {
    val (lines, points) = values.partition { it is IterableValue<*> }
    val lineOfPoints: ChartLine = points.map { it.asDouble() }
    return lines
        .asSequence()
        .map { it as IterableValue<*> }
        .filterNot { it.unwrappedValue.none() }
        .map { it.unwrappedValue.map { point -> point.asDouble() } }
        .toMutableList()
        .apply {
            if (lineOfPoints.isNotEmpty()) add(lineOfPoints)
        }
}

/**
 * Creates a chart diagram on the XY plane.
 *
 * The following example plots 4 points at (1, 5), (2, 2), (3, 4), (4, 10), connected by a line:
 * ```
 * .xychart
 *   - 5
 *   - 2
 *   - 4
 *   - 10
 * ```
 *
 * Multiple lines can be plotted by supplying a list of lists of points. Each list will be plotted as a separate line:
 * ```
 * .xychart
 *   - |
 *     - 5
 *     - 8
 *     - 3
 *   - |
 *     - 3
 *     - 5
 *     - 10
 *   - |
 *     - 8
 *     - 3
 *     - 5
 * ```
 *
 * @param showLines whether to draw lines
 * @param showBars whether to draw bars
 * @param xAxisLabel optional label for the X axis
 * @param xAxisRange optional range for the X axis. If open-ended, the range will be set to the minimum and maximum values of the X values. Incompatible with [xAxisTags].
 * @param xAxisTags optional categorical tags for the X axis. Incompatible with [xAxisRange].
 * @param yAxisLabel optional label for the Y axis
 * @param yAxisRange optional range for the Y axis. If open-ended, the range will be set to the minimum and maximum values of the Y values
 * @param caption optional caption. If a caption is present, the diagram will be numbered as a figure.
 * @param values the Y values to plot.
 *               They can be a list of points, which will be plotted as a single line,
 *               or a list of lists of points, which will be plotted as multiple lines.
 * @return the generated diagram node
 * @throws IllegalArgumentException if both [xrange] and [xtags] are set
 */
@Name("xychart")
fun xyChart(
    @Name("lines") showLines: Boolean = true,
    @Name("bars") showBars: Boolean = false,
    @Name("x") xAxisLabel: String? = null,
    @Name("xrange") xAxisRange: Range? = null,
    @Name("xtags") xAxisTags: Iterable<Value<*>>? = null,
    @Name("y") yAxisLabel: String? = null,
    @Name("yrange") yAxisRange: Range? = null,
    caption: String? = null,
    values: Iterable<OutputValue<*>>,
): NodeValue {
    val lines: List<ChartLine> = extractLines(values)
    val (minY, maxY) = lines.flatten().let { (it.minOrNull() ?: 0.0) to (it.maxOrNull() ?: 1.0) }
    val (minX, maxX) = 0.0 to (lines.maxByOrNull { it.size }?.size?.toDouble() ?: 1.0)

    fun StringBuilder.axis(
        name: String,
        label: String?,
        range: Range?,
        tags: Iterable<Value<*>>?,
        min: Double,
        max: Double,
    ) {
        if (label == null && range == null && tags == null) return

        require(!(range != null && tags != null)) { "An XY chart axis cannot feature both numeric range and categorical tags." }

        append("\n")
        append(name).append("-axis")
        label?.let {
            append(" \"")
            append(it)
            append("\"")
        }
        range?.let {
            append(" ")
            append(it.start ?: min)
            append(" --> ")
            append(it.end ?: max)
        }
        tags?.let {
            append(" ")
            append(it.map { tag -> tag.unwrappedValue })
        }
    }

    val content =
        buildString {
            axis("x", xAxisLabel, xAxisRange, xAxisTags, minX, maxX)
            axis("y", yAxisLabel, yAxisRange, null, minY, maxY)

            lines.forEach { points ->
                if (showBars) {
                    append("\n")
                    append("bar ")
                    append(points)
                }
                if (showLines) {
                    append("\n")
                    append("line ")
                    append(points)
                }
            }
        }

    val code = "xychart-beta\n" + content.indent("\t")
    return mermaidFigure(caption, code)
}
