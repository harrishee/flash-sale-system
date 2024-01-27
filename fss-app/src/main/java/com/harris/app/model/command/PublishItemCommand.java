package com.harris.app.model.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Data
@Accessors(chain = true)
public class PublishItemCommand {
    private String itemTitle;
    private String itemSubTitle;
    private String itemDesc;
    private Integer initialStock;
    private Integer availableStock;
    private Long originalPrice;
    private Long salePrice;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    public boolean invalidParams() {
        return !StringUtils.isNotEmpty(itemTitle) || !StringUtils.isNotEmpty(itemSubTitle) ||
                initialStock == null || initialStock <= 0 ||
                availableStock == null || availableStock <= 0 ||
                originalPrice == null || originalPrice <= 0 ||
                salePrice == null || salePrice <= 0 ||
                startTime == null || endTime == null ||
                !startTime.before(endTime);
    }
}
