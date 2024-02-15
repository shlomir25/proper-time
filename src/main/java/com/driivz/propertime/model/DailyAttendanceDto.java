package com.driivz.propertime.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DailyAttendanceDto {

    private Boolean ignore_locked;
    private String date;
    private List<TaskEntryDto> entries;
    private List<TaskEntryDto> deleted;


}
