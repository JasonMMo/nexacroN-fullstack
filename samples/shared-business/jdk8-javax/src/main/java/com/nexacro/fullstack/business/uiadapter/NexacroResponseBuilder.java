package com.nexacro.fullstack.business.uiadapter;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import com.nexacro.fullstack.business.xapi.NexacroEnvelope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Static factory methods for building standard {@link NexacroEnvelope} responses.
 *
 * <p>Nexacro protocol convention:
 * <ul>
 *   <li>Success: {@code ErrorCode=0, ErrorMsg=""}</li>
 *   <li>Error:   {@code ErrorCode=<negative int>, ErrorMsg="<message>"}</li>
 * </ul>
 */
public final class NexacroResponseBuilder {

    private NexacroResponseBuilder() {
        // non-instantiable utility class
    }

    // -----------------------------------------------------------------------
    // Success factories
    // -----------------------------------------------------------------------

    /**
     * Build a success envelope with the given datasets (varargs).
     *
     * @param datasets zero or more datasets to include
     * @return envelope with {@code ErrorCode=0, ErrorMsg=""}
     */
    public static NexacroEnvelope ok(NexacroDataset... datasets) {
        List<NexacroDataset> list = (datasets == null || datasets.length == 0)
                ? new ArrayList<NexacroDataset>()
                : new ArrayList<NexacroDataset>(Arrays.asList(datasets));
        return buildOk(list);
    }

    /**
     * Build a success envelope with the given dataset list.
     *
     * @param datasets list of datasets; {@code null} is treated as empty
     * @return envelope with {@code ErrorCode=0, ErrorMsg=""}
     */
    public static NexacroEnvelope ok(List<NexacroDataset> datasets) {
        List<NexacroDataset> list = (datasets != null) ? datasets : new ArrayList<NexacroDataset>();
        return buildOk(list);
    }

    // -----------------------------------------------------------------------
    // Error factories
    // -----------------------------------------------------------------------

    /**
     * Build an error envelope with no datasets.
     *
     * @param code    error code (typically a negative integer)
     * @param message human-readable error message
     * @return envelope with {@code ErrorCode=code, ErrorMsg=message, Datasets=[]}
     */
    public static NexacroEnvelope error(int code, String message) {
        List<NexacroEnvelope.Parameter> params = new ArrayList<NexacroEnvelope.Parameter>(
                Arrays.asList(
                        new NexacroEnvelope.Parameter("ErrorCode", code, null),
                        new NexacroEnvelope.Parameter("ErrorMsg",
                                message != null ? message : "", null)
                )
        );
        return new NexacroEnvelope("1.0", params, new ArrayList<NexacroDataset>());
    }

    /**
     * Build an error envelope from an exception.
     *
     * <p>Uses {@code -1} as the error code and the exception message (or simple
     * class name if {@link Throwable#getMessage()} returns {@code null}).
     *
     * @param t the throwable to wrap
     * @return error envelope
     */
    public static NexacroEnvelope errorFromException(Throwable t) {
        String msg = (t.getMessage() != null)
                ? t.getMessage()
                : t.getClass().getSimpleName();
        return error(-1, msg);
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private static NexacroEnvelope buildOk(List<NexacroDataset> datasets) {
        List<NexacroEnvelope.Parameter> params = new ArrayList<NexacroEnvelope.Parameter>(
                Arrays.asList(
                        new NexacroEnvelope.Parameter("ErrorCode", 0, null),
                        new NexacroEnvelope.Parameter("ErrorMsg", "", null)
                )
        );
        return new NexacroEnvelope("1.0", params, datasets);
    }
}
