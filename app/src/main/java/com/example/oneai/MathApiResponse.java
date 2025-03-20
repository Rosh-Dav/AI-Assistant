package com.example.oneai;

public class MathApiResponse {
    private double result;

    // If the API returns a number directly, update this to accept the result directly
    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }
}