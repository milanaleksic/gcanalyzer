package net.milanaleksic.gcanalyzer.parser

import java.util.regex.Pattern
import net.milanaleksic.gcanalyzer.Utils

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:33 AM
 */
class GCLogParser {

    def private static final statisticDetails = $/
                (\[                      # 22/1 - [helper group]
                     (                   # 23/2 - [helper group]
                         (\w+)\:\s       # 24/3 (GROUP_STATS_GC_NAME) - GC name
                         ([\dKMG]+)->    # 25/4 (GROUP_STATS_GC_START_VALUE) - GC start value
                         ([\dKMG]+)\(    # 26/5 (GROUP_STATS_GC_END_VALUE) - GC end value
                         ([\dKMG]+)\)    # 27/6 (GROUP_STATS_GC_MAX_VALUE) - memory segment Max size
                     )+
                 \]\s?)
                |
                (                        # 28/7 - [helper group]
                    ([\dKMG]+)->         # 29/8 (GROUP_STATS_GC_COMPLETE_START_VALUE) - non-PermGen start value
                    ([\dKMG]+)\(         # 30/9 (GROUP_STATS_GC_COMPLETE_END_VALUE) - non-PermGen end value
                    ([\dKMG]+)\)\s?      # 31/10 (GROUP_STATS_GC_COMPLETE_MAX_VALUE) - non-PermGen Max size
                )
    /$

    def private static final timingDetails = $/
                (\w+)=                   # 35/1 (GROUP_TIMINGS_TITLE) - timing name (user, sys, real)
                ([\d\.]+)                # 36/2 (GROUP_TIMINGS_VALUE) - timing value (user, sys, real)
    /$

    private static final Pattern completeLineRegEx = Pattern.compile($/
        (\d{4})-(\d{2})-(\d{2})T         # 1, 2, 3 (GROUP_MAIN_DATE_YEAR, GROUP_MAIN_DATE_MONTH, GROUP_MAIN_DATE_DATE) - date parts
        (\d{2}):(\d{2}):(\d{2})\.(\d{3}) # 4, 5, 6, 7 (GROUP_MAIN_DATE_HOUR, GROUP_MAIN_DATE_MIN, GROUP_MAIN_DATE_SEC, GROUP_MAIN_DATE_MILLIS) - time parts
        \+\d{4}:\s
        (\d+\.\d+):\s                    # 8 (GROUP_MAIN_TIME_SINCE_PROGRAM_START) - time in seconds from program start
        \[
		(                                # 9 (GROUP_MAIN_EVENT_NAME) - Garbage collection event
			([Ful\s]+)?                  # 10 - [helper group]
			GC\s?
			(\(System\))?                # 11 - [helper group]
		)
		(                                # 12 (GROUP_MAIN_SURVIVOR_SUBGROUP) - [sub-group]
			[^\d]+
			(\d+)                        # 13 (GROUP_MAIN_SURVIVOR_DESIRED_SIZE) - desired survivor size
			[^\d]+
			(\d+)                        # 14 (GROUP_MAIN_SURVIVOR_THRESHOLD_NEW) - new threshold
			[^\(]+
			\(max\s
			(\d+)                        # 15 (GROUP_MAIN_SURVIVOR_THRESHOLD_MAX) - max threshold
			(                            # 16 - [helper group]
				[^\d\[]+
				(                        # 17 - [helper group]
					(\d+)(?=\stotal\s)   # 18 (GROUP_MAIN_SURVIVOR_TOTAL) - total survivor occupancy in the non-empty survivor space
				)?
				(\d+)?                   # 19 - [helper group]
			)+
		)?\s
        (                                # 20 (GROUP_MAIN_STATISTICS_SUBGROUP) - [sub-group]
            (                            # 21 - [helper group]
                $statisticDetails
            )+
        )
        ,\s([\d\.]+)\ssecs\]             # 32 (GROUP_MAIN_TIMING_TOTAL) - total garbage collection event time
        \s?
        (\[Times:\s                      # 33 (GROUP_MAIN_TIMING_SUBGROUP) - [sub-group]
            (                            # 34 - [helper group]
                $timingDetails
            ,?\s)+
        secs\]\s?)?
    /$, Pattern.COMMENTS)

    private static final Pattern statisticDetailsRegEx = Pattern.compile(statisticDetails, Pattern.COMMENTS)

    private static final Pattern timingDetailsRegEx = Pattern.compile(timingDetails, Pattern.COMMENTS)

    GCEvents parse(URL url) {
        String remoteFileContent
        InputStream remoteFileStream
        try {
            remoteFileStream = url.openStream()
            remoteFileContent = remoteFileStream.text
        } finally {
            if (remoteFileStream)
                remoteFileStream.close()
        }
        parse(remoteFileContent)
    }

    GCEvents parse(File file) {
        parse(file.text)
    }

    GCEvents parse(String text) {
        HashMap<Date, GCEvent> hashMapOnDate = new LinkedHashMap<Date, GCEvent>()
        HashMap<Long, GCEvent> hashMapOnMillis = new LinkedHashMap<Long, GCEvent>()
        text.eachLine { line ->
            processLine(line, hashMapOnDate, hashMapOnMillis)
        }
        //TODO: use unmodifiable maps
        return new GCEvents(hashMapOnDate: hashMapOnDate, hashMapOnMillis: hashMapOnMillis)
    }

    private static final int GROUP_MAIN_DATE_YEAR = 1
    private static final int GROUP_MAIN_DATE_MONTH = 2
    private static final int GROUP_MAIN_DATE_DATE = 3
    private static final int GROUP_MAIN_DATE_HOUR = 4
    private static final int GROUP_MAIN_DATE_MIN = 5
    private static final int GROUP_MAIN_DATE_SEC = 6
    private static final int GROUP_MAIN_DATE_MILLIS = 7

    private static final int GROUP_MAIN_TIME_SINCE_PROGRAM_START = 8
    private static final int GROUP_MAIN_EVENT_NAME = 9

    private static final int GROUP_MAIN_SURVIVOR_SUBGROUP = 12
    private static final int GROUP_MAIN_SURVIVOR_DESIRED_SIZE = 13
	private static final int GROUP_MAIN_SURVIVOR_THRESHOLD_NEW = 14
	private static final int GROUP_MAIN_SURVIVOR_THRESHOLD_MAX = 15
	private static final int GROUP_MAIN_SURVIVOR_TOTAL = 18

    private static final int GROUP_MAIN_STATISTICS_SUBGROUP = 20

    private static final int GROUP_MAIN_TIMING_TOTAL = 32
    private static final int GROUP_MAIN_TIMING_SUBGROUP = 33

    void processLine(String line, HashMap<Date, GCEvent> hashMapOnDate, HashMap<Long, GCEvent> hashMapOnMillis) {
        def matcher = (line =~ completeLineRegEx)
        if (matcher.find()) {
            def calendar = Calendar.getInstance()
            calendar.set(
                    matcher.group(GROUP_MAIN_DATE_YEAR) as int,
                    (matcher.group(GROUP_MAIN_DATE_MONTH) as int) - 1,
                    matcher.group(GROUP_MAIN_DATE_DATE) as int,
                    matcher.group(GROUP_MAIN_DATE_HOUR) as int,
                    matcher.group(GROUP_MAIN_DATE_MIN) as int,
                    matcher.group(GROUP_MAIN_DATE_SEC) as int)
            calendar.set(Calendar.MILLISECOND, matcher.group(GROUP_MAIN_DATE_MILLIS) as int)
            Date time = calendar.getTime()

            long timeMs = new BigDecimal(matcher.group(GROUP_MAIN_TIME_SINCE_PROGRAM_START)) * 1000
            def gcEventName = matcher.group(GROUP_MAIN_EVENT_NAME)

            def survivorDetails = null
            if (matcher.group(GROUP_MAIN_SURVIVOR_SUBGROUP)) {
                survivorDetails = new GCSurvivorDetails(
                    desiredSize: Long.parseLong(matcher.group(GROUP_MAIN_SURVIVOR_DESIRED_SIZE)),
                    newThreshold: Integer.parseInt(matcher.group(GROUP_MAIN_SURVIVOR_THRESHOLD_NEW)),
                    maxThreshold: Integer.parseInt(matcher.group(GROUP_MAIN_SURVIVOR_THRESHOLD_MAX)),
                    endingTotalSize: Long.parseLong(matcher.group(GROUP_MAIN_SURVIVOR_TOTAL)))
            }

            def userTiming = null, sysTiming = null, realTiming = null
            Map<String, SingleGCStatistic> stats = extractStatisticsData(matcher.group(GROUP_MAIN_STATISTICS_SUBGROUP))
            def timingsString = matcher.group(GROUP_MAIN_TIMING_SUBGROUP)
            if (timingsString) {
                Map<String, Long> timings = extractTimings(timingsString)
                userTiming = timings['user']
                sysTiming = timings['sys']
                realTiming = timings['real']
                assert userTiming != null && sysTiming != null && realTiming != null
            }

            long completeEventTimeInMicroSeconds = new BigDecimal(matcher.group(GROUP_MAIN_TIMING_TOTAL)) * 1000 * 1000

            def event = new GCEvent(time: time, timeInMillis: timeMs,
                    gcEventName: gcEventName, stats: Collections.unmodifiableMap(stats),
                    survivorDetails: survivorDetails,
                    userTiming: userTiming, sysTiming: sysTiming, realTiming: realTiming,
                    completeEventTimeInMicroSeconds: completeEventTimeInMicroSeconds
            )

            hashMapOnDate[time] = hashMapOnMillis[timeMs] = event

        } else {
            throw new IllegalArgumentException("Not matched garbage collection log line: $line")
        }
    }

    private static final int GROUP_STATS_GC_NAME = 3
    private static final int GROUP_STATS_GC_START_VALUE = 4
    private static final int GROUP_STATS_GC_END_VALUE = 5
    private static final int GROUP_STATS_GC_MAX_VALUE = 6
    private static final int GROUP_STATS_GC_COMPLETE_START_VALUE = 8
    private static final int GROUP_STATS_GC_COMPLETE_END_VALUE = 9
    private static final int GROUP_STATS_GC_COMPLETE_MAX_VALUE = 10

    private Map<String, SingleGCStatistic> extractStatisticsData(String statisticsString) {
        if (!statisticsString)
            throw new RuntimeException("Statistics data sub-group must not be null")
        Map<String, SingleGCStatistic> stats = [:]
        def statisticsMatcher = statisticDetailsRegEx.matcher(statisticsString)
        while (statisticsMatcher.find()) {
            String gcName = statisticsMatcher.group(GROUP_STATS_GC_NAME)
            stats[gcName] = new SingleGCStatistic(gcName: gcName,
                    startValueInB: Utils.convertMemoryValueStringToLong(
                            statisticsMatcher.group(gcName ?
                                GROUP_STATS_GC_START_VALUE :
                                GROUP_STATS_GC_COMPLETE_START_VALUE)),
                    endValueInB: Utils.convertMemoryValueStringToLong(
                            statisticsMatcher.group(gcName ?
                                GROUP_STATS_GC_END_VALUE :
                                GROUP_STATS_GC_COMPLETE_END_VALUE)),
                    maxValueInB: Utils.convertMemoryValueStringToLong(
                            statisticsMatcher.group(gcName ?
                                GROUP_STATS_GC_MAX_VALUE :
                                GROUP_STATS_GC_COMPLETE_MAX_VALUE))
            )
        }
        return stats
    }

    private static final int GROUP_TIMINGS_TITLE = 1
    private static final int GROUP_TIMINGS_VALUE = 2

    private Map<String, Long> extractTimings(String timingString) {
        Map<String, Long> stats = [:]
        def timingsMatcher = timingDetailsRegEx.matcher(timingString)
        while (timingsMatcher.find()) {
            stats[timingsMatcher.group(GROUP_TIMINGS_TITLE)] = new BigDecimal(timingsMatcher.group(GROUP_TIMINGS_VALUE))*1000
        }
        return stats
    }

}