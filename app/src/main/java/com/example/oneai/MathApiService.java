package com.example.oneai;

import com.google.gson.JsonElement;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MathApiService {
    @GET("evaluate")
    Call<String> solveExpression(@Query("expr") String expression);
}
