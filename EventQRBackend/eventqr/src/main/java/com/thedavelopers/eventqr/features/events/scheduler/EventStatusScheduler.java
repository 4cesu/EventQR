package com.thedavelopers.eventqr.features.events.scheduler;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thedavelopers.eventqr.features.events.model.entity.Event;
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
    public void transitionOverdueEvents() {
        Instant now = Instant.now();

        int activated = activateStartedEvents(now);
        int ended = endFinishedEvents(now);

        if (activated > 0 || ended > 0) {
            log.info("Event status sweep: {} event(s) moved to ACTIVE, {} event(s) moved to ENDED", activated, ended);
        }
    }

    private int activateStartedEvents(Instant now) {
        List<Event> due = eventRepository.findByStatusAndEventStartAtLessThanEqual(EventStatus.APPROVED, now);
        for (Event event : due) {
            EventStatus previous = event.getStatus();
            event.setStatus(EventStatus.ACTIVE);
            eventRepository.save(event);
            log.info("Status transition: event {} ('{}') {} -> ACTIVE", event.getId(), event.getTitle(), previous);
        }
        return due.size();
    }

    private int endFinishedEvents(Instant now) {
        List<Event> due = eventRepository.findByStatusAndEventEndAtLessThanEqual(EventStatus.ACTIVE, now);
        for (Event event : due) {
            EventStatus previous = event.getStatus();
            event.setStatus(EventStatus.ENDED);
            eventRepository.save(event);
            log.info("Status transition: event {} ('{}') {} -> ENDED", event.getId(), event.getTitle(), previous);
        }
        return due.size();
    }
}
