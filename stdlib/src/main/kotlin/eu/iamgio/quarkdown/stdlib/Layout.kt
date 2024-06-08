package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.Aligned
import eu.iamgio.quarkdown.ast.Box
import eu.iamgio.quarkdown.ast.Clipped
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Stacked
import eu.iamgio.quarkdown.ast.Table
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.document.page.Size
import eu.iamgio.quarkdown.document.page.SizeUnit
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.reflect.Name
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.Value
import eu.iamgio.quarkdown.function.value.ValueFactory
import eu.iamgio.quarkdown.function.value.wrappedAsValue
import eu.iamgio.quarkdown.misc.Color

/**
 * `Layout` stdlib module exporter.
 * This module handles position and shape of an element.
 */
val Layout: Module =
    setOf(
        ::align,
        ::center,
        ::stack,
        ::row,
        ::column,
        ::clip,
        ::box,
        ::table,
    )

/**
 * Aligns content within its parent.
 * @param alignment content alignment anchor
 * @param body content to center
 * @return the new aligned block
 */
fun align(
    alignment: Aligned.Alignment,
    body: MarkdownContent,
) = Aligned(alignment, body.children).wrappedAsValue()

/**
 * Centers content within its parent.
 * @param body content to center
 * @return the new aligned block
 * @see align
 */
fun center(body: MarkdownContent) = align(Aligned.Alignment.CENTER, body)

/**
 * Default gap between stacked contents.
 * @see stack
 * @see row
 * @see column
 */
private val DEFAULT_STACK_GAP = Size(12.0, SizeUnit.PX)

/**
 * Stacks content along an axis.
 * @param orientation orientation of the stack
 * @param mainAxisAlignment content alignment along the main axis
 * @param crossAxisAlignment content alignment along the cross axis
 * @param gap blank space between children
 * @param body content to stack
 * @return the new stacked block
 * @see row
 * @see column
 */
fun stack(
    orientation: Stacked.Orientation,
    @Name("alignment") mainAxisAlignment: Stacked.MainAxisAlignment = Stacked.MainAxisAlignment.START,
    @Name("cross") crossAxisAlignment: Stacked.CrossAxisAlignment = Stacked.CrossAxisAlignment.CENTER,
    gap: Size = DEFAULT_STACK_GAP,
    body: MarkdownContent,
) = Stacked(orientation, mainAxisAlignment, crossAxisAlignment, gap, body.children).wrappedAsValue()

/**
 * Stacks content horizontally.
 * @param mainAxisAlignment content alignment along the main axis
 * @param crossAxisAlignment content alignment along the cross axis
 * @param gap blank space between children
 * @param body content to stack
 * @return the new stacked block
 * @see stack
 */
fun row(
    @Name("alignment") mainAxisAlignment: Stacked.MainAxisAlignment = Stacked.MainAxisAlignment.START,
    @Name("cross") crossAxisAlignment: Stacked.CrossAxisAlignment = Stacked.CrossAxisAlignment.CENTER,
    gap: Size = DEFAULT_STACK_GAP,
    body: MarkdownContent,
) = stack(Stacked.Orientation.HORIZONTAL, mainAxisAlignment, crossAxisAlignment, gap, body)

/**
 * Stacks content vertically.
 * @param mainAxisAlignment content alignment along the main axis
 * @param crossAxisAlignment content alignment along the cross axis
 * @param gap blank space between children
 * @param body content to stack
 * @return the new stacked block
 * @see stack
 */
fun column(
    @Name("alignment") mainAxisAlignment: Stacked.MainAxisAlignment = Stacked.MainAxisAlignment.START,
    @Name("cross") crossAxisAlignment: Stacked.CrossAxisAlignment = Stacked.CrossAxisAlignment.CENTER,
    gap: Size = DEFAULT_STACK_GAP,
    body: MarkdownContent,
) = stack(Stacked.Orientation.VERTICAL, mainAxisAlignment, crossAxisAlignment, gap, body)

/**
 * Applies a clipping path to its content.
 * @param clip clip type to apply
 * @return the new clipped block
 */
fun clip(
    clip: Clipped.Clip,
    body: MarkdownContent,
) = Clipped(clip, body.children).wrappedAsValue()

/**
 * Inserts content in a box.
 * @param title box title. The box is untitled if it is `null`
 * @param backgroundColor background color. If `null`, the box uses the default color.
 * @param foregroundColor foreground (text) color. If `null`, the box uses the default color.
 * @param body box content
 * @return the new box node
 */
fun box(
    title: MarkdownContent? = null,
    @Name("background") backgroundColor: Color? = null,
    @Name("foreground") foregroundColor: Color? = null,
    body: MarkdownContent,
) = Box(title?.children, backgroundColor, foregroundColor, body.children).wrappedAsValue()

/**
 * Creates a table out of a collection of columns.
 *
 * The following example joins 5 columns:
 * ```
 * .table
 *     .foreach {1..5}
 *         | Header .1 |
 *         |-----------|
 *         |  Cell .1  |
 * ```
 *
 * @param subTables independent tables (as Markdown sources) that will be parsed and joined together into a single table
 * @return a new [Table] node
 */
fun table(
    @Injected context: Context,
    subTables: Iterable<Value<String>>,
): NodeValue {
    val columns =
        subTables.asSequence()
            .map { it.unwrappedValue }
            .map { ValueFactory.blockMarkdown(it, context).unwrappedValue }
            .map { it.children.first() }
            .filterIsInstance<Table>()
            .flatMap { it.columns }

    return Table(columns.toList()).wrappedAsValue()
}
