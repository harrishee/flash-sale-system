package com.harris.controller.model.converter;

import com.harris.app.model.command.BucketArrangementCommand;
import com.harris.controller.model.request.BucketArrangementRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BucketConverter {
    public static BucketArrangementCommand toCommand(BucketArrangementRequest bucketArrangementRequest) {
        if (bucketArrangementRequest == null) {
            return null;
        }
        BucketArrangementCommand bucketsArrangementCommand = new BucketArrangementCommand();
        BeanUtils.copyProperties(bucketArrangementRequest, bucketsArrangementCommand);
        return bucketsArrangementCommand;
    }
}
