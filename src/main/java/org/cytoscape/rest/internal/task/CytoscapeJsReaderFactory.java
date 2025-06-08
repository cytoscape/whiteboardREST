package org.cytoscape.rest.internal.task;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;

@Target({ FIELD }) @Retention(RUNTIME)
public @interface CytoscapeJsReaderFactory {}
