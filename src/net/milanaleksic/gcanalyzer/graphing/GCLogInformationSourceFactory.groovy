package net.milanaleksic.gcanalyzer.graphing

import net.milanaleksic.gcanalyzer.util.Utils
import java.lang.reflect.Method
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * User: Milan Aleksic
 * Date: 2/19/12
 * Time: 8:02 PM
 */
class GCLogInformationSourceFactory {

    private static def pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    static GCLogInformationSource createGCLogInformationSource(String filename) {
        GCLogInformationSource source = new GCLogInformationSource(filename)
        parallelizedInitializationOfAllCacheableCalls(source)
        return source
    }

    static GCLogInformationSource createGCLogInformationSource(URL url) {
        GCLogInformationSource source = new GCLogInformationSource(url)
        parallelizedInitializationOfAllCacheableCalls(source)
        return source
    }

    private static void parallelizedInitializationOfAllCacheableCalls(GCLogInformationSource gcLogInformationSource) {
        long begin = System.currentTimeMillis()
        Method[] methods = Utils.getCacheableMethodsOnClass(GCLogInformationSource.class)
        methods
            .collect { method -> pool.submit({invoke(method, gcLogInformationSource)} as Runnable) }
            .each { Future future -> future.get() }
        println "Parallelized initialization of ${methods.size()} calls done in ${System.currentTimeMillis() - begin} ms"
    }

    private static void initializationOfAllCacheableCalls(GCLogInformationSource gcLogInformationSource) {
        long begin = System.currentTimeMillis()
        Method[] methods = Utils.getCacheableMethodsOnClass(GCLogInformationSource.class)
        methods.each { method ->
            invoke(method, gcLogInformationSource)
        }
        println "Serialized initialization of ${methods.size()} calls done in ${System.currentTimeMillis() - begin} ms"
    }

    private static def invoke(Method method, Object object) {
        boolean wasAccessible = method.isAccessible()
        if (!wasAccessible)
            method.setAccessible(true)
        method.invoke(object)
        if (!wasAccessible)
            method.setAccessible(false)
    }

}
