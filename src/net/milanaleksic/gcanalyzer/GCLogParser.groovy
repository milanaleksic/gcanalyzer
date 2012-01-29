package net.milanaleksic.gcanalyzer

import java.util.regex.Pattern

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:33 AM
 */
class GCLogParser {

    def private static final lineRegEx = Pattern.compile(
 /(\d{4})-(\d{2})-(\d{2})T/ +        // 1, 2, 3 - date parts
 /(\d{2}):(\d{2}):(\d{2})\.(\d{3})/+ // 4, 5, 6, 7 - time parts
 /\+\d{4}:\s/ +                      // [noise]
 /(\d+\.\d+):\s/ +                   // 8 - time in seconds from program start
 /\[([^\[]+)\s/ +                    // 9 - Garbage collection event
 /(/ +                               // 10 - [helper group, don't use]
    /(\[/ +                          // 11 - [helper group, don't use]
        /(/ +                        // 12 - [helper group, don't use]
			/(\w+)\:\s/ +            // 13 - GC name
			/([\dKMG]+)->/ +         // 14 - GC start value
			/([\dKMG]+)\(/ +         // 15 - GC end value
			/([\dKMG]+)\)/ +         // 16 - memory segment Max size
		/)+/ +
    /\]\s?)/ +
	/|/ +
 /(/ +                               // 17 - [helper group, don't use]
    /([\dKMG]+)->/ +                 // 18 - non-PermGen start value
	/([\dKMG]+)\(/ +                 // 19 - non-PermGen end value
	/([\dKMG]+)\)\s?/ +              // 20 - non-PermGen Max size
 /))+/ +
 /,\s([\d\.]+)\ssecs\]/ +            // 21 - total garbage collection event time
 /(\s\[Times:\s/ +                   // 22 - [helper group, don't use]
	/(/ +                            // 23 - helper group, don't use
		/(\w+)=/ +                   // 24 - timing name (user/sys/real)
		/([\d\.]+)/ +                // 25 - timing value (user/sys/real)
	/,?\s)+/ +
 /secs\]\s?)?/)

    private String text

    GCLogParser(File file) {
        this(file.text)
    }

    GCLogParser(String text) {
        this.text = text
    }

    GCEvents parse() {
        HashMap<Date, GCEvent> hashMapOnDate = new HashMap<Date, GCEvent>()
        HashMap<Long, GCEvent> hashMapOnMillis = new HashMap<Long, GCEvent>()
        text.eachLine { line ->
            processLine(line, hashMapOnDate, hashMapOnMillis)
        }
        //TODO: use unmodifiable maps
        return new GCEvents(hashMapOnDate: hashMapOnDate, hashMapOnMillis: hashMapOnMillis)
    }

    void processLine(String line, HashMap<Date, GCEvent> hashMapOnDate, HashMap<Long, GCEvent> hashMapOnMillis) {
        def matcher = (line =~ lineRegEx)
        if (matcher.find()) {
            def calendar = Calendar.getInstance()
            calendar.set(matcher.group(1) as int, (matcher.group(2) as int) - 1, matcher.group(3) as int,
                matcher.group(4) as int, matcher.group(5) as int, matcher.group(6) as int)
            calendar.set(Calendar.MILLISECOND, matcher.group(7) as int)
            Date time = calendar.getTime()

            long timeInMillis = new BigDecimal(matcher.group(8)) * 1000
            def gcEventName = matcher.group(9)

            Map<String, SingleGCStatistic> stats = [:]
            String gcName = matcher.group(13)
            stats[gcName] = new SingleGCStatistic(gcName: gcName,
                    startValueInB: Utils.convertMemoryValueStringToLong(matcher.group(14)),
                    endValueInB: Utils.convertMemoryValueStringToLong(matcher.group(15)),
                    maxValueInB: Utils.convertMemoryValueStringToLong(matcher.group(16)))
            stats[null] = new SingleGCStatistic(gcName: null,
                    startValueInB: Utils.convertMemoryValueStringToLong(matcher.group(18)),
                    endValueInB: Utils.convertMemoryValueStringToLong(matcher.group(19)),
                    maxValueInB: Utils.convertMemoryValueStringToLong(matcher.group(20)))

            //TODO: timing statistics

            def event = new GCEvent(time: time, timeInMillis: timeInMillis,
                    gcEventName: gcEventName, stats: Collections.unmodifiableMap(stats))

            hashMapOnDate[time] = hashMapOnMillis[timeInMillis] = event

        } else {
            throw new IllegalArgumentException("Not matched garbage collection log line: $line")
        }
    }
}
