package net.milanaleksic.gcanalyzer.parser

import groovy.transform.Immutable
import javax.annotation.Nullable
import javax.annotation.Nonnull

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
