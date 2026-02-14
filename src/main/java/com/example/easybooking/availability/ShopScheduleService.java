package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.ShopSettings;
import com.example.easybooking.availability.dto.request.ShopTimeRangeRequest;
import com.example.easybooking.availability.dto.request.UpdateShopOperatingTimesRequest;
import com.example.easybooking.availability.dto.request.UpdateShopScheduleSettingsRequest;
import com.example.easybooking.availability.dto.response.ShopOperatingTimesResponse;
import com.example.easybooking.availability.dto.response.ShopScheduleSettingsResponse;
import com.example.easybooking.availability.dto.response.ShopTimeRangeResponse;
import com.example.easybooking.errors.errorcode.ShopErrorCode;
import com.example.easybooking.errors.exception.ShopException;
import com.example.easybooking.shop.ShopReader;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopScheduleService {

    private final ShopReader shopReader;
    private final ShopSettingsReader shopSettingsReader;
    private final ShopSettingsWriter shopSettingsWriter;
    private final ShopOperatingTimeWriter shopOperatingTimeWriter;
    private final ShopOperatingTimeReader shopOperatingTimeReader;

    public ShopScheduleSettingsResponse getSettings(Long shopId, Long ownerUserId) {
        validateOwner(shopId, ownerUserId);
        ShopSettings shopSettings = shopSettingsReader.readByShopId(shopId);
        return ShopScheduleSettingsResponse.from(shopSettings);
    }

    public ShopScheduleSettingsResponse updateSettings(
            Long shopId,
            Long ownerUserId,
            UpdateShopScheduleSettingsRequest request
    ) {
        validateOwner(shopId, ownerUserId);
        ShopSettings shopSettings = shopSettingsReader.readByShopId(shopId);
        shopSettings.updateInterval(request.getIntervalMinutes());
        ShopSettings saved = shopSettingsWriter.save(shopSettings);
        return ShopScheduleSettingsResponse.from(saved);
    }

    private void validateOwner(Long shopId, Long ownerUserId) {
        if (!shopReader.isShopOwnedBy(shopId, ownerUserId)) {
            throw new ShopException(ShopErrorCode.SHOP_OWNER_MISMATCH);
        }
    }

    public ShopOperatingTimesResponse getOperatingTimes(Long shopId, Long ownerUserId) {
        validateOwner(shopId, ownerUserId);
        ShopSettings shopSettings = shopSettingsReader.readByShopId(shopId);
        List<com.example.easybooking.availability.domain.ShopOperatingTime> operatingTimes =
                shopOperatingTimeReader.readByShopId(shopId);
        Map<DayOfWeek, List<ShopTimeRangeResponse>> dayTimes = buildDayTimes(operatingTimes);
        return ShopOperatingTimesResponse.builder()
                .scheduleType(shopSettings.getScheduleType())
                .dayTimes(dayTimes)
                .build();
    }

    public void updateOperatingTimes(
            Long shopId,
            Long ownerUserId,
            UpdateShopOperatingTimesRequest request
    ) {
        validateOwner(shopId, ownerUserId);
        ShopSettings shopSettings = shopSettingsReader.readByShopId(shopId);
        shopSettings.updateScheduleType(request.getScheduleType());
        shopSettingsWriter.save(shopSettings);

        List<com.example.easybooking.availability.domain.ShopOperatingTime> rows =
                buildOperatingTimes(shopId, request);
        shopOperatingTimeWriter.replaceAll(shopId, rows);
    }

    private List<com.example.easybooking.availability.domain.ShopOperatingTime> buildOperatingTimes(
            Long shopId,
            UpdateShopOperatingTimesRequest request
    ) {
        List<com.example.easybooking.availability.domain.ShopOperatingTime> rows = new ArrayList<>();
        if (request.getScheduleType() == null) {
            return rows;
        }

        if (request.getScheduleType().name().equals("DAILY")) {
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                appendRanges(rows, shopId, dayOfWeek, request.getTimes());
            }
            return rows;
        }

        if (request.getScheduleType().name().equals("WEEKDAY_WEEKEND")) {
            for (DayOfWeek dayOfWeek : EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)) {
                appendRanges(rows, shopId, dayOfWeek, request.getWeekdayTimes());
            }
            appendRanges(rows, shopId, DayOfWeek.SATURDAY, request.getWeekendTimes());
            appendRanges(rows, shopId, DayOfWeek.SUNDAY, request.getWeekendTimes());
            return rows;
        }

        if (request.getScheduleType().name().equals("BY_DAY") && request.getDayTimes() != null) {
            request.getDayTimes().forEach((dayOfWeek, ranges) -> appendRanges(rows, shopId, dayOfWeek, ranges));
        }
        return rows;
    }

    private void appendRanges(
            List<com.example.easybooking.availability.domain.ShopOperatingTime> rows,
            Long shopId,
            DayOfWeek dayOfWeek,
            List<ShopTimeRangeRequest> ranges
    ) {
        if (ranges == null || ranges.isEmpty()) {
            return;
        }
        ranges.forEach(range -> rows.add(com.example.easybooking.availability.domain.ShopOperatingTime.create(
                shopId,
                dayOfWeek,
                range.getStart(),
                range.getEnd()
        )));
    }

    private Map<DayOfWeek, List<ShopTimeRangeResponse>> buildDayTimes(
            List<com.example.easybooking.availability.domain.ShopOperatingTime> operatingTimes
    ) {
        Map<DayOfWeek, List<ShopTimeRangeResponse>> result = new LinkedHashMap<>();
        operatingTimes.stream()
                .sorted(Comparator.comparing(com.example.easybooking.availability.domain.ShopOperatingTime::getDayOfWeek)
                        .thenComparing(com.example.easybooking.availability.domain.ShopOperatingTime::getStartTime))
                .forEach(row -> result.computeIfAbsent(row.getDayOfWeek(), key -> new ArrayList<>())
                        .add(ShopTimeRangeResponse.builder()
                                .start(row.getStartTime())
                                .end(row.getEndTime())
                                .build()));
        return result;
    }
}
