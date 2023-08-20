package com.hanfei.flashsales.vo;

import com.hanfei.flashsales.pojo.Activity;
import com.hanfei.flashsales.pojo.Commodity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailVO {

    private Activity activity;

    private Commodity commodity;

    private Long userId;

    private String username;

    private int secKillStatus;

    private int remainSeconds;
}
