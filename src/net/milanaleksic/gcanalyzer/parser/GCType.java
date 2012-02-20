package net.milanaleksic.gcanalyzer.parser;

/**
 * User: Milan Aleksic
 * Date: 2/20/12
 * Time: 1:28 PM
 */
public enum GCType {

    YOUNG {
        @Override public String[] knownMappings() {
            return new String[] {
                    "DefNew",    // Serial GC
                    "PSYoungGen" // Throughput (aka Parallel)

            };
        }
    },
    OLD {
        @Override public String[] knownMappings() {
            return new String[] {
                    "Tenured",  // Serial GC
                    "ParOldGen" // Throughput (aka Parallel)
            };
        }
    },
    PERM {
        @Override public String[] knownMappings() {
            return new String[] {
                    "Perm",     // Serial GC
                    "PSPermGen" // Throughput (aka Parallel)
            };
        }
    };

    public abstract String[] knownMappings() ;

}