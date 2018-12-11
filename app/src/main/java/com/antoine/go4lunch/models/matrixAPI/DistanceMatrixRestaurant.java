package com.antoine.go4lunch.models.matrixAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DistanceMatrixRestaurant {

    @SerializedName("rows")
    @Expose
    private List<Row> rows = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
