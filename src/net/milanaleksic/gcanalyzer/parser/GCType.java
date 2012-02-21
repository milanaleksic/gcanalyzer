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
                    YOUNG_SERIAL,
                    YOUNG_PARALLEL
            };
        }
    },
    OLD {
        @Override public String[] knownMappings() {
            return new String[] {
                    OLD_SERIAL,
                    OLD_PARALLEL
            };
        }
    },
    PERM {
        @Override public String[] knownMappings() {
            return new String[] {
                    PERM_SERIAL,
                    PERM_PARALLEL
            };
        }
    };
    public static final String YOUNG_PARALLEL = "PSYoungGen";
    public static final String YOUNG_SERIAL = "DefNew";

    public static final String OLD_PARALLEL = "ParOldGen";
    public static final String OLD_SERIAL = "Tenured";

    public static final String PERM_PARALLEL = "PSPermGen";
    public static final String PERM_SERIAL = "Perm";

    public abstract String[] knownMappings() ;

}