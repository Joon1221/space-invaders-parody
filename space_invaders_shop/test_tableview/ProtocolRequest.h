#import <Foundation/Foundation.h>

@interface ProtocolRequest : NSObject {
    NSString *operation;
    NSString *userId;
    NSString *pwd;
    NSString *message;
    NSString *platform;
}
    
- (void)print;
- (NSString *)toString;

- (NSString *)getOperation;
- (void)setOperation:(NSString *)operation;

- (NSString *)getUserId;
- (void)setUserId:(NSString *)userId;

- (NSString *)getPwd;
- (void)setPwd:(NSString *)pwd;

- (NSString *)getMessage;
- (void)setMessage:(NSString *)pwd;

- (NSString *)getPlatform;
- (void)setPlatform:(NSString *)pwd;

@end
