//
//  PlayerNavController.m
//  test_tableview
//
//  Created by SAMIL CHAI on 2016. 10. 4..
//  Copyright © 2016년 Joon Kang. All rights reserved.
//

#import "PlayerTabBarController.h"
#import "SharedConfig.h"
#import "Item.h"

@interface PlayerTabBarController ()

@end

@implementation PlayerTabBarController {
    NSMutableString *tabItemIndex;
    
    NSMutableArray *itemsShopInfo;
    NSMutableArray *itemsInfo;

}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    tabItemIndex = [[SharedConfig sharedSetupConfig] tabItemIndex];

    self.selectedIndex = [tabItemIndex intValue];
    
    // 아래 처럼 특정 item을 숨길 수 있다.
    UITabBarItem *itemToHide = [self.tabBar.items objectAtIndex:1];
    //[itemToHide setEnabled:NO];
    
    itemsShopInfo = [[SharedConfig sharedSetupConfig] itemsInfo];
    
    NSLog(@"itemsInfo started: [itemsInfo count] = %d", [itemsShopInfo count]);
    for (int i = 0; i < [itemsShopInfo count]; i++) {
        Item *itemShopInfo = itemsShopInfo[i];
        NSLog(@"Name: %@", itemShopInfo.name);
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"SimpleTableCell"];
    
    UILabel *nameLabel = (UILabel *)[cell viewWithTag:200];
    nameLabel.text = [[itemsShopInfo objectAtIndex:indexPath.row] name];
    
    UILabel *descLabel = (UILabel *)[cell viewWithTag:201];
    descLabel.text = [[itemsShopInfo objectAtIndex:indexPath.row] description];
    
    UIImageView *thumbnailImageView = (UIImageView *)[cell viewWithTag:202];
    thumbnailImageView.image = [UIImage imageNamed:[[itemsShopInfo objectAtIndex:indexPath.row] imageFileName]];
    
    return cell;
}

@end
