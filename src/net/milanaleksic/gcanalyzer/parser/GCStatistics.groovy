package net.milanaleksic.gcanalyzer.parser

/**
 * User: Milan Aleksic
 * Date: 2/20/12
 * Time: 11:17 AM
 */
@groovy.transform.Immutable
final class GCStatistics {

    GCStatistic youngGeneration

    GCStatistic oldGeneration

    GCStatistic permanentGeneration

    GCStatistic heapWithoutPermGen

}
