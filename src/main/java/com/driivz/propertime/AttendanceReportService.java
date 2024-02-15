package com.driivz.propertime;

import com.driivz.propertime.model.AttendancePeriodDto;
import com.driivz.propertime.model.DailyAttendanceDto;
import com.driivz.propertime.model.InfoDto;
import com.driivz.propertime.model.PunchDto;
import com.driivz.propertime.model.ResponseDto;
import com.driivz.propertime.model.TaskEntryDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AttendanceReportService {

    public AttendanceReportService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private static final Logger log = LoggerFactory.getLogger(AttendanceReportService.class);
    private static final String PUNCH_URL = "https://app.propertime.co.il/api/punches";
    private static final String SAVE_DAILY_CHANGES_URL = "https://app.propertime.co.il/api/daily/save-changes";
    private static final String CLIENT_ID = "6d84c2ef-1cc5-47bd-a253-9478cfc54635";
    private static final String PROJECT_ID = "b9415f05-5c53-492c-9b2e-56f793927c0e";
    private static final String TASK_ID = "51cfc7e0-af1f-4459-995e-895d1f2c936e";
    private static final String TASK_NAME = "שוטף";
    private static final List<Pair<String, String>> dailyTimeRange = List.of(
            Pair.of("09:40", "19:00"),
            Pair.of("09:50", "18:55"),
            Pair.of("09:45", "18:35"),
            Pair.of("09:55", "18:55")
    );
    private final Gson gson;

    public List<InfoDto> saveTimePeriod(AttendancePeriodDto attendancePeriodDto, String sessionCookie) throws IOException {
        List<InfoDto> errorList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate attendanceStartDate = LocalDate.parse(attendancePeriodDto.getStartDate(), formatter);
        LocalDate attendanceEndDate = LocalDate.parse(attendancePeriodDto.getEndDate(), formatter);

        int numOfDaysBetween = Long.valueOf(ChronoUnit.DAYS.between(attendanceStartDate, attendanceEndDate) + 1).intValue();
        List<LocalDate> attendanceDays = IntStream.iterate(0, i -> i + 1)
                .limit(numOfDaysBetween)
                .mapToObj(attendanceStartDate::plusDays)
                .filter(it -> it.getDayOfWeek() != DayOfWeek.FRIDAY && it.getDayOfWeek() != DayOfWeek.SATURDAY)
                .collect(Collectors.toList());

        for (int i=0; i < attendanceDays.size(); i++) {

            ObjectMapper objectMapper = new ObjectMapper();
            CloseableHttpClient httpClient = new DefaultHttpClient();
            HttpPost request = new HttpPost(PUNCH_URL);
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Cookie", sessionCookie);
            String dayAsString = attendanceDays.get(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String dailyStartDate = dailyTimeRange.get(i % dailyTimeRange.size()).getFirst();
            String dailyEndDate = dailyTimeRange.get(i % dailyTimeRange.size()).getSecond();

            PunchDto punchInDto = PunchDto.builder()
                    .time(dailyStartDate)
                    .date(dayAsString)
                    .kind("in")
                    .ignore_locked(null)
                    .build();
            String bodyAsString = gson.toJson(punchInDto);
            request.setEntity(new StringEntity(bodyAsString));
            HttpResponse result = httpClient.execute(request);
            String resultAsString = EntityUtils.toString(result.getEntity(), "UTF-8");
            ResponseDto responseDto = objectMapper.readValue(resultAsString, new TypeReference<>(){});

            if(responseDto.getHas_error()) {
                errorList.add(responseDto.getError());
            }

            PunchDto punchOutDto = PunchDto.builder()
                    .time(dailyEndDate)
                    .date(dayAsString)
                    .kind("out")
                    .ignore_locked(null)
                    .build();
            bodyAsString = gson.toJson(punchOutDto);
            request.setEntity(new StringEntity(bodyAsString));
            result = httpClient.execute(request);
            resultAsString = EntityUtils.toString(result.getEntity(), "UTF-8");
            responseDto = objectMapper.readValue(resultAsString, ResponseDto.class);

            if(responseDto.getHas_error()) {
                errorList.add(responseDto.getError());
            }

            request.setURI(URI.create(SAVE_DAILY_CHANGES_URL));

            TaskEntryDto taskEntryDto = TaskEntryDto.builder()
                .client_id(CLIENT_ID)
                .end_time(dailyEndDate)
                .expenses(List.of())
                .id("")
                .is_dirty(true)
                .project_id(PROJECT_ID)
                .remarks("")
                .start_time(dailyStartDate)
                .task_id(TASK_ID)
                .task_name(TASK_NAME)
                .build();

            DailyAttendanceDto dailyAttendanceDto = DailyAttendanceDto.builder()
                    .ignore_locked(null)
                    .date(dayAsString)
                    .entries(List.of(taskEntryDto))
                    .deleted(new ArrayList<>())
                    .build();

            bodyAsString = gson.toJson(dailyAttendanceDto);
            request.setEntity(new StringEntity(bodyAsString));
            result = httpClient.execute(request);
            resultAsString = EntityUtils.toString(result.getEntity(), "UTF-8");
            responseDto = objectMapper.readValue(resultAsString, ResponseDto.class);

            if(responseDto.getHas_error()) {
                errorList.add(responseDto.getError());
            }
        }

        return errorList;
    }
}