//
//  ContentsViewController.h
//  test_tableview
//
//  Created by SAMIL CHAI on 2016. 7. 28..
//  Copyright © 2016년 Joon Kang. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ContentsViewController : UIViewController {
    bool buySuccess;
    NSTimer *timer;

    NSString *from;
}

@property (nonatomic, retain, readwrite) NSString *from;

@end
