package com.driivz.propertime.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PunchDto {
    private String ignore_locked;
    private String kind;
    private String date;
    private String time;
}
