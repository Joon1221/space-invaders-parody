//
//  ContentsViewController.m
//  test_tableview
//
//  Created by SAMIL CHAI on 2016. 7. 28..
//  Copyright © 2016년 Joon Kang. All rights reserved.
//

#import "ContentsViewController.h"
#import "SharedConfig.h"
#import "ProtocolRequest.h"

@implementation ContentsViewController {

NSMutableString *spaceshipID;
NSMutableString *myMutableString;
NSMutableString *mutableImageFileName;
NSMutableString *mutableTitle;
NSMutableString *mutableDesc;
}

@synthesize from;

- (void)viewDidLoad {
    [super viewDidLoad];
    
    NSLog(@"ContentsViewController");
    
    spaceshipID = [[SharedConfig sharedSetupConfig] spaceshipID];
    myMutableString = [[SharedConfig sharedSetupConfig] myMutableString];
    mutableImageFileName = [[SharedConfig sharedSetupConfig] mutableImageFileName];
    mutableTitle = [[SharedConfig sharedSetupConfig] mutableTitle];
    mutableDesc = [[SharedConfig sharedSetupConfig] mutableDesc];
    
    UILabel *nameLabel = (UILabel *)[self.view viewWithTag:103];
    nameLabel.text = mutableTitle;
    
    UILabel *descLabel = (UILabel *)[self.view viewWithTag:104];
    descLabel.text = mutableDesc;
    
    UIImageView *thumbnailImageView = (UIImageView *)[self.view viewWithTag:105];
    thumbnailImageView.image = [UIImage imageNamed:mutableImageFileName];
    
    buySuccess = false;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)handleBuyButton:(id)sender {
    NSLog(@"ContentsViewController::handleBuyButton()");
    
//    NSString *jsonString = @"{\"operation\":\"shop_purchase_item\",\"id\":\"qwe123\",\"pwd\":\"\",\"message\":\"\"}";
    ProtocolRequest *protocolRequest = [[ProtocolRequest alloc] init];
    [protocolRequest setOperation:@"shop_purchase_item"];
    [protocolRequest setUserId:spaceshipID];
    [protocolRequest setPwd:@""];
    [protocolRequest setMessage:mutableTitle];
    [protocolRequest print];
    
    NSString *jsonString = [protocolRequest toString];
    
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc]initWithURL:[NSURL URLWithString:@"http://localhost:8080/space_invaders_server/SpaceInvadersServer"]];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-type"];
    [request setValue:[NSString stringWithFormat:@"%lu", (unsigned long)[jsonString length]] forHTTPHeaderField:@"Content-length"];
    [request setHTTPBody:[jsonString dataUsingEncoding:NSUTF8StringEncoding]];
    NSOperationQueue *queue = [[NSOperationQueue alloc] init];
    
    [NSURLConnection sendAsynchronousRequest:request queue:queue completionHandler:^(NSURLResponse *response, NSData *data, NSError *error)
     {
         if (error)
         {
             NSLog(@"Error,%@", [error localizedDescription]);
         }
         else 
         {
             NSString *result = [[NSString alloc] initWithData:data encoding:NSASCIIStringEncoding];
             NSLog(@"[%@]", result);
             buySuccess = true;
//             if ([result isEqualToString:@"successful"]) {
//                 NSLog(@"Log in successful!");
//                 buySuccess = true;
//             }
//             NSLog(@"(IBAction)login:(id)sender : %@", [[NSString alloc] initWithData:data encoding:NSASCIIStringEncoding]);
         } 
     }];  
    if (timer == nil) {
        timer = [NSTimer scheduledTimerWithTimeInterval: 0.1 target:self selector:@selector(onTimer) userInfo:nil repeats:(YES)];
    }
    
    NSLog(@"HI2");
}

- (void) onTimer {
    if (buySuccess) {
        NSLog(@"onTimer");
        
        UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
        UIViewController *vc = [sb instantiateViewControllerWithIdentifier:myMutableString];
        vc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
        [self presentViewController:vc animated:YES completion:NULL];
        
        if (timer != nil) {
            [timer invalidate];
            timer = nil;
        }
    }
}

- (IBAction)handleHomeButton:(id)sender {
    NSLog(@"handleProgrammaticallyContentsViewerButton");
        
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    UIViewController *vc = [sb instantiateViewControllerWithIdentifier:myMutableString];
    vc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
    [self presentViewController:vc animated:YES completion:NULL];
}

@end
