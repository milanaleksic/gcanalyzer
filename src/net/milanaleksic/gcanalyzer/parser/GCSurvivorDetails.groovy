package net.milanaleksic.gcanalyzer.parser

import groovy.transform.Immutable
import javax.annotation.Nullable
import javax.annotation.Nonnull

/**
 * Created by IntelliJ IDEA.
 * User: b25791
 * Date: 2/15/12
 * Time: 3:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Immutable
class GCSurvivorDetails {

    @Nonnull
    int newThreshold

    @Nonnull
    int maxThreshold

    @Nonnull
    Long desiredSize

    @Nullable
    Long endingTotalSize

}
