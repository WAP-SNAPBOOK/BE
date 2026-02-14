package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.HolidayType;
import com.example.easybooking.availability.domain.ShopHoliday;
import com.example.easybooking.availability.repository.PublicHolidayRepository;
import com.example.easybooking.availability.repository.ShopHolidayRepository;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HolidayChecker {

    private final ShopHolidayRepository shopHolidayRepository;
    private final PublicHolidayRepository publicHolidayRepository;
    private final ShopSettingsRepository shopSettingsRepository;

    public boolean isHoliday(Long shopId, LocalDate date) {
        List<ShopHoliday> shopHolidays = shopHolidayRepository.findByShopId(shopId);
        for (ShopHoliday shopHoliday : shopHolidays) {
            if (matches(shopHoliday, date)) {
                return true;
            }
        }

        boolean publicHolidayOff = shopSettingsRepository.findByShopId(shopId)
                .map(settings -> settings.isPublicHolidayOff())
                .orElse(false);

        return publicHolidayOff && publicHolidayRepository.existsByHolidayDate(date);
    }

    private boolean matches(ShopHoliday shopHoliday, LocalDate date) {
        HolidayType holidayType = shopHoliday.getHolidayType();

        if (holidayType == HolidayType.WEEKLY) {
            return shopHoliday.getDayOfWeek() == date.getDayOfWeek();
        }

        if (holidayType == HolidayType.BIWEEKLY) {
            if (shopHoliday.getDayOfWeek() != date.getDayOfWeek() || shopHoliday.getReferenceDate() == null) {
                return false;
            }
            long weeks = ChronoUnit.WEEKS.between(shopHoliday.getReferenceDate(), date);
            return weeks % 2 == 0;
        }

//        TODO : 몇 주차인지 계산하는 로직이 부정확함.
//        https://chatgpt.com/s/t_698e90eca7f08191a0ac922ac0370b95 해당 링크 참고
        if (holidayType == HolidayType.MONTHLY) {
            if (shopHoliday.getWeekOfMonth() == null || shopHoliday.getDayOfWeek() == null) {
                return false;
            }
            int weekOfMonth = ((date.getDayOfMonth() - 1) / 7) + 1;
            return weekOfMonth == shopHoliday.getWeekOfMonth()
                    && date.getDayOfWeek() == shopHoliday.getDayOfWeek();
        }

        if (holidayType == HolidayType.CUSTOM) {
            return date.equals(shopHoliday.getSpecificDate());
        }

        return false;
    }
}
