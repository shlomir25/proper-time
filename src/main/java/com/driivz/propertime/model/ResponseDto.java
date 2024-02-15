package com.driivz.propertime.model;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {
    private String status;
    private Boolean has_error;
    private String created_entry_id;
    private List<InfoDto> warnings;
    private InfoDto error;
}
