package com.xiaoqigao.creditcardrewards.constant;

import java.util.ArrayList;
import java.util.List;

// stores the name of the merchant code
public class MerchantCode {

    public static final String OTHER = "other";
    public static final String SPORT_CHECK = "sportcheck";
    public static final String TIM_HORTONS = "tim_hortons";
    public static final String SUBWAY = "subway";

    public static List<String> makeRewardMerchantList() {
        List<String> merchantsForReward = new ArrayList<>();
        merchantsForReward.add(MerchantCode.SPORT_CHECK);
        merchantsForReward.add(MerchantCode.TIM_HORTONS);
        merchantsForReward.add(MerchantCode.SUBWAY);

        return merchantsForReward;
    }


}
