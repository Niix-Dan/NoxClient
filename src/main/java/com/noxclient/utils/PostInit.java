/*
 * This file is part of the Nox Client.
 * Made by Niix#8237
 */

package com.noxclient.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostInit {
    Class<?>[] dependencies() default { };
}
