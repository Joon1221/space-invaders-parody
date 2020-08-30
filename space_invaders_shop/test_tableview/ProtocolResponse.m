#import "ProtocolResponse.h"

@implementation ProtocolResponse

- (void)print {
    NSString *output = [NSString stringWithFormat:@"%@%@%@%@%@%@%@%@%@", @"{\"operation\":\"", self->operation, @"\",\"id\":\"", self->userId, @"\",\"result\":\"", self->result, @"\",\"info\":\"", self->info, @"\"}"];
    NSLog(output);
}

- (NSString *)toString {
    NSString *output = [NSString stringWithFormat:@"%@%@%@%@%@%@%@%@%@", @"{\"operation\":\"", self->operation, @"\",\"id\":\"", self->userId, @"\",\"result\":\"", self->result, @"\",\"info\":\"", self->info, @"\"}"];
    return output;
}

- (NSString *)getOperation  {
    return self->operation;
}

- (void)setOperation:(NSString *)operation {
    self->operation = operation;   
}

- (NSString *)getUserId {
    return self->userId;
}

- (void)setUserId:(NSString *)userID {
    self->userId = userId;
}

- (NSString *)getResult {
    return self->result;
}

- (void)setResult:(NSString *)result {
    self->result = result;   
}

- (NSString *)getInfo {
    return self->info;
}

- (void)setInfo:(NSString *)info {
    self->info = info;
}

- (void)setKeyAndValue:(NSString *)key val:(NSString *)val {
    if ([key isEqual:@"operation"]) {
        operation = val;
    }
    else if ([key isEqual:@"userId"]) {
        userId = val;
    }
    else if ([key isEqual:@"result"]) {
        result = val;
    }
    else if ([key isEqual:@"info"]) {
        info = val;
    }
}

@end
