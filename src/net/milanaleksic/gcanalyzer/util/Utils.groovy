package net.milanaleksic.gcanalyzer.util

import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 12:26 PM
 */
class Utils {
    static long convertMemoryValueStringToLong(String value) {
        if (value.endsWith('K'))
            return Long.parseLong(value.substring(0, value.size() - 1), 10) << 10
        else if (value.endsWith('M'))
            return Long.parseLong(value.substring(0, value.size() - 1), 10) << 20
        else if (value.endsWith('G'))
            return Long.parseLong(value.substring(0, value.size() - 1), 10) << 30
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
            stream = Utils.class.getResourceAsStream("/net/milanaleksic/gcanalyzer/version.txt")
            if (stream == null)
                throw new IOException("Resource not found: version.txt")
            return stream.text
        } finally {
            if (stream)
                stream.close()
        }
    }

    public static String getSmallerStackTraceForThrowable(Throwable t) {
        return getSmallerStackTrace(getStackTrace(t))
    }

    private static final def groovyStack = [
            ~/at groovy\.lang\./,
            ~/at org\.codehaus\.groovy\.runtime\./,
            ~/at org\.codehaus\.runtime\./,
            ~/at sun\.reflect/,
            ~/at java\.lang\.reflect/,
            ~/at org\.codehaus\.groovy\.reflection/,
            ~/\.call\(Unknown Source\)/,
            ~/\.callCurrent\(Unknown Source\)/,
            ~/\_closure(\d+)\.doCall/
    ]
    public static String getSmallerStackTrace(String stackTrace) {
        String miniStackTrace = ''
        stackTrace.eachLine { line ->
            def isGroovyStack = false
            groovyStack.each { groovyStackElem ->
                if (line =~ groovyStackElem)
                    isGroovyStack = true
            }
            if (!isGroovyStack)
                miniStackTrace += line + '\r\n'
        }
        return miniStackTrace
    }

    public static Method[] getCacheableMethodsOnClass(Class<?> clazz) {
        def ofTheJedi = []
        clazz.declaredMethods.each { Method method ->
            if (method.declaringClass.name.equals(clazz.name) &&
                    method.annotations.any { it.annotationType().equals(Cacheable.class)}) {
                    if (method.parameterTypes.length!=0) {
                        println "Method $method can't be cached because it has parameters"
                        return
                    }
                    ofTheJedi << method
            }
        }
        return ofTheJedi
    }

}
