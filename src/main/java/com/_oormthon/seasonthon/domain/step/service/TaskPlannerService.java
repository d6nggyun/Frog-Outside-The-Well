package com._oormthon.seasonthon.domain.step.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskPlannerService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api-key}")
    private String apiKey;

//    @Transactional(readOnly = true)
//    public List<StepResponse> generatePlan() {
//
//    }
}
