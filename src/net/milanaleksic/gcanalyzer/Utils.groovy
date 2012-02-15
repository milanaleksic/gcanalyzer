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

    static Date roundToHour(Date date) {
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(date)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.getTime()
    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter()
        PrintWriter pw = new PrintWriter(sw)
        t.printStackTrace(pw)
        return sw.toString()
    }

    public static String getApplicationVersion() {
        InputStream stream
        try {
            stream = Utils.class.getResourceAsStream("version.txt")
            if (stream == null)
                throw new IOException("Resource not found: version.txt")
            return stream.text
        } finally {
            if (stream)
                stream.close()
        }
    }

}
