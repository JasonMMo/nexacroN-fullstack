package com.nexacro.fullstack.business.uiadapter;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import com.nexacro.fullstack.business.xapi.NexacroEnvelope;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Abstract base class for Nexacro {@code @RestController}s.
 *
 * <p>Provides:
 * <ul>
 *   <li>A shared {@link ExceptionHandler} that converts any unhandled
 *       {@link Exception} into a standard error {@link NexacroEnvelope}.</li>
 *   <li>Convenience helpers for looking up datasets and parameters by id
 *       from an inbound {@link NexacroEnvelope}.</li>
 * </ul>
 *
 * <p>Concrete subclasses carry the {@code @RestController} annotation
 * and define their own {@code @RequestMapping} methods.
 */
public abstract class NexacroController {

    // -----------------------------------------------------------------------
    // Exception handling
    // -----------------------------------------------------------------------

    /**
     * Wrap any unhandled exception into a Nexacro error envelope.
     *
     * @param e the exception that escaped a handler method
     * @return error envelope with {@code ErrorCode=-1} and the exception message
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public NexacroEnvelope handleException(Exception e) {
        return NexacroResponseBuilder.errorFromException(e);
    }

    // -----------------------------------------------------------------------
    // Lookup helpers
    // -----------------------------------------------------------------------

    /**
     * Find an input dataset by its {@code id} within the given envelope.
     *
     * @param env the inbound request envelope; may be {@code null}
     * @param id  dataset identifier
     * @return the first matching {@link NexacroDataset}, or {@code null} if not found
     */
    protected NexacroDataset datasetById(NexacroEnvelope env, String id) {
        if (env == null || env.getDatasets() == null) return null;
        for (NexacroDataset ds : env.getDatasets()) {
            if (id.equals(ds.getId())) return ds;
        }
        return null;
    }

    /**
     * Find a parameter value by its {@code id} within the given envelope.
     *
     * @param env the inbound request envelope; may be {@code null}
     * @param id  parameter identifier
     * @return the raw value {@link Object}, or {@code null} if not found
     */
    protected Object parameterById(NexacroEnvelope env, String id) {
        if (env == null || env.getParameters() == null) return null;
        for (NexacroEnvelope.Parameter p : env.getParameters()) {
            if (id.equals(p.getId())) return p.getValue();
        }
        return null;
    }
}
