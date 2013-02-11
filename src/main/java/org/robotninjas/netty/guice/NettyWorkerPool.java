package org.robotninjas.netty.guice;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@com.google.inject.BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, METHOD, PARAMETER})
public @interface NettyWorkerPool {
}
