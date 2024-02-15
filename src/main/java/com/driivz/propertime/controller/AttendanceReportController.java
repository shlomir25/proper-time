package com.driivz.propertime.controller;

import com.driivz.propertime.model.AttendancePeriodDto;
import com.driivz.propertime.model.InfoDto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.driivz.propertime.AttendanceReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController()
@RequestMapping("/attendance-report")
public class AttendanceReportController {

    public AttendanceReportController(AttendanceReportService attendanceReportService) {
        this.attendanceReportService = attendanceReportService;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private static final Logger log = LoggerFactory.getLogger(AttendanceReportController.class);

    private final AttendanceReportService attendanceReportService;
    private final Gson gson;

    @PostMapping("/save-time-period")
    public ResponseEntity<List<InfoDto>> saveTimePeriod(@RequestBody AttendancePeriodDto attendancePeriodDto,
                                                        @RequestHeader String sessionCookie) {
        log.info("STARTING Endpoint /attendance-report/save-time-period, with the request body: {}", gson.toJson(attendancePeriodDto));
        List<InfoDto> res;
        try {
            res = attendanceReportService.saveTimePeriod(attendancePeriodDto, sessionCookie);
        } catch (IOException e) {
            log.error("Error in Endpoint /attendance-report/save-time-period, : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        log.info("FINISHED Endpoint /attendance-report/save-time-period, with the response body: {}", gson.toJson(res));
        return ResponseEntity.ok(res);
    }
}
