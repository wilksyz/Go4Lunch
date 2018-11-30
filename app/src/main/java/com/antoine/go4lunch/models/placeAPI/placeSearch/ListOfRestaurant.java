package com.antoine.go4lunch.models.placeAPI.placeSearch;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListOfRestaurant {

    @SerializedName("results")
    @Expose
    private List<Results> results = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<Results> getResults() {
        return results;
    }

    public void setResults(List<Results> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
