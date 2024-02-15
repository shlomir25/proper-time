package com.driivz.propertime.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TaskEntryDto {
    private String client_id;
    private String end_time;
    private List expenses;
    private String id;
    private Boolean is_dirty;
    private String project_id;
    private String remarks;
    private String start_time;
    private String task_id;
    private String task_name;
}
