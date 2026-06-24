package com.example.easybooking.reservation.service;

import com.example.easybooking.availability.HolidayChecker;
import com.example.easybooking.availability.OperatingTimeResolver;
import com.example.easybooking.availability.domain.ShopOperatingTime;
import com.example.easybooking.availability.ShopOperatingTimeReader;
import com.example.easybooking.availability.dto.response.ShopTimeRangeResponse;
import com.example.easybooking.errors.errorcode.AuthErrorCode;
import com.example.easybooking.errors.errorcode.ShopErrorCode;
import com.example.easybooking.errors.exception.AuthException;
import com.example.easybooking.errors.exception.ShopException;
import com.example.easybooking.reservation.ReservationMenuItemReader;
import com.example.easybooking.reservation.ReservationReader;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.domain.ReservationMenuItem;
import com.example.easybooking.reservation.dto.calendar.OwnerCalendarDayResponse;
import com.example.easybooking.reservation.dto.calendar.OwnerCalendarReservationResponse;
import com.example.easybooking.reservation.dto.calendar.OwnerCalendarResponse;
import com.example.easybooking.reservation.dto.calendar.OwnerCalendarStaffColumnResponse;
import com.example.easybooking.reservation.dto.calendar.OwnerCalendarTimelineResponse;
import com.example.easybooking.reservation.dto.calendar.OwnerCalendarTimeRangeResponse;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.staff.StaffReader;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerCalendarService {

    private static final int PENDING_DISPLAY_DURATION_MINUTES = 30;
    private static final int RESERVED_PADDING_MINUTES = 30;
    private static final String UNASSIGNED_STAFF_NAME = "미지정";

    private final ReservationReader reservationReader;
    private final ReservationMenuItemReader reservationMenuItemReader;
    private final ShopReader shopReader;
    private final StaffReader staffReader;
    private final UserReader userReader;
    private final HolidayChecker holidayChecker;
    private final OperatingTimeResolver operatingTimeResolver;
    private final ShopOperatingTimeReader shopOperatingTimeReader;
    private final Clock clock;

    public OwnerCalendarResponse getCalendar(Long shopId, Long ownerUserId, LocalDate requestedDate, Long staffId) {
        validateOwner(shopId, ownerUserId);

        LocalDate selectedDate = requestedDate != null ? requestedDate : LocalDate.now(clock);
        LocalDate weekStartDate = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        List<Reservation> reservations = reservationReader.findByShopIdAndDateBetween(
                shopId,
                weekStartDate,
                weekEndDate
        ).stream()
                .filter(reservation -> reservation.getStatus() == Reservation.Status.PENDING
                        || reservation.getStatus() == Reservation.Status.CONFIRMED)
                .toList();

        List<Staff> staffColumns = resolveStaffColumns(shopId, staffId);
        Map<Long, Staff> staffById = staffColumns.stream()
                .filter(staff -> staff.getId() != null)
                .collect(Collectors.toMap(Staff::getId, staff -> staff, (a, b) -> a, LinkedHashMap::new));

        Map<Long, List<ReservationMenuItem>> menuItemsByReservationId = loadMenuItems(reservations);
        Map<Long, User> customerById = loadCustomers(reservations);
        Map<LocalDate, List<Reservation>> reservationsByDate = reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getDate, LinkedHashMap::new, Collectors.toList()));

        List<OwnerCalendarDayResponse> days = buildDays(
                shopId,
                weekStartDate,
                selectedDate,
                reservationsByDate
        );

        OwnerCalendarTimelineResponse timeline = buildTimeline(
                shopId,
                selectedDate,
                staffId,
                staffColumns,
                staffById,
                reservationsByDate.getOrDefault(selectedDate, List.of()),
                menuItemsByReservationId,
                customerById
        );

        return OwnerCalendarResponse.builder()
                .shopId(shopId)
                .selectedDate(selectedDate)
                .weekStartDate(weekStartDate)
                .weekEndDate(weekEndDate)
                .days(days)
                .timeline(timeline)
                .build();
    }

    private void validateOwner(Long shopId, Long ownerUserId) {
        User user = userReader.read(ownerUserId);
        if (user.getUserType() != UserType.OWNER) {
            throw new AuthException(AuthErrorCode.ACCESS_DENIED, "캘린더 조회 권한이 없습니다.");
        }
        if (!shopReader.isShopOwnedBy(shopId, ownerUserId)) {
            throw new ShopException(ShopErrorCode.SHOP_OWNER_MISMATCH);
        }
    }

    private List<Staff> resolveStaffColumns(Long shopId, Long staffId) {
        if (staffId == null) {
            return staffReader.findByShopIdOrderByIdAsc(shopId);
        }

        Staff staff = staffReader.read(staffId);
        if (!Objects.equals(staff.getShopId(), shopId)) {
            throw new ShopException(ShopErrorCode.SHOP_OWNER_MISMATCH);
        }
        return List.of(staff);
    }

    private Map<Long, List<ReservationMenuItem>> loadMenuItems(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            return Map.of();
        }

        List<Long> reservationIds = reservations.stream()
                .map(Reservation::getId)
                .toList();
        return reservationMenuItemReader.findByReservationIdIn(reservationIds).stream()
                .sorted(Comparator.comparing(ReservationMenuItem::getReservationId)
                        .thenComparing(ReservationMenuItem::getSortOrder))
                .collect(Collectors.groupingBy(
                        ReservationMenuItem::getReservationId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private Map<Long, User> loadCustomers(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            return Map.of();
        }

        List<Long> customerIds = reservations.stream()
                .map(Reservation::getCustomerId)
                .distinct()
                .toList();
        return userReader.readAllByIds(customerIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user, (a, b) -> a, HashMap::new));
    }

    private List<OwnerCalendarDayResponse> buildDays(
            Long shopId,
            LocalDate weekStartDate,
            LocalDate selectedDate,
            Map<LocalDate, List<Reservation>> reservationsByDate
    ) {
        List<OwnerCalendarDayResponse> days = new ArrayList<>();
        LocalDate cursor = weekStartDate;
        while (!cursor.isAfter(weekStartDate.plusDays(6))) {
            List<Reservation> dayReservations = reservationsByDate.getOrDefault(cursor, List.of());
            days.add(OwnerCalendarDayResponse.builder()
                    .date(cursor)
                    .dayOfWeek(cursor.getDayOfWeek())
                    .dayLabel(toKoreanDayLabel(cursor.getDayOfWeek()))
                    .dayOfMonth(cursor.getDayOfMonth())
                    .selected(cursor.equals(selectedDate))
                    .holiday(holidayChecker.isHoliday(shopId, cursor))
                    .hasPending(dayReservations.stream().anyMatch(r -> r.getStatus() == Reservation.Status.PENDING))
                    .hasConfirmed(dayReservations.stream().anyMatch(r -> r.getStatus() == Reservation.Status.CONFIRMED))
                    .build());
            cursor = cursor.plusDays(1);
        }
        return days;
    }

    private OwnerCalendarTimelineResponse buildTimeline(
            Long shopId,
            LocalDate selectedDate,
            Long staffId,
            List<Staff> staffColumns,
            Map<Long, Staff> staffById,
            List<Reservation> selectedDateReservations,
            Map<Long, List<ReservationMenuItem>> menuItemsByReservationId,
            Map<Long, User> customerById
    ) {
        boolean holiday = holidayChecker.isHoliday(shopId, selectedDate);
        List<OwnerCalendarTimeRangeResponse> shopWorkingRanges = buildShopWorkingRanges(shopId, selectedDate);
        List<OwnerCalendarTimeRangeResponse> visibleWorkingRanges = shopWorkingRanges;

        TimelineBounds bounds = resolveTimelineBounds(selectedDate, selectedDateReservations, visibleWorkingRanges, holiday);

        boolean emptyState = bounds.startTime() == null
                && bounds.endTime() == null
                && selectedDateReservations.isEmpty()
                && visibleWorkingRanges.isEmpty();

        Map<Long, List<Reservation>> reservationsByStaffId = selectedDateReservations.stream()
                .filter(reservation -> reservation.getStaffId() != null)
                .filter(reservation -> staffId == null || Objects.equals(reservation.getStaffId(), staffId))
                .collect(Collectors.groupingBy(Reservation::getStaffId, LinkedHashMap::new, Collectors.toList()));

        List<OwnerCalendarStaffColumnResponse> columns = new ArrayList<>();
        if (!emptyState) {
            for (Staff staff : staffColumns) {
                List<Reservation> staffReservations = reservationsByStaffId.getOrDefault(staff.getId(), List.of());
                List<OwnerCalendarTimeRangeResponse> staffWorkingRanges = buildStaffWorkingRanges(staff.getId(), selectedDate);
                columns.add(buildStaffColumn(
                        staff,
                        staffWorkingRanges,
                        bounds.startTime(),
                        bounds.endTime(),
                        staffReservations,
                        menuItemsByReservationId,
                        customerById
                ));
            }

            if (staffId == null) {
                List<Reservation> unassignedReservations = selectedDateReservations.stream()
                        .filter(reservation -> reservation.getStaffId() == null
                                || !staffById.containsKey(reservation.getStaffId()))
                        .toList();
                if (!unassignedReservations.isEmpty()) {
                    columns.add(buildUnassignedColumn(
                            bounds.startTime(),
                            bounds.endTime(),
                            unassignedReservations,
                            menuItemsByReservationId,
                            customerById
                    ));
                }
            }
        }

        return OwnerCalendarTimelineResponse.builder()
                .date(selectedDate)
                .holiday(holiday)
                .startTime(bounds.startTime())
                .endTime(bounds.endTime())
                .workingRanges(visibleWorkingRanges)
                .unavailableRanges(buildUnavailableRanges(visibleWorkingRanges, bounds.startTime(), bounds.endTime()))
                .staffColumns(columns)
                .build();
    }

    private OwnerCalendarStaffColumnResponse buildStaffColumn(
            Staff staff,
            List<OwnerCalendarTimeRangeResponse> workingRanges,
            LocalTime timelineStart,
            LocalTime timelineEnd,
            List<Reservation> staffReservations,
            Map<Long, List<ReservationMenuItem>> menuItemsByReservationId,
            Map<Long, User> customerById
    ) {
        return OwnerCalendarStaffColumnResponse.builder()
                .staffId(staff.getId())
                .staffName(staff.getName())
                .unassigned(false)
                .workingRanges(workingRanges)
                .unavailableRanges(buildUnavailableRanges(workingRanges, timelineStart, timelineEnd))
                .reservations(buildReservationResponses(staffReservations, staff.getId(), menuItemsByReservationId, customerById))
                .build();
    }

    private OwnerCalendarStaffColumnResponse buildUnassignedColumn(
            LocalTime timelineStart,
            LocalTime timelineEnd,
            List<Reservation> unassignedReservations,
            Map<Long, List<ReservationMenuItem>> menuItemsByReservationId,
            Map<Long, User> customerById
    ) {
        return OwnerCalendarStaffColumnResponse.builder()
                .staffId(null)
                .staffName(UNASSIGNED_STAFF_NAME)
                .unassigned(true)
                .workingRanges(List.of())
                .unavailableRanges(buildUnavailableRanges(List.of(), timelineStart, timelineEnd))
                .reservations(buildReservationResponses(unassignedReservations, null, menuItemsByReservationId, customerById))
                .build();
    }

    private List<OwnerCalendarReservationResponse> buildReservationResponses(
            List<Reservation> reservations,
            Long fixedStaffId,
            Map<Long, List<ReservationMenuItem>> menuItemsByReservationId,
            Map<Long, User> customerById
    ) {
        return reservations.stream()
                .sorted(Comparator.comparing(Reservation::getStartAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Reservation::getId))
                .map(reservation -> {
                    LocalDateTime startAt = reservation.getStartAt() != null
                            ? reservation.getStartAt()
                            : LocalDateTime.of(reservation.getDate(), reservation.getTime());
                    Integer displayDurationMinutes = determineDisplayDurationMinutes(reservation);
                    LocalDateTime endAt = startAt.plusMinutes(displayDurationMinutes);
                    List<ReservationMenuItem> menuItems = menuItemsByReservationId.getOrDefault(reservation.getId(), List.of());
                    String representativeMenuName = menuItems.isEmpty() ? null : menuItems.get(0).getMenuNameSnapshot();
                    String menuSummary = buildMenuSummary(menuItems);
                    User customer = customerById.get(reservation.getCustomerId());
                    return OwnerCalendarReservationResponse.builder()
                            .reservationId(reservation.getId())
                            .status(reservation.getStatus())
                            .startAt(startAt)
                            .endAt(endAt)
                            .durationMinutes(reservation.getDurationMinutes())
                            .displayDurationMinutes(displayDurationMinutes)
                            .staffId(fixedStaffId != null ? fixedStaffId : reservation.getStaffId())
                            .customerName(customer != null ? customer.getName() : "알 수 없음")
                            .representativeMenuName(representativeMenuName)
                            .menuCount(menuItems.size())
                            .menuSummary(menuSummary)
                            .build();
                })
                .toList();
    }

    private List<OwnerCalendarTimeRangeResponse> buildShopWorkingRanges(Long shopId, LocalDate date) {
        return shopOperatingTimeReader.readByShopIdAndDayOfWeek(shopId, date.getDayOfWeek()).stream()
                .sorted(Comparator.comparing(ShopOperatingTime::getStartTime))
                .map(this::toRange)
                .toList();
    }

    private List<OwnerCalendarTimeRangeResponse> buildStaffWorkingRanges(Long staffId, LocalDate date) {
        return operatingTimeResolver.resolve(staffId, date.getDayOfWeek()).stream()
                .map(this::toRange)
                .toList();
    }

    private OwnerCalendarTimeRangeResponse toRange(ShopOperatingTime operatingTime) {
        return OwnerCalendarTimeRangeResponse.builder()
                .startTime(operatingTime.getStartTime())
                .endTime(operatingTime.getEndTime())
                .build();
    }

    private OwnerCalendarTimeRangeResponse toRange(ShopTimeRangeResponse range) {
        return OwnerCalendarTimeRangeResponse.builder()
                .startTime(range.getStart())
                .endTime(range.getEnd())
                .build();
    }

    private List<OwnerCalendarTimeRangeResponse> buildUnavailableRanges(
            List<OwnerCalendarTimeRangeResponse> workingRanges,
            LocalTime timelineStart,
            LocalTime timelineEnd
    ) {
        if (timelineStart == null || timelineEnd == null) {
            return List.of();
        }

        List<OwnerCalendarTimeRangeResponse> sortedWorkingRanges = workingRanges.stream()
                .sorted(Comparator.comparing(OwnerCalendarTimeRangeResponse::getStartTime))
                .toList();
        if (sortedWorkingRanges.isEmpty()) {
            return List.of();
        }

        List<OwnerCalendarTimeRangeResponse> unavailableRanges = new ArrayList<>();
        LocalTime cursor = timelineStart;
        for (OwnerCalendarTimeRangeResponse range : sortedWorkingRanges) {
            if (cursor.isBefore(range.getStartTime())) {
                unavailableRanges.add(OwnerCalendarTimeRangeResponse.builder()
                        .startTime(cursor)
                        .endTime(range.getStartTime())
                        .build());
            }
            if (cursor.isBefore(range.getEndTime())) {
                cursor = range.getEndTime();
            }
        }
        if (cursor.isBefore(timelineEnd)) {
            unavailableRanges.add(OwnerCalendarTimeRangeResponse.builder()
                    .startTime(cursor)
                    .endTime(timelineEnd)
                    .build());
        }
        return unavailableRanges;
    }

    private TimelineBounds resolveTimelineBounds(
            LocalDate selectedDate,
            List<Reservation> selectedDateReservations,
            List<OwnerCalendarTimeRangeResponse> shopWorkingRanges,
            boolean holiday
    ) {
        if (!shopWorkingRanges.isEmpty() && !holiday) {
            LocalTime startTime = shopWorkingRanges.stream()
                    .map(OwnerCalendarTimeRangeResponse::getStartTime)
                    .min(LocalTime::compareTo)
                    .orElse(null);
            LocalTime endTime = shopWorkingRanges.stream()
                    .map(OwnerCalendarTimeRangeResponse::getEndTime)
                    .max(LocalTime::compareTo)
                    .orElse(null);
            return new TimelineBounds(startTime, endTime);
        }

        if (selectedDateReservations.isEmpty()) {
            return new TimelineBounds(null, null);
        }

        LocalDateTime earliest = selectedDateReservations.stream()
                .map(this::resolveStartAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime latest = selectedDateReservations.stream()
                .map(this::resolveEndAtForTimeline)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (earliest == null || latest == null) {
            return new TimelineBounds(null, null);
        }

        LocalTime startTime = clampStart(earliest.toLocalTime(), RESERVED_PADDING_MINUTES);
        LocalTime endTime = clampEnd(latest.toLocalTime(), RESERVED_PADDING_MINUTES);
        return new TimelineBounds(startTime, endTime);
    }

    private LocalTime clampStart(LocalTime time, int paddingMinutes) {
        int minutes = time.getHour() * 60 + time.getMinute() - paddingMinutes;
        if (minutes < 0) {
            minutes = 0;
        }
        return LocalTime.of(minutes / 60, minutes % 60);
    }

    private LocalTime clampEnd(LocalTime time, int paddingMinutes) {
        int minutes = time.getHour() * 60 + time.getMinute() + paddingMinutes;
        if (minutes > 23 * 60 + 59) {
            minutes = 23 * 60 + 59;
        }
        return LocalTime.of(minutes / 60, minutes % 60);
    }

    private LocalDateTime resolveStartAt(Reservation reservation) {
        return reservation.getStartAt() != null
                ? reservation.getStartAt()
                : LocalDateTime.of(reservation.getDate(), reservation.getTime());
    }

    private LocalDateTime resolveEndAtForTimeline(Reservation reservation) {
        return resolveStartAt(reservation).plusMinutes(determineDisplayDurationMinutes(reservation));
    }

    private Integer determineDisplayDurationMinutes(Reservation reservation) {
        if (reservation.getStatus() == Reservation.Status.PENDING) {
            return PENDING_DISPLAY_DURATION_MINUTES;
        }
        if (reservation.getDurationMinutes() != null) {
            return reservation.getDurationMinutes();
        }
        return PENDING_DISPLAY_DURATION_MINUTES;
    }

    private String buildMenuSummary(List<ReservationMenuItem> menuItems) {
        if (menuItems.isEmpty()) {
            return null;
        }
        String first = menuItems.get(0).getMenuNameSnapshot();
        if (menuItems.size() == 1) {
            return first;
        }
        return first + " 외 " + (menuItems.size() - 1) + "개";
    }

    private String toKoreanDayLabel(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case SUNDAY -> "일";
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
        };
    }
    private record TimelineBounds(LocalTime startTime, LocalTime endTime) {
    }
}
