package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.StaffOperatingTime;
import com.example.easybooking.availability.dto.request.StaffOperatingTimeOverrideRequest;
import com.example.easybooking.availability.dto.request.UpdateStaffOperatingTimesRequest;
import com.example.easybooking.availability.dto.response.StaffOperatingTimeOverrideResponse;
import com.example.easybooking.availability.dto.response.StaffOperatingTimesResponse;
import com.example.easybooking.availability.exception.StaffOverrideOutOfShopRangeException;
import com.example.easybooking.availability.repository.StaffOperatingTimeRepository;
import com.example.easybooking.errors.errorcode.ShopErrorCode;
import com.example.easybooking.errors.exception.ShopException;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.staff.StaffReader;
import com.example.easybooking.staff.domain.Staff;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffOperatingTimesService {

    private final ShopReader shopReader;
    private final StaffReader staffReader;
    private final StaffOperatingTimeRepository staffOperatingTimeRepository;
    private final ShopOperatingTimeReader shopOperatingTimeReader;

    public void updateStaffOperatingTimes(
            Long shopId,
            Long staffId,
            Long ownerUserId,
            UpdateStaffOperatingTimesRequest request
    ) {
        validateOwner(shopId, ownerUserId);
        validateStaffInShop(shopId, staffId);
        validateOverridesWithinShopRange(shopId, request.getOverrides());

        staffOperatingTimeRepository.deleteByStaffId(staffId);
        List<StaffOperatingTime> overrides = mapOverrides(staffId, request.getOverrides());
        staffOperatingTimeRepository.saveAll(overrides);
    }

    public StaffOperatingTimesResponse getStaffOperatingTimes(Long shopId, Long staffId, Long ownerUserId) {
        validateOwner(shopId, ownerUserId);
        validateStaffInShop(shopId, staffId);
        List<StaffOperatingTimeOverrideResponse> overrides = staffOperatingTimeRepository.findByStaffId(staffId)
                .stream()
                .sorted(Comparator.comparing(StaffOperatingTime::getDayOfWeek))
                .map(StaffOperatingTimeOverrideResponse::from)
                .toList();
        return StaffOperatingTimesResponse.builder()
                .overrides(overrides)
                .build();
    }

    private void validateOwner(Long shopId, Long ownerUserId) {
        if (!shopReader.isShopOwnedBy(shopId, ownerUserId)) {
            throw new ShopException(ShopErrorCode.SHOP_OWNER_MISMATCH);
        }
    }

    private void validateStaffInShop(Long shopId, Long staffId) {
        Staff staff = staffReader.read(staffId);
        if (!staff.getShopId().equals(shopId)) {
            throw new ShopException(ShopErrorCode.SHOP_OWNER_MISMATCH);
        }
    }

    private List<StaffOperatingTime> mapOverrides(Long staffId, List<StaffOperatingTimeOverrideRequest> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return List.of();
        }

        return overrides.stream()
                .map(override -> override.isOff()
                        ? StaffOperatingTime.createOff(staffId, override.getDayOfWeek())
                        : StaffOperatingTime.create(
                                staffId,
                                override.getDayOfWeek(),
                                override.getStart(),
                                override.getEnd()
                        ))
                .toList();
    }

    private void validateOverridesWithinShopRange(Long shopId, List<StaffOperatingTimeOverrideRequest> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return;
        }

        for (StaffOperatingTimeOverrideRequest override : overrides) {
            if (override.isOff()) {
                continue;
            }
            if (override.getStart() == null || override.getEnd() == null) {
                throw new StaffOverrideOutOfShopRangeException();
            }

            boolean insideAnyRange = shopOperatingTimeReader
                    .readByShopIdAndDayOfWeek(shopId, override.getDayOfWeek())
                    .stream()
                    .anyMatch(shopTime -> isInsideRange(
                            override.getStart(),
                            override.getEnd(),
                            shopTime.getStartTime(),
                            shopTime.getEndTime()
                    ));

            if (!insideAnyRange) {
                throw new StaffOverrideOutOfShopRangeException();
            }
        }
    }

    private boolean isInsideRange(
            LocalTime overrideStart,
            LocalTime overrideEnd,
            LocalTime shopStart,
            LocalTime shopEnd
    ) {
        return !overrideStart.isBefore(shopStart) && !overrideEnd.isAfter(shopEnd);
    }
}
