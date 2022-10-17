package com.xiaoqigao.creditcardrewards.response;

import com.xiaoqigao.creditcardrewards.enums.Status;
import lombok.Value;

import java.util.List;

@Value
public class MonthlyReportResponse extends CommonResponse {

    //  total points
    // hashmap of transaction name: points

    private String year;
    private String month;
    private int maximum_monthly_rewards_point;
    private List<TransactionLevelPointResponse> transaction_level_points_list;

    public MonthlyReportResponse(String year, String month, int maximum_monthly_rewards_point, List<TransactionLevelPointResponse> transaction_level_point_list) {
        super(Status.OK);
        this.year = year;
        this.month = month;
        this.maximum_monthly_rewards_point = maximum_monthly_rewards_point;
        this.transaction_level_points_list = transaction_level_point_list;
    }
}
