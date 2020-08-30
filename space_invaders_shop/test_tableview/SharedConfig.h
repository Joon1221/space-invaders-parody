#import <Foundation/Foundation.h>

@interface SharedConfig : NSObject {
//    NSString *_myString;
    NSMutableString *spaceshipID;

    NSMutableString *_myMutableString;
    NSMutableString *_mutableImageFileName;
    NSMutableString *_mutableTitle;
    NSMutableString *_mutableDesc;
    
    NSMutableString *_tabItemIndex;
    
    NSMutableArray *_itemsInfo;
    NSMutableArray *_itemsShopInfo;
}

//@property (retain) NSString *myString;
@property (retain) NSMutableString *spaceshipID;
@property (retain) NSMutableString *myMutableString;
@property (retain) NSMutableString *mutableImageFileName;
@property (retain) NSMutableString *mutableTitle;
@property (retain) NSMutableString *mutableDesc;

@property (retain) NSMutableString *tabItemIndex;

@property (retain) NSMutableArray *itemsInfo;
@property (retain) NSMutableArray *itemsShopInfo;

+ (id)sharedSetupConfig;

@end
