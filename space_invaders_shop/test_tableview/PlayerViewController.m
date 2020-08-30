//
//  PlayerViewController.m
//  test_tableview
//
//  Created by Joon Kang on 2016-07-26.
//  Copyright © 2016 Joon Kang. All rights reserved.
//

#import "PlayerViewController.h"
#import "Item.h"
#import "SharedConfig.h"

//#import "ViewController.h"


@interface PlayerViewController ()

@end

@implementation PlayerViewController {
    NSArray *itemName;
    NSArray *thumbnails;
    NSArray *itemDesc;

    NSMutableString *myMutableString;
    NSMutableString *mutableImageFileName;
    NSMutableString *mutableTitle;
    NSMutableString *mutableDesc;
    NSMutableString *tabItemIndex;

    NSMutableArray *itemsInfo;
}

- (void)viewDidLoad {
    [super viewDidLoad];

    itemsInfo = [[SharedConfig sharedSetupConfig] itemsInfo];

    NSLog(@"itemsInfo started: [itemsInfo count] = %d", [itemsInfo count]);
    for (int i = 0; i < [itemsInfo count]; i++) {
        Item *itemInfo = itemsInfo[i];
        NSLog(@"Name: %@", itemInfo.name);
    }
    NSLog(@"itemsInfo end");

        
    // Initialize table data
    itemName = [NSArray arrayWithObjects:@"Double Point Item", @"Skin Changing Item Blue", @"Skin Changing Item Green", @"Skin Changing Item Red", nil];
    
    // Initialize thumbnails
    thumbnails = [NSArray arrayWithObjects:@"double_point_item.png", @"skin_changing_item_blue.png", @"skin_changing_item_green.png", @"skin_changing_item_red.png", nil];
    
    // Initialize Preparation Time
    itemDesc = [NSArray arrayWithObjects:@"Gives x2 Extra Points", @"Changes Spaceship Colour to Blue", @"Changes Spaceship Colour to Green", @"Changes Spaceship Colour to Red", nil];
    
    // Find out the path of recipes.plist
    NSString *path = [[NSBundle mainBundle] pathForResource:@"space_invaders" ofType:@"plist"];
    
    // Load the file content and read the data into arrays
    NSDictionary *dict = [[NSDictionary alloc] initWithContentsOfFile:path];
    itemName = [dict objectForKey:@"ItemName"];
    thumbnails = [dict objectForKey:@"Thumbnail"];
    itemDesc = [dict objectForKey:@"ItemDesc"];
    
    myMutableString = [[SharedConfig sharedSetupConfig] myMutableString];
    mutableImageFileName = [[SharedConfig sharedSetupConfig] mutableImageFileName];
    mutableTitle = [[SharedConfig sharedSetupConfig] mutableTitle];
    mutableDesc = [[SharedConfig sharedSetupConfig] mutableDesc];
    tabItemIndex = [[SharedConfig sharedSetupConfig] tabItemIndex];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [itemsInfo count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"SimpleTableCell"];
    
    UILabel *nameLabel = (UILabel *)[cell viewWithTag:100];
    nameLabel.text = [[itemsInfo objectAtIndex:indexPath.row] name];
    
    UILabel *descLabel = (UILabel *)[cell viewWithTag:101];
    descLabel.text = [[itemsInfo objectAtIndex:indexPath.row] description];
    
    UIImageView *thumbnailImageView = (UIImageView *)[cell viewWithTag:102];
    thumbnailImageView.image = [UIImage imageNamed:[[itemsInfo objectAtIndex:indexPath.row] imageFileName]];
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSLog(@"didSelectRowAtIndexPath");
    
//    /*UIAlertView *messageAlert = [[UIAlertView alloc]
//     initWithTitle:@"Row Selected" message:@"You've selected a row" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];*/
//    UIAlertView *messageAlert = [[UIAlertView alloc]
//                                 initWithTitle:@"Row Selected" message:[itemName objectAtIndex:indexPath.row] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
//    // Display the Hello World Message
//    [messageAlert show];

    // Checked the selected row

    NSString *string = @"";
    
    NSMutableString *mutableString = [string mutableCopy];

    [mutableImageFileName setString:[thumbnails objectAtIndex:indexPath.row]];
    [mutableTitle setString:[itemName objectAtIndex:indexPath.row]];
    [mutableDesc setString:[itemName objectAtIndex:indexPath.row]];
    [myMutableString setString:@"PlayerView"];
    
    [tabItemIndex setString:@"1"];
    
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    UIViewController *vc = [sb instantiateViewControllerWithIdentifier:@"MainTabBar"];
    vc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
    [self presentViewController:vc animated:YES completion:NULL];
}

- (NSIndexPath *)tableView:(UITableView *)tableView willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSLog(@"willSelectRowAtIndexPath");
//    if (indexPath.row == 0) {
//        return nil;
//    }

    return indexPath;
}

@end
