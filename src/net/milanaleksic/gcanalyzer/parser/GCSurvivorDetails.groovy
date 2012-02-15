package net.milanaleksic.gcanalyzer.parser

import groovy.transform.Immutable

/**
 * Created by IntelliJ IDEA.
 * User: b25791
 * Date: 2/15/12
 * Time: 3:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Immutable
class GCSurvivorDetails {

    long desiredSize

    long endingTotalSize

    int newThreshold

    int maxThreshold

}
