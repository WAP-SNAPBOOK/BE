package com.example.easybooking.availability.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateStaffOperatingTimesRequest {

    private List<StaffOperatingTimeOverrideRequest> overrides;
}
