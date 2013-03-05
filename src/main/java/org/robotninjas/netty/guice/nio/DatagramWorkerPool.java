package org.robotninjas.netty.guice.nio;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

@com.google.inject.BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, METHOD, PARAMETER})
@interface DatagramWorkerPool {
}
