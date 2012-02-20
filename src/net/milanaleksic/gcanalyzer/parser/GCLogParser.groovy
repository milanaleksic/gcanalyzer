package net.milanaleksic.gcanalyzer.parser

import java.util.regex.Pattern
import net.milanaleksic.gcanalyzer.util.Utils

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:33 AM
 */
class GCLogParser {



    def private static final statisticDetails = $/
                (\[                      # 23/1 - [helper group]
                     (                   # 24/2 - [helper group]
                         (\w+)\s?:\s     # 25/3 (GROUP_STATS_GC_NAME) - GC name
                         ([\dKMG]+)->    # 26/4 (GROUP_STATS_GC_START_VALUE) - GC start value
                         ([\dKMG]+)\(    # 27/5 (GROUP_STATS_GC_END_VALUE) - GC end value
                         ([\dKMG]+)\)    # 28/6 (GROUP_STATS_GC_MAX_VALUE) - memory segment Max size
                     )+
					 (,\s                # 29/7 - [helper group]
					     ([\d\.]+)       # 30/8 - [helper group]
				     \ssecs)?
                 \]\s?)
                |
                (                        # 31/9 - [helper group]
                    ([\dKMG]+)->         # 32/10 (GROUP_STATS_GC_COMPLETE_START_VALUE) - non-PermGen start value
                    ([\dKMG]+)\(         # 33/11 (GROUP_STATS_GC_COMPLETE_END_VALUE) - non-PermGen end value
                    ([\dKMG]+)\),?\s?    # 34/12 (GROUP_STATS_GC_COMPLETE_MAX_VALUE) - non-PermGen Max size
                )
    /$

    def private static final timingDetails = $/
                (\w+)=                   # 38/1 (GROUP_TIMINGS_TITLE) - timing name (user, sys, real)
                ([\d\.]+)                # 39/2 (GROUP_TIMINGS_VALUE) - timing value (user, sys, real)
    /$

    private static final Pattern completeLineRegEx = Pattern.compile($/
        (\d{4})-(\d{2})-(\d{2})T         # 1, 2, 3 (GROUP_MAIN_DATE_YEAR, GROUP_MAIN_DATE_MONTH, GROUP_MAIN_DATE_DATE) - date parts
        (\d{2}):(\d{2}):(\d{2})\.(\d{3}) # 4, 5, 6, 7 (GROUP_MAIN_DATE_HOUR, GROUP_MAIN_DATE_MIN, GROUP_MAIN_DATE_SEC, GROUP_MAIN_DATE_MILLIS) - time parts
        \+\d{4}:\s
        (\d+\.\d+):\s\[                  # 8 (GROUP_MAIN_TIME_SINCE_PROGRAM_START) - time in seconds from program start
		(                                # 9 (GROUP_MAIN_EVENT_NAME) - Garbage collection event
			([Ful\s]+)?                  # 10 - [helper group]
			GC
			(\s?\(System\))?             # 11 - [helper group]
		)\s?
		(\d+\.\d+:\s?)?                  # 12 - [helper group]
		(                                # 13 (GROUP_MAIN_SURVIVOR_SUBGROUP) - [sub-group]
			[^\d]+
			(\d+)                        # 14 (GROUP_MAIN_SURVIVOR_DESIRED_SIZE) - desired survivor size
			[^\d]+
			(\d+)                        # 15 (GROUP_MAIN_SURVIVOR_THRESHOLD_NEW) - new threshold
			[^\(]+
			\(max\s
			(\d+)                        # 16 (GROUP_MAIN_SURVIVOR_THRESHOLD_MAX) - max threshold
			(                            # 17 - [helper group]
				[^\d\[]+
				(                        # 18 - [helper group]
					(\d+)(?=\stotal\s)   # 19 (GROUP_MAIN_SURVIVOR_TOTAL) - total survivor occupancy in the non-empty survivor space
				)?
				(\d+)?                   # 20 - [helper group]
			)+
		)?\s
        (                                # 21 (GROUP_MAIN_STATISTICS_SUBGROUP) - [sub-group]
            (                            # 22 - [helper group]
                $statisticDetails
            )+
        )
        ,\s([\d\.]+)\ssecs\]             # 35 (GROUP_MAIN_TIMING_TOTAL) - total garbage collection event time
        \s?
        (\[Times:\s                      # 36 (GROUP_MAIN_TIMING_SUBGROUP) - [sub-group]
            (                            # 37 - [helper group]
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
        LinkedList<GCEvent> linkedList = new LinkedList<GCEvent>()

        StringBuilder lineBuilder = new StringBuilder()
        StringReader reader = new StringReader(text)
        String line
        while (line = reader.readLine()) {
            boolean isProperEnding = lineIsProperEndingOfGCRecord(line)
            if (isProperEnding && lineBuilder.size()>0) {
                processLine(lineBuilder.append(line).toString(), linkedList)
                lineBuilder = new StringBuilder()
            } else if (isProperEnding) {
                processLine(line, linkedList)
            }
            else {
                if (line == 'Heap') {
                    break; // we don't want to analyze footer of the GC log if one exists.
                }
                lineBuilder.append(line)
            }
        }

        //TODO: use unmodifiable maps
        return new GCEvents(linkedList)
    }

    private boolean lineIsProperEndingOfGCRecord(String line) {
        return line.endsWith(']') || line.endsWith('] ')
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

    private static final int GROUP_MAIN_SURVIVOR_SUBGROUP = 13
    private static final int GROUP_MAIN_SURVIVOR_DESIRED_SIZE = 14
	private static final int GROUP_MAIN_SURVIVOR_THRESHOLD_NEW = 15
	private static final int GROUP_MAIN_SURVIVOR_THRESHOLD_MAX = 16
	private static final int GROUP_MAIN_SURVIVOR_TOTAL = 19

    private static final int GROUP_MAIN_STATISTICS_SUBGROUP = 21

    private static final int GROUP_MAIN_TIMING_TOTAL = 35
    private static final int GROUP_MAIN_TIMING_SUBGROUP = 36

    private void processLine(String line, LinkedList<GCEvent> linkedList) {
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
                String totalSurvivorSize = matcher.group(GROUP_MAIN_SURVIVOR_TOTAL)
                survivorDetails = new GCSurvivorDetails(
                    desiredSize: Long.parseLong(matcher.group(GROUP_MAIN_SURVIVOR_DESIRED_SIZE)),
                    newThreshold: Integer.parseInt(matcher.group(GROUP_MAIN_SURVIVOR_THRESHOLD_NEW)),
                    maxThreshold: Integer.parseInt(matcher.group(GROUP_MAIN_SURVIVOR_THRESHOLD_MAX)),
                    endingTotalSize: totalSurvivorSize ? Long.parseLong(totalSurvivorSize) : null)
            }

            def userTiming = null, sysTiming = null, realTiming = null
            def timingsString = matcher.group(GROUP_MAIN_TIMING_SUBGROUP)
            if (timingsString) {
                Map<String, Long> timings = extractTimings(timingsString)
                userTiming = timings['user']
                sysTiming = timings['sys']
                realTiming = timings['real']
                assert userTiming != null && sysTiming != null && realTiming != null
            }

            long completeEventTimeInMicroSeconds = new BigDecimal(matcher.group(GROUP_MAIN_TIMING_TOTAL)) * 1000 * 1000
            GCStatistics stats = convertMapToGCStatistics(extractStatisticsData(matcher.group(GROUP_MAIN_STATISTICS_SUBGROUP)))

            def event = new GCEvent(moment: time, momentInMillis: timeMs,
                    gcEventName: gcEventName, stats: stats,
                    survivorDetails: survivorDetails,
                    userTiming: userTiming, sysTiming: sysTiming, realTiming: realTiming,
                    completeEventTimeInMicroSeconds: completeEventTimeInMicroSeconds
            )

            linkedList.add(event)

        } else {
            throw new IllegalArgumentException("Not matched garbage collection log line: $line")
        }
    }

    private GCStatistics convertMapToGCStatistics(Map<String, GCStatistic> stats) {
        GCStatistic youngGeneration = stats.find { stat -> GCType.YOUNG.knownMappings().any {it == stat.key} }?.value
        GCStatistic oldGeneration = stats.find { stat -> GCType.OLD.knownMappings().any {it == stat.key} }?.value
        GCStatistic permanentGeneration = stats.find { stat -> GCType.PERM.knownMappings().any {it == stat.key} }?.value
        GCStatistic heapWithoutPermGen = stats[null]
        assert heapWithoutPermGen : "Heap size information must be present in each GC event $stats"
        return new GCStatistics(
                youngGeneration: youngGeneration, oldGeneration: oldGeneration,
                permanentGeneration: permanentGeneration, heapWithoutPermGen: heapWithoutPermGen
        )
    }

    private static final int GROUP_STATS_GC_NAME = 3
    private static final int GROUP_STATS_GC_START_VALUE = 4
    private static final int GROUP_STATS_GC_END_VALUE = 5
    private static final int GROUP_STATS_GC_MAX_VALUE = 6
    private static final int GROUP_STATS_GC_COMPLETE_START_VALUE = 10
    private static final int GROUP_STATS_GC_COMPLETE_END_VALUE = 11
    private static final int GROUP_STATS_GC_COMPLETE_MAX_VALUE = 12

    private Map<String, GCStatistic> extractStatisticsData(String statisticsString) {
        if (!statisticsString)
            throw new RuntimeException("Statistics data sub-group must not be null")
        Map<String, GCStatistic> stats = [:]
        def statisticsMatcher = statisticDetailsRegEx.matcher(statisticsString)
        while (statisticsMatcher.find()) {
            String gcName = statisticsMatcher.group(GROUP_STATS_GC_NAME)
            stats[gcName] = new GCStatistic(gcName: gcName,
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