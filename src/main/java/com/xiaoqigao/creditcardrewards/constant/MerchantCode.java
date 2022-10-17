package com.xiaoqigao.creditcardrewards.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores merchant codes as constants.
 */
public class MerchantCode {

    /**
     * Formalizes constants for all merchant codes.
     */
    public static final String OTHER = "other";
    public static final String SPORT_CHECK = "sportcheck";
    public static final String TIM_HORTONS = "tim_hortons";
    public static final String SUBWAY = "subway";

    /**
     * Makes a list of merchants that are mentioned in the reward rules.
     * @return a list of merchants
     */
    public static List<String> makeRewardMerchantList() {
        List<String> merchantsForReward = new ArrayList<>();
        merchantsForReward.add(MerchantCode.SPORT_CHECK);
        merchantsForReward.add(MerchantCode.TIM_HORTONS);
        merchantsForReward.add(MerchantCode.SUBWAY);

        return merchantsForReward;
    }


}
