package net.milanaleksic.gcanalyzer.parser

import javax.annotation.Nullable
import javax.annotation.Nonnull

@groovy.transform.Immutable
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
