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
    Long desiredSizeInB

    @Nullable
    Long endingTotalSizeInB

    @Nonnull
    Long desiredSizeInKB

    @Nullable
    Long endingTotalSizeInKB

    @Newify([GCSurvivorDetails])
    public static GCSurvivorDetails create(int newThreshold, int maxThreshold, Long desiredSize, Long endingTotalSize) {
        GCSurvivorDetails(
                newThreshold: newThreshold, maxThreshold: maxThreshold,
                desiredSizeInB: desiredSize, endingTotalSizeInB: endingTotalSize,
                desiredSizeInKB: desiredSize >> 10, endingTotalSizeInKB: endingTotalSize == null ? null : endingTotalSize >> 10
        )
    }

}
