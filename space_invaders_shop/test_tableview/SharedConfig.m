#import "SharedConfig.h"

SharedConfig *g_sharedSetupConfig = nil;

@implementation SharedConfig

//@synthesize myString = _myString;
@synthesize spaceshipID = _spaceshipID;
@synthesize myMutableString = _myMutableString;
@synthesize mutableImageFileName = _mutableImageFileName;
@synthesize mutableTitle = _mutableTitle;
@synthesize mutableDesc = _mutableDesc;
@synthesize tabItemIndex = _tabItemIndex;
@synthesize itemsInfo = _itemsInfo;
@synthesize itemsShopInfo = _itemsShopInfo;

+ (id)sharedSetupConfig {
    if (!g_sharedSetupConfig) {
        g_sharedSetupConfig = [[SharedConfig alloc] init];
    }
    
    return g_sharedSetupConfig;
}

- (id)init
{
    if (self = [super init])
    {
//        _myString = @"SharedConfig::init(): hello";
//        _myMutableString = [NSMutableString stringWithString:@"SharedConfig::init(): hello2"];
        
        _spaceshipID = [[NSMutableString alloc]init];
        _myMutableString = [[NSMutableString alloc]init];
        _mutableImageFileName = [[NSMutableString alloc]init];
        _mutableTitle = [[NSMutableString alloc]init];
        _mutableDesc = [[NSMutableString alloc]init];
        _tabItemIndex = [[NSMutableString alloc]init];
        _itemsInfo = [[NSMutableArray alloc]init];
    }
    return self;
}

@end
