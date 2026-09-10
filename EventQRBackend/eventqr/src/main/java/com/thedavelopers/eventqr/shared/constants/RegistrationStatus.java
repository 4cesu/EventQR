package com.thedavelopers.eventqr.shared.constants;

import java.util.Set;

/**
 * Canonical set of registration statuses that are NOT counted as "registered".
 * Used by DashboardService, OrganizerService, and EventReportGenerationService
 * to keep registration counts in sync.
 */
public enum RegistrationStatus {
    REGISTERED,
    ENTERED,
    EXITED,
    CANCELLED,
    NO_SHOW;

    private static final Set<RegistrationStatus> NON_REGISTERED_STATUSES =
            Set.of(CANCELLED, NO_SHOW);

    /**
     * Returns true if this status represents an active registration
     * (i.e. not CANCELLED and not NO_SHOW).
     * Canonical predicate — must stay in sync with DashboardService,
     * OrganizerService, and EventReportGenerationService.
     */
    public boolean isCountedAsRegistered() {
        return !NON_REGISTERED_STATUSES.contains(this);
    }
}