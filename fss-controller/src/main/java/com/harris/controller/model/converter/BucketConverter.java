package com.harris.controller.model.converter;

import com.harris.app.model.command.ArrangeBucketCommand;
import com.harris.controller.model.request.ArrangeBucketRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BucketConverter {
    public static ArrangeBucketCommand toCommand(ArrangeBucketRequest arrangeBucketRequest) {
        if (arrangeBucketRequest == null) {
            return null;
        }

        ArrangeBucketCommand bucketsArrangementCommand = new ArrangeBucketCommand();
        BeanUtils.copyProperties(arrangeBucketRequest, bucketsArrangementCommand);

        return bucketsArrangementCommand;
    }
}
