package com.example.carrental.dto;

import java.util.List;

public class AiSearchResponse {

    private AiSearchCriteria criteria;
    private List<CarResponse> cars;

    public AiSearchResponse() {
    }

    public AiSearchResponse(AiSearchCriteria criteria, List<CarResponse> cars) {
        this.criteria = criteria;
        this.cars = cars;
    }

    public AiSearchCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(AiSearchCriteria criteria) {
        this.criteria = criteria;
    }

    public List<CarResponse> getCars() {
        return cars;
    }

    public void setCars(List<CarResponse> cars) {
        this.cars = cars;
    }
}
