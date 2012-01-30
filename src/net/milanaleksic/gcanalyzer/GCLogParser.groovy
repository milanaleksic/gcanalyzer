package net.milanaleksic.gcanalyzer

import java.util.regex.Pattern

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:33 AM
 */
class GCLogParser {

    def private static final statisticDetails = $/
    (\[                                  # 12/1 - [helper group]
         (                               # 13/2 - [helper group]
             (\w+)\:\s                   # 14/3 - GC name
             ([\dKMG]+)->                # 15/4 - GC start value
             ([\dKMG]+)\(                # 16/5 - GC end value
             ([\dKMG]+)\)                # 17/6 - memory segment Max size
         )+
     \]\s?)
    |
    (                                    # 18/7 - [helper group]
        ([\dKMG]+)->                     # 19/8 - non-PermGen start value
        ([\dKMG]+)\(                     # 20/9 - non-PermGen end value
        ([\dKMG]+)\)\s?                  # 21/10 - non-PermGen Max size
    )
    /$

    def private static final timingDetails = $/
        (\w+)=                           # 25/1 - timing name (user, sys, real)
        ([\d\.]+)                        # 26/2 - timing value (user, sys, real)
    /$

    private static final Pattern completeLineRegEx = Pattern.compile($/
        (\d{4})-(\d{2})-(\d{2})T         # 1, 2, 3 - date parts
        (\d{2}):(\d{2}):(\d{2})\.(\d{3}) # 4, 5, 6, 7 - time parts
        \+\d{4}:\s                       # [noise]
        (\d+\.\d+):\s                    # 8 - time in seconds from program start
        \[([^\[]+)\s                     # 9 - Garbage collection event
        (                                # 10 - [helper group]
            (                            # 11 - [helper group]
                $statisticDetails
            )+
        )
        ,\s([\d\.]+)\ssecs\]             # 22 - total garbage collection event time
        \s?
        (\[Times:\s                      # 23 - [helper group]
            (                            # 24 - helper group
                $timingDetails
            ,?\s)+
        secs\]\s?)?
    /$, Pattern.COMMENTS)

    private static final Pattern statisticDetailsRegEx = Pattern.compile(statisticDetails, Pattern.COMMENTS)

    private static final Pattern timingDetailsRegEx = Pattern.compile(timingDetails, Pattern.COMMENTS)

    GCEvents parse(File file) {
        parse(file.text)
    }

    GCEvents parse(String text) {
        HashMap<Date, GCEvent> hashMapOnDate = new HashMap<Date, GCEvent>()
        HashMap<Long, GCEvent> hashMapOnMillis = new HashMap<Long, GCEvent>()
        text.eachLine { line ->
            processLine(line, hashMapOnDate, hashMapOnMillis)
        }
        //TODO: use unmodifiable maps
        return new GCEvents(hashMapOnDate: hashMapOnDate, hashMapOnMillis: hashMapOnMillis)
    }

    void processLine(String line, HashMap<Date, GCEvent> hashMapOnDate, HashMap<Long, GCEvent> hashMapOnMillis) {
        def matcher = (line =~ completeLineRegEx)
        if (matcher.find()) {
            def calendar = Calendar.getInstance()
            calendar.set(matcher.group(1) as int, (matcher.group(2) as int) - 1, matcher.group(3) as int,
                matcher.group(4) as int, matcher.group(5) as int, matcher.group(6) as int)
            calendar.set(Calendar.MILLISECOND, matcher.group(7) as int)
            Date time = calendar.getTime()

            long timeMs = new BigDecimal(matcher.group(8)) * 1000
            def gcEventName = matcher.group(9)

            def userTiming = null, sysTiming = null, realTiming = null
            Map<String, SingleGCStatistic> stats = extractStatisticsData(matcher.group(10))
            def timingsString = matcher.group(23)
            if (timingsString) {
                Map<String, Long> timings = extractTimings(matcher.group(23))
                userTiming = timings['user']
                sysTiming = timings['sys']
                realTiming = timings['real']
                assert userTiming != null && sysTiming != null && realTiming != null
            }

            long completeEventTimeInMicroSeconds = new BigDecimal(matcher.group(22)) * 1000 * 1000

            def event = new GCEvent(time: time, timeInMillis: timeMs,
                    gcEventName: gcEventName, stats: Collections.unmodifiableMap(stats),
                    userTiming: userTiming, sysTiming: sysTiming, realTiming: realTiming,
                    completeEventTimeInMicroSeconds: completeEventTimeInMicroSeconds
            )

            hashMapOnDate[time] = hashMapOnMillis[timeMs] = event

        } else {
            throw new IllegalArgumentException("Not matched garbage collection log line: $line")
        }
    }

    private Map<String, SingleGCStatistic> extractStatisticsData(String statisticsString) {
        Map<String, SingleGCStatistic> stats = [:]
        def statisticsMatcher = statisticDetailsRegEx.matcher(statisticsString)
        while (statisticsMatcher.find()) {
            String gcName = statisticsMatcher.group(3)
            stats[gcName] = new SingleGCStatistic(gcName: gcName,
                    startValueInB: Utils.convertMemoryValueStringToLong(statisticsMatcher.group(gcName ? 4 : 8)),
                    endValueInB: Utils.convertMemoryValueStringToLong(statisticsMatcher.group(gcName ? 5 : 9)),
                    maxValueInB: Utils.convertMemoryValueStringToLong(statisticsMatcher.group(gcName ? 6 : 10))
            )
        }
        return stats
    }

    private Map<String, Long> extractTimings(String timingString) {
        Map<String, Long> stats = [:]
        def timingsMatcher = timingDetailsRegEx.matcher(timingString)
        while (timingsMatcher.find()) {
            stats[timingsMatcher.group(1)] = new BigDecimal(timingsMatcher.group(2))*1000
        }
        return stats
    }

}