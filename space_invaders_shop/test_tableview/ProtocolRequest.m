#import "ProtocolRequest.h"

@implementation ProtocolRequest 

- (id)init {
    NSLog(@"ProtocolRequest::init");
    
    operation = @"";
    userId = @"";
    pwd = @"";
    message = @"";
    platform = @"";
    
    return self;
}

- (void)print {
    NSString *output = [NSString stringWithFormat:@"%@%@%@%@%@%@%@%@%@%@%@", @"{\"operation\":\"", self->operation, @"\",\"id\":\"", self->userId, @"\",\"pwd\":\"", self->pwd, @"\",\"message\":\"", self->message, @"\",\"platform\":\"", self->platform, @"\"}"];
    NSLog(output);
}

- (NSString *)toString {
    NSString *output = [NSString stringWithFormat:@"%@%@%@%@%@%@%@%@%@%@%@", @"{\"operation\":\"", self->operation, @"\",\"id\":\"", self->userId, @"\",\"pwd\":\"", self->pwd, @"\",\"message\":\"", self->message, @"\",\"platform\":\"", self->platform, @"\"}"];
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

- (void)setUserId:(NSString *)userId {
    self->userId = userId;
}

- (NSString *)getPwd {
    return self->pwd;
}

- (void)setPwd:(NSString *)pwd {
    self->pwd = pwd;
}

- (NSString *)getMessage {
    return self->message;
}

- (void)setMessage:(NSString *)message {
    self->message = message;
}

- (NSString *)getPlatform {
    return self->platform;
}

- (void)setPlatform:(NSString *)platform {
    self->platform = platform;
}

@end
