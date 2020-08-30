//
//  NSObject_Item.h
//  test_tableview
//
//  Created by SAMIL CHAI on 2016. 10. 25..
//  Copyright © 2016년 Joon Kang. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Item : NSObject

@property (strong, nonatomic) NSString *name;
@property (strong, nonatomic) NSString *className;
@property (strong, nonatomic) NSString *price;
@property (strong, nonatomic) NSString *imageFileName;
@property (strong, nonatomic) NSString *description;
@property (strong, nonatomic) NSString *uuid;

@end
