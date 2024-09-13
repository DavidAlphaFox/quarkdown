package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.BAD_FUNCTION_CALL_EXIT_CODE
import eu.iamgio.quarkdown.function.asString
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.call.asString

/**
 * An exception thrown if a [FunctionCall] could not be executed.
 * @param call the invalid call
 * @param reason optional additional reason the call failed for
 */
open class InvalidFunctionCallException(val call: FunctionCall<*>, reason: String? = null) :
    FunctionException(
        "Cannot call function ${call.function.asString()} with arguments ${call.arguments.asString()}" +
            (reason?.let { ": $it" } ?: ""),
        code = BAD_FUNCTION_CALL_EXIT_CODE,
        function = call.function,
    )
