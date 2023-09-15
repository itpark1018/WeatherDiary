package com.example.weatherdiary.service;

import com.example.weatherdiary.WeatherDiaryApplication;
import com.example.weatherdiary.domain.DateWeather;
import com.example.weatherdiary.domain.Diary;
import com.example.weatherdiary.repository.DateWeatherRepository;
import com.example.weatherdiary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DateWeatherRepository dateWeatherRepository;
    private final DiaryRepository diaryRepository;
    private static final Logger logger = LoggerFactory.getLogger(WeatherDiaryApplication.class);

    // 날씨 정보와 함께 일기 쓰기
    @Transactional
    public void createDiary(LocalDate date, String text) {
        logger.info("started to create diary");

        // 날씨 데이터 가져오기
        DateWeather dateWeather = getWeatherData(date);

        // 파싱된 데이터 + 일기 DB에 저장하기
        Diary diary = new Diary();
        diary.setDateWeather(dateWeather);
        diary.setText(text);
        diaryRepository.save(diary);

        logger.info("end to create diary");
    }

    private DateWeather getWeatherData(LocalDate date) {
        // DB에서 데이터를 가져온다.
        List<DateWeather> dateWeatherList = dateWeatherRepository.findAllByDate(date);

        // DB에 데이터가 없다면 현재 날씨 데이터를 보낸다.
        if (dateWeatherList.size() == 0) {
            return getDateWeather();
        } else {
            return dateWeatherList.get(0);
        }
    }

    // 매일 새벽 1시마다 받아온 날씨정보 DB에 저장
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveDateWeather() {
        logger.info("날씨 데이터 잘 가져옴");
        dateWeatherRepository.save(getDateWeather());
    }

    // open api에서 날씨정보 받아오고 파싱하기
    private DateWeather getDateWeather() {
        // open api에서 날씨 데이터 받아오기
        String weatherData = getWeatherString();

        // 받아온 날씨 데이터 파싱하기
        Map<String, Object> parseWeather = parseDateWeather(weatherData);
        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather(parseWeather.get("main").toString());
        dateWeather.setIcon(parseWeather.get("icon").toString());
        dateWeather.setTemperature((Double) parseWeather.get("tepm"));

        return dateWeather;
    }

    //받아온 날씨정보를 파싱
    private Map<String, Object> parseDateWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> resultMap = new HashMap<>();

        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));

        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0);
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));

        return resultMap;
    }

    // Open Api를 통해서 서울의 날씨정보를 String으로 받아오기
    @Value("${myapp.openapi.api-key}")
    private String apikey;
    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apikey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            return response.toString();
        } catch (Exception e) {
            return "failed to get reponse";
        }
    }

    // 날짜에 해당하는 일기들을 가져오기
    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        logger.debug("read diary");
        return diaryRepository.findAllByDate(date);
    }

    // 기간에 해당하는 일기들을 가져오기
    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        logger.debug("read diaries");
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    @Transactional
    public void updateDiary(LocalDate date, String text) {
        logger.debug("update diary");
        Diary diary = diaryRepository.getFirstByDate(date);
        diary.setText(text);
        diaryRepository.save(diary);
    }

    @Transactional
    public void deleteDiary(LocalDate date) {
        logger.debug("delete diary");
        diaryRepository.deleteAllByDate(date);
    }
}
