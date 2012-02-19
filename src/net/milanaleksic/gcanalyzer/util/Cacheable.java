package net.milanaleksic.gcanalyzer.util;

import java.lang.annotation.*;

/**
 * User: Milan Aleksic
 * Date: 2/19/12
 * Time: 5:48 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable {
}
