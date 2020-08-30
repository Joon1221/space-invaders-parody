//
//  ViewController.h
//  test_tableview
//
//  Created by Joon Kang on 2016-07-26.
//  Copyright © 2016 Joon Kang. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController {

    bool loginSuccess;
    
    NSTimer *timer;
}

@property (weak, nonatomic) IBOutlet UITextField *userIdTextField;
@property (weak, nonatomic) IBOutlet UITextField *pwdTextField;

@end

