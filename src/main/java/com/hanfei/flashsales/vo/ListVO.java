package com.hanfei.flashsales.vo;

import com.hanfei.flashsales.pojo.Activity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListVO extends Activity {

    private String commodityName;

    private String commodityImg;

    private String commodityDetail;
}
