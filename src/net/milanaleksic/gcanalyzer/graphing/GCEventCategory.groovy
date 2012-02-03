package net.milanaleksic.gcanalyzer.graphing

public enum GCEventCategory {

    MEMORY_MAX_SIZE {
        @Override
        String getTitle() {
            return "Memory Max Size"
        }
    },
    EVENTS_PER_HOUR {
        @Override
        String getTitle() {
            return "Events per hour"
        }
    },
    INDIVIDUAL_EVENT_TIMING {
        @Override
        String getTitle() {
            return "Individual Event timing"
        }
    },
    TIME_SPENT_PER_HOUR {
        @Override
        String getTitle() {
            return "Time spent for GC per hour"
        }
    }

    String getTitle() {throw new IllegalStateException("Abstract")}
}
