#import <Foundation/Foundation.h>

@interface ProtocolResponse : NSObject {
    NSString *operation;
    NSString *userId;
    NSString *result;
    NSString *info;
}

- (void)print;
- (NSString *)toString;

- (NSString *)getOperation;
- (void)setOperation:(NSString *)operation;

- (NSString *)getUserId;
- (void)setUserId:(NSString *)userId;

- (NSString *)getResult;
- (void)setResult:(NSString *)result;

- (NSString *)getInfo;
- (void)setInfo:(NSString *)info;

- (void)setKeyAndValue:(NSString *)key val:(NSString *)val;

@end
