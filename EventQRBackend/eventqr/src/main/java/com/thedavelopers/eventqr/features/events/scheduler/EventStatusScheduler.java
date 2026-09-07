package com.thedavelopers.eventqr.features.events.scheduler;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thedavelopers.eventqr.features.events.repository.EventRepository;
import com.thedavelopers.eventqr.shared.constants.EventStatus;

@Component
public class EventStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventStatusScheduler.class);

    private static final long SWEEP_INTERVAL_MS = 60_000L;

    private final EventRepository eventRepository;

    public EventStatusScheduler(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Scheduled(fixedRate = SWEEP_INTERVAL_MS)
    @Transactional
    @CacheEvict(cacheNames = "events", allEntries = true)
    public void transitionOverdueEvents() {
        Instant now = Instant.now();

        int activated = eventRepository.bulkUpdateStatusForStartedEvents(
                EventStatus.APPROVED, EventStatus.ACTIVE, now);
        int ended = eventRepository.bulkUpdateStatusForFinishedEvents(
                EventStatus.ACTIVE, EventStatus.ENDED, now);

        if (activated > 0 || ended > 0) {
            log.info("Event status sweep: {} event(s) moved to ACTIVE, {} event(s) moved to ENDED", activated, ended);
        }
    }
}
