package net.milanaleksic.gcanalyzer.graphing;

public enum GCEventCategory {

    HEAP_CALCULATION {
        @Override public String getTitle() {
            return "Heap calculation";
        }
    },
    MEMORY_OCCUPANCY {
        @Override public String getTitle() {
            return "Memory occupancy";
        }
    },
    EVENTS_PER_HOUR {
        @Override public String getTitle() {
            return "Events per hour";
        }
    },
    INDIVIDUAL_EVENT_TIMING {
        @Override public String getTitle() {
            return "Individual Event timing";
        }
    },
    TIME_SPENT_PER_HOUR {
        @Override public String getTitle() {
            return "Time spent for GC per hour";
        }
    },
    SURVIVOR_DETAILS {
        @Override public String getTitle() {
            return "Survivor pool details";
        }
    };

    public abstract String getTitle() ;
}
