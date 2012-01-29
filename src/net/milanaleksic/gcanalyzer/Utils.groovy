package net.milanaleksic.gcanalyzer

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 12:26 PM
 */
class Utils {
    static long convertMemoryValueStringToLong(String value) {
        if (value.endsWith('K'))
            return Long.parseLong(value.substring(0, value.size()-1), 10) << 10
        else if (value.endsWith('M'))
            return Long.parseLong(value.substring(0, value.size()-1), 10) << 20
        else if (value.endsWith('G'))
            return Long.parseLong(value.substring(0, value.size()-1), 10) << 30
        else
            return Long.parseLong(value, 10)
    }
}
