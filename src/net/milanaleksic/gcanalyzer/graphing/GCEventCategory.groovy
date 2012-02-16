package net.milanaleksic.gcanalyzer.graphing

public enum GCEventCategory {

    HEAP_CALCULATION {
        @Override
        String getTitle() {
            return "Heap calculation"
        }
    },
    MEMORY_OCCUPANCY {
        @Override
        String getTitle() {
            return "Memory occupancy"
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
    },
    SURVIVOR_DETAILS {
        @Override
        String getTitle() {
            return "Survivor pool details"
        }
    }

    String getTitle() {throw new IllegalStateException("Abstract")}
}
