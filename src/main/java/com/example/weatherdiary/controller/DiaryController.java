package com.example.weatherdiary.controller;

import com.example.weatherdiary.domain.Diary;
import com.example.weatherdiary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @Operation(summary = "일기 텍스트와 날씨를 이용해서 DB에 일기 저장")
    @PostMapping("/create/diary")
    public void createDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "날짜양식: 2023-01-01") LocalDate date,
                            @RequestBody String text) {
        diaryService.createDiary(date, text);
    }

    @Operation(summary = "선택한 날짜의 모든 일기 데이터를 가져옵니다.")
    @GetMapping("/read/diary")
    public List<Diary> readDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "날짜양식: 2023-01-01") LocalDate date) {
        return diaryService.readDiary(date);
    }

    @Operation(summary = "선택한 기간중의 모든 일기 데이터를 가져옵니다.")
    @GetMapping("/read/diaries")
    public List<Diary> readDiaries(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "날짜양식: 2023-01-01") LocalDate startDate,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "날짜양식: 2023-01-01") LocalDate endDate) {
        return diaryService.readDiaries(startDate, endDate);
    }

    @Operation(summary = "선택한 날짜의 첫번째 일기 데이터를 수정")
    @PutMapping("/update/diary")
    public void updateDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "날짜양식: 2023-01-01") LocalDate date,
                            @RequestBody String text) {
        diaryService.updateDiary(date, text);
    }

    @Operation(summary = "선택한 날짜의 모든 일기 데이터를 삭제")
    @DeleteMapping("/delete/diary")
    public void deleteDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "날짜양식: 2023-01-01") LocalDate date) {
        diaryService.deleteDiary(date);
    }
}
