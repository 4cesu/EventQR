package com.thedavelopers.eventqr.features.reports.service;

import static com.thedavelopers.eventqr.features.reports.model.dto.EventReportDtos.EventReportFilters;
import static com.thedavelopers.eventqr.features.reports.model.dto.EventReportDtos.EventReportResponse;
import static com.thedavelopers.eventqr.features.reports.model.dto.EventReportDtos.EventReportRow;
import static com.thedavelopers.eventqr.features.reports.model.dto.EventReportDtos.EventReportSummaryResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thedavelopers.eventqr.features.events.model.entity.Event;
import com.thedavelopers.eventqr.features.events.repository.EventRepository;
import com.thedavelopers.eventqr.features.registrations.model.entity.EventRegistration;
import com.thedavelopers.eventqr.features.registrations.repository.EventRegistrationRepository;
import com.thedavelopers.eventqr.features.reports.model.ReportEmptyState;
import com.thedavelopers.eventqr.features.reports.model.ReportFilterStatus;
import com.thedavelopers.eventqr.features.reports.model.ReportType;
import com.thedavelopers.eventqr.features.rewards.model.entity.PointTransaction;
import com.thedavelopers.eventqr.features.rewards.repository.PointTransactionRepository;
import com.thedavelopers.eventqr.features.transactions.model.entity.TransactionLog;
import com.thedavelopers.eventqr.features.transactions.repository.TransactionLogRepository;
import com.thedavelopers.eventqr.shared.constants.RegistrationStatus;
import com.thedavelopers.eventqr.shared.constants.TransactionResult;
import com.thedavelopers.eventqr.shared.constants.TransactionType;
import com.thedavelopers.eventqr.shared.exceptions.BadRequestException;
import com.thedavelopers.eventqr.shared.exceptions.ForbiddenException;
import com.thedavelopers.eventqr.shared.exceptions.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
public class EventReportGenerationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy h:mm a", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC);

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ObjectMapper objectMapper;

    public EventReportGenerationService(EventRepository eventRepository,
                                        EventRegistrationRepository registrationRepository,
                                        TransactionLogRepository transactionLogRepository,
                                        PointTransactionRepository pointTransactionRepository,
                                        ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.transactionLogRepository = transactionLogRepository;
        this.pointTransactionRepository = pointTransactionRepository;
        this.objectMapper = objectMapper;
    }

    public EventReportSummaryResponse summary(UUID organizerUserId, UUID eventId) {
        Event event = requireOrganizerEvent(organizerUserId, eventId);
        List<EventRegistration> registrations = registrationRepository.findByEventId(eventId);
        long registered = registrations.size();
        long checkedIn = registrations.stream().filter(registration -> registration.getStatus() == RegistrationStatus.ENTERED).count();
        long exited = registrations.stream().filter(registration -> registration.getStatus() == RegistrationStatus.EXITED).count();
        boolean hasAnyRecords = registered > 0 || !transactionLogRepository.findByEventId(eventId).isEmpty()
                || !pointTransactionRepository.findByEventId(eventId).isEmpty();
        return new EventReportSummaryResponse(eventId, event.getTitle(), registered, checkedIn, exited, hasAnyRecords);
    }

    public EventReportResponse generate(UUID organizerUserId, UUID eventId, ReportType reportType, EventReportFilters filters) {
        validateDateRange(filters);
        Event event = requireOrganizerEvent(organizerUserId, eventId);
        List<EventRegistration> registrations = registrationRepository.findByEventId(eventId);
        List<TransactionLog> transactions = transactionLogRepository.findByEventIdOrderByScannedAtDesc(eventId);
        List<PointTransaction> pointTransactions = pointTransactionRepository.findByEventId(eventId);
        Map<UUID, EventRegistration> registrationByUser = registrations.stream()
                .collect(Collectors.toMap(EventRegistration::getAttendeeUserId, registration -> registration, (first, second) -> first));

        ReportAssembly assembly = switch (reportType) {
            case ROSTER -> buildRoster(event, registrations, filters);
            case NO_SHOWS -> buildNoShows(event, registrations, filters);
            case ENTRY_LOGS -> buildEntryLogs(event, registrationByUser, transactions, filters);
            case ATTENDANCE -> buildAttendance(event, registrationByUser, transactions, filters);
            case CLAIMS -> buildClaims(event, registrationByUser, transactions, filters);
            case BOOTH_VISITS -> buildBoothVisits(event, registrationByUser, transactions, filters);
            case EXIT_LOGS -> buildExitLogs(event, registrationByUser, transactions, filters);
            case POINTS -> buildPoints(event, registrationByUser, pointTransactions, filters);
        };

        ReportEmptyState emptyState = ReportEmptyState.NONE;
        if (assembly.allRows().isEmpty()) {
            emptyState = ReportEmptyState.NO_EVENT_RECORDS;
        } else if (assembly.rows().isEmpty()) {
            emptyState = ReportEmptyState.NO_FILTER_MATCH;
        }

        return new EventReportResponse(
                eventId,
                reportType,
                assembly.title(),
                event.getTitle(),
                Instant.now(),
                assembly.columns(),
                assembly.rows().stream().map(RowData::row).toList(),
                assembly.chartSeries(),
                emptyState,
                normalizeFilters(filters)
        );
    }

    private ReportAssembly buildRoster(Event event, List<EventRegistration> registrations, EventReportFilters filters) {
        List<String> columns = List.of("Name", "Registration Status", "Registered On");
        List<RowData> all = registrations.stream()
                .sorted(Comparator.comparing(EventRegistration::getRegisteredAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(registration -> new RowData(
                        new EventReportRow(List.of(
                                safe(registration.getAttendeeName()),
                                prettyRegistrationStatus(registration.getStatus()),
                                formatDate(registration.getRegisteredAt())
                        )),
                        registration.getRegisteredAt(),
                        safe(registration.getAttendeeName()),
                        null
                )).toList();

        List<RowData> filtered = applyDateFilter(all, filters)
                .stream()
                .filter(row -> attendeeMatches(filters, row.attendeeName()))
                .toList();

        Map<String, Long> chart = chartBy(columns.get(1), filtered.stream()
                .collect(Collectors.groupingBy(row -> row.row().values().get(1), LinkedHashMap::new, Collectors.counting())));

        return new ReportAssembly("Attendee Roster Report", columns, all, filtered, chart);
    }

    private ReportAssembly buildNoShows(Event event, List<EventRegistration> registrations, EventReportFilters filters) {
        List<String> columns = List.of("Name", "Registered On", "Reason");
        List<RowData> all = registrations.stream()
                .filter(registration -> registration.getStatus() == RegistrationStatus.REGISTERED || registration.getStatus() == RegistrationStatus.NO_SHOW)
                .sorted(Comparator.comparing(EventRegistration::getRegisteredAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(registration -> new RowData(
                        new EventReportRow(List.of(
                                safe(registration.getAttendeeName()),
                                formatDate(registration.getRegisteredAt()),
                                registration.getStatus() == RegistrationStatus.NO_SHOW ? "Marked No Show" : "Not Entered"
                        )),
                        registration.getRegisteredAt(),
                        safe(registration.getAttendeeName()),
                        null
                )).toList();

        List<RowData> filtered = applyDateFilter(all, filters);
        Map<String, Long> chart = chartBy("No Shows", Map.of("No Shows", (long) filtered.size()));
        return new ReportAssembly("No-Shows Report", columns, all, filtered, chart);
    }

    private ReportAssembly buildEntryLogs(Event event,
                                          Map<UUID, EventRegistration> registrationByUser,
                                          List<TransactionLog> transactions,
                                          EventReportFilters filters) {
        List<String> columns = List.of("Name", "Entry Time", "Result");
        List<RowData> all = transactions.stream()
                .filter(transaction -> transaction.getTransactionType() == TransactionType.ENTRY)
                .map(transaction -> toTransactionRow(transaction, registrationByUser, columns,
                        safeAttendeeName(registrationByUser, transaction.getAttendeeUserId()),
                        formatDateTime(transaction.getScannedAt()),
                        prettyResult(transaction.getTransactionResult())))
                .toList();

        List<RowData> filtered = applyStatusFilter(applyDateFilter(all, filters), filters);
        Map<String, Long> chart = chartBy("Result", filtered.stream()
                .collect(Collectors.groupingBy(row -> row.row().values().get(2), LinkedHashMap::new, Collectors.counting())));
        return new ReportAssembly("Entry Logs Report", columns, all, filtered, chart);
    }

    private ReportAssembly buildAttendance(Event event,
                                           Map<UUID, EventRegistration> registrationByUser,
                                           List<TransactionLog> transactions,
                                           EventReportFilters filters) {
        List<String> columns = List.of("Name", "Session/Activity", "Timestamp");
        List<RowData> all = transactions.stream()
                .filter(transaction -> transaction.getTransactionType() == TransactionType.ATTENDANCE)
                .map(transaction -> new RowData(
                        new EventReportRow(List.of(
                                safeAttendeeName(registrationByUser, transaction.getAttendeeUserId()),
                                extractActivityLabel(transaction),
                                formatDateTime(transaction.getScannedAt())
                        )),
                        transaction.getScannedAt(),
                        safeAttendeeName(registrationByUser, transaction.getAttendeeUserId()),
                        transaction.getTransactionResult()
                ))
                .toList();

        List<RowData> filtered = applyStatusFilter(applyDateFilter(all, filters), filters)
                .stream()
                .filter(row -> attendeeMatches(filters, row.attendeeName()))
                .toList();

        Map<String, Long> chart = chartBy("Activity", filtered.stream()
                .collect(Collectors.groupingBy(row -> row.row().values().get(1), LinkedHashMap::new, Collectors.counting())));
        return new ReportAssembly("Attendance Report", columns, all, filtered, chart);
    }

    private ReportAssembly buildClaims(Event event,
                                       Map<UUID, EventRegistration> registrationByUser,
                                       List<TransactionLog> transactions,
                                       EventReportFilters filters) {
        List<String> columns = List.of("Name", "Benefit", "Claimed At", "Result");
        List<RowData> all = transactions.stream()
                .filter(transaction -> transaction.getTransactionType() == TransactionType.BENEFIT_CLAIM)
                .map(transaction -> toTransactionRow(transaction, registrationByUser, columns,
                        safeAttendeeName(registrationByUser, transaction.getAttendeeUserId()),
                        benefitLabel(transaction),
                        formatDateTime(transaction.getScannedAt()),
                        prettyResult(transaction.getTransactionResult())))
                .toList();

        List<RowData> filtered = applyStatusFilter(applyDateFilter(all, filters), filters);
        Map<String, Long> chart = chartBy("Result", filtered.stream()
                .collect(Collectors.groupingBy(row -> row.row().values().get(3), LinkedHashMap::new, Collectors.counting())));
        return new ReportAssembly("Benefit Claims Report", columns, all, filtered, chart);
    }

    private ReportAssembly buildBoothVisits(Event event,
                                            Map<UUID, EventRegistration> registrationByUser,
                                            List<TransactionLog> transactions,
                                            EventReportFilters filters) {
        List<String> columns = List.of("Name", "Booth/Session", "Visit Time");
        List<RowData> all = transactions.stream()
                .filter(transaction -> transaction.getTransactionType() == TransactionType.BOOTH_VISIT
                        || transaction.getTransactionType() == TransactionType.SESSION_VISIT)
                .map(transaction -> new RowData(
                        new EventReportRow(List.of(
                                safeAttendeeName(registrationByUser, transaction.getAttendeeUserId()),
                                extractActivityLabel(transaction),
                                formatDateTime(transaction.getScannedAt())
                        )),
                        transaction.getScannedAt(),
                        safeAttendeeName(registrationByUser, transaction.getAttendeeUserId()),
                        transaction.getTransactionResult()
                ))
                .toList();

        List<RowData> filtered = applyStatusFilter(applyDateFilter(all, filters), filters);
        Map<String, Long> chart = chartBy("Visit Type", filtered.stream()
                .collect(Collectors.groupingBy(row -> row.row().values().get(1), LinkedHashMap::new, Collectors.counting())));
        return new ReportAssembly("Booth/Session Visits Report", columns, all, filtered, chart);
    }

    private ReportAssembly buildExitLogs(Event event,
                                         Map<UUID, EventRegistration> registrationByUser,
                                         List<TransactionLog> transactions,
                                         EventReportFilters filters) {
        List<String> columns = List.of("Name", "Exit Time", "Result");
        List<RowData> all = transactions.stream()
                .filter(transaction -> transaction.getTransactionType() == TransactionType.EXIT)
                .map(transaction -> toTransactionRow(transaction, registrationByUser, columns,
                        safeAttendeeName(registrationByUser, transaction.getAttendeeUserId()),
                        formatDateTime(transaction.getScannedAt()),
                        prettyResult(transaction.getTransactionResult())))
                .toList();

        List<RowData> filtered = applyDateFilter(all, filters);
        Map<String, Long> chart = chartBy("Result", filtered.stream()
                .collect(Collectors.groupingBy(row -> row.row().values().get(2), LinkedHashMap::new, Collectors.counting())));
        return new ReportAssembly("Exit Logs Report", columns, all, filtered, chart);
    }

    private ReportAssembly buildPoints(Event event,
                                       Map<UUID, EventRegistration> registrationByUser,
                                       List<PointTransaction> pointTransactions,
                                       EventReportFilters filters) {
        List<String> columns = List.of("Name", "Points Earned", "Source Activity");
        List<RowData> all = pointTransactions.stream()
                .sorted(Comparator.comparing(PointTransaction::getOccurredAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(transaction -> new RowData(
                        new EventReportRow(List.of(
                                safeAttendeeName(registrationByUser, transaction.getAttendeeUserId()),
                                String.valueOf(transaction.getPointsChanged()),
                                safe(transaction.getReason()).isBlank() ? "Scan reward points" : safe(transaction.getReason())
                        )),
                        transaction.getOccurredAt(),
                        safeAttendeeName(registrationByUser, transaction.getAttendeeUserId()),
                        null
                ))
                .toList();

        List<RowData> filtered = applyDateFilter(all, filters)
                .stream()
                .filter(row -> attendeeMatches(filters, row.attendeeName()))
                .toList();

        Map<String, Long> chart = chartBy("Source", filtered.stream()
                .collect(Collectors.groupingBy(row -> row.row().values().get(2), LinkedHashMap::new, Collectors.counting())));
        return new ReportAssembly("Points Report", columns, all, filtered, chart);
    }

    private List<RowData> applyDateFilter(List<RowData> rows, EventReportFilters filters) {
        if (filters == null || (filters.startDate() == null && filters.endDate() == null)) {
            return rows;
        }
        LocalDate start = filters.startDate();
        LocalDate end = filters.endDate();
        return rows.stream()
                .filter(row -> {
                    if (row.occurredAt() == null) {
                        return false;
                    }
                    LocalDate day = row.occurredAt().atOffset(ZoneOffset.UTC).toLocalDate();
                    boolean afterStart = start == null || !day.isBefore(start);
                    boolean beforeEnd = end == null || !day.isAfter(end);
                    return afterStart && beforeEnd;
                })
                .toList();
    }

    private List<RowData> applyStatusFilter(List<RowData> rows, EventReportFilters filters) {
        if (filters == null || filters.status() == null || filters.status() == ReportFilterStatus.ALL) {
            return rows;
        }
        Predicate<RowData> matcher = switch (filters.status()) {
            case APPROVED -> row -> row.result() == TransactionResult.APPROVED;
            case REJECTED -> row -> row.result() == TransactionResult.REJECTED;
            case ALL -> row -> true;
        };
        return rows.stream().filter(matcher).toList();
    }

    private boolean attendeeMatches(EventReportFilters filters, String attendeeName) {
        if (filters == null || filters.attendeeQuery() == null || filters.attendeeQuery().isBlank()) {
            return true;
        }
        return safe(attendeeName).toLowerCase(Locale.ENGLISH)
                .contains(filters.attendeeQuery().trim().toLowerCase(Locale.ENGLISH));
    }

    private ReportAssembly toAssembly(String title,
                                      List<String> columns,
                                      List<RowData> all,
                                      List<RowData> filtered,
                                      Map<String, Long> chartSeries) {
        return new ReportAssembly(title, columns, all, filtered, chartSeries);
    }

    private EventReportFilters normalizeFilters(EventReportFilters filters) {
        if (filters == null) {
            return new EventReportFilters(null, null, null, ReportFilterStatus.ALL);
        }
        return new EventReportFilters(
                filters.startDate(),
                filters.endDate(),
                filters.attendeeQuery(),
                filters.status() == null ? ReportFilterStatus.ALL : filters.status()
        );
    }

    private Event requireOrganizerEvent(UUID organizerUserId, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (event.getOrganizerUserId() == null || !event.getOrganizerUserId().equals(organizerUserId)) {
            throw new ForbiddenException("Organizer does not have access to this event");
        }
        return event;
    }

    private void validateDateRange(EventReportFilters filters) {
        if (filters == null || filters.startDate() == null || filters.endDate() == null) {
            return;
        }
        if (filters.endDate().isBefore(filters.startDate())) {
            throw new BadRequestException("endDate must not be before startDate");
        }
    }

    private RowData toTransactionRow(TransactionLog transaction,
                                     Map<UUID, EventRegistration> registrationByUser,
                                     List<String> columns,
                                     String value1,
                                     String value2,
                                     String value3) {
        return new RowData(
                new EventReportRow(List.of(value1, value2, value3)),
                transaction.getScannedAt(),
                safeAttendeeName(registrationByUser, transaction.getAttendeeUserId()),
                transaction.getTransactionResult()
        );
    }

    private RowData toTransactionRow(TransactionLog transaction,
                                     Map<UUID, EventRegistration> registrationByUser,
                                     List<String> columns,
                                     String value1,
                                     String value2,
                                     String value3,
                                     String value4) {
        return new RowData(
                new EventReportRow(List.of(value1, value2, value3, value4)),
                transaction.getScannedAt(),
                safeAttendeeName(registrationByUser, transaction.getAttendeeUserId()),
                transaction.getTransactionResult()
        );
    }

    private String safeAttendeeName(Map<UUID, EventRegistration> registrations, UUID attendeeUserId) {
        return Optional.ofNullable(registrations.get(attendeeUserId))
                .map(EventRegistration::getAttendeeName)
                .map(this::safe)
                .filter(name -> !name.isBlank())
                .orElse("Unknown attendee");
    }

    private String extractActivityLabel(TransactionLog transaction) {
        String fromMetadata = metadataValue(transaction.getMetadata(), "scanPurposeLabel")
                .orElseGet(() -> metadataValue(transaction.getMetadata(), "scanPurposeCode").orElse(""));
        if (!fromMetadata.isBlank()) {
            return fromMetadata;
        }
        return switch (transaction.getTransactionType()) {
            case SESSION_VISIT -> "Session Visit";
            case BOOTH_VISIT -> "Booth Visit";
            case ATTENDANCE -> "Attendance";
            default -> prettyType(transaction.getTransactionType());
        };
    }

    private String benefitLabel(TransactionLog transaction) {
        if (transaction.getReason() != null && !transaction.getReason().isBlank()) {
            return transaction.getReason();
        }
        String fromMetadata = metadataValue(transaction.getMetadata(), "scanPurposeLabel").orElse("");
        if (!fromMetadata.isBlank()) {
            return fromMetadata;
        }
        return "Benefit Claim";
    }

    private Optional<String> metadataValue(String metadata, String key) {
        if (metadata == null || metadata.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode node = objectMapper.readTree(metadata);
            JsonNode valueNode = node.get(key);
            if (valueNode == null || valueNode.isNull()) {
                return Optional.empty();
            }
            return Optional.ofNullable(valueNode.asText(""));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private Map<String, Long> chartBy(String key, Map<String, Long> values) {
        if (values.isEmpty()) {
            return Map.of(key, 0L);
        }
        LinkedHashMap<String, Long> sorted = values.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> first,
                        LinkedHashMap::new));
        return sorted;
    }

    private String prettyResult(TransactionResult result) {
        return result == null ? "Unknown" : (result == TransactionResult.APPROVED ? "Approved" : "Rejected");
    }

    private String prettyRegistrationStatus(RegistrationStatus status) {
        return switch (status) {
            case REGISTERED -> "Registered";
            case ENTERED -> "Entered";
            case EXITED -> "Exited";
            case NO_SHOW -> "No Show";
            case CANCELLED -> "Cancelled";
        };
    }

    private String prettyType(TransactionType type) {
        String normalized = type.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
        String[] chunks = normalized.split(" ");
        List<String> titled = new ArrayList<>();
        for (String chunk : chunks) {
            if (chunk.isBlank()) {
                continue;
            }
            titled.add(Character.toUpperCase(chunk.charAt(0)) + chunk.substring(1));
        }
        return String.join(" ", titled);
    }

    private String formatDateTime(Instant instant) {
        if (instant == null) {
            return "-";
        }
        return DATE_TIME_FORMATTER.format(instant);
    }

    private String formatDate(Instant instant) {
        if (instant == null) {
            return "-";
        }
        return DATE_FORMATTER.format(instant);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record RowData(EventReportRow row, Instant occurredAt, String attendeeName, TransactionResult result) {
    }

    private record ReportAssembly(String title,
                                  List<String> columns,
                                  List<RowData> allRows,
                                  List<RowData> rows,
                                  Map<String, Long> chartSeries) {
    }
}
