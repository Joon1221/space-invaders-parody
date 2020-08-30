//
//  ViewController.m
//  test_tableview
//
//  Created by Joon Kang on 2016-07-26.
//  Copyright © 2016 Joon Kang. All rights reserved.
//

#import "ViewController.h"
#import "Item.h"
#import "SharedConfig.h"
#import "ProtocolRequest.h"
#import "ProtocolResponse.h"

@interface ViewController ()

@end

@implementation ViewController {

NSMutableString *myMutableString;
NSMutableString *spaceshipID;
NSMutableString *tabItemIndex;

NSMutableArray *itemsInfo;
NSMutableArray *itemsShopInfo;

}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    
    NSLog(@"Main ViewController");
    
//    NSLog(myMutableString);
    myMutableString = [[SharedConfig sharedSetupConfig] myMutableString];
    tabItemIndex = [[SharedConfig sharedSetupConfig] tabItemIndex];

    itemsInfo = [[SharedConfig sharedSetupConfig] itemsInfo];
    itemsShopInfo = [[SharedConfig sharedSetupConfig] itemsInfo];

    loginSuccess = false;
    spaceshipID = @"";
    timer = nil;
    
    // 처리할 nested json: {"a1"="a1v \" ", "b1"="{\"a2\"=\"a2v \\\" \", \"b2\"=\"{\\\"a3\\\"=\\\"a3v\\\", \\\"b3\\\"=\\\"b3v\\\"}\"}"}
    //                   {"a1"="a1v \" ", "b1"="{\"a2\"=\"a2v \\\" \", \"b2\"=\"{\\\"a3\\\"=\\\"a3v\\\", \\\"b3\\\"=\\\"b3v\\\"}\"}"}
//    NSString *nestedJson = @"{\"a1\":\"a1v \\\" \", \"b1\":\"{\\\"a2\\\":\\\"a2v \\\\\\\" \\\", \\\"b2\\\":\\\"{\\\\\\\"a3\\\\\\\":\\\\\\\"a3v\\\\\\\", \\\\\\\"b3\\\\\\\":\\\\\\\"b3v\\\\\\\"}\\\"}\"}";
////    NSMutableDictionary *jobs = [NSMutableDictionary dictionary];
//    NSLog(@"Main ViewController: NSMutableDictionary *keyAndValues = [self jsonToHashMap:nestedJson];");
//    NSMutableDictionary *keyAndValues1 = [self jsonToHashMap:nestedJson];
//    NSMutableDictionary *keyAndValues2 = [self jsonToHashMap:[keyAndValues1 valueForKey:@"b1"]];
//    NSMutableDictionary *keyAndValues3 = [self jsonToHashMap:[keyAndValues2 valueForKey:@"b2"]];
//    
    spaceshipID = [[SharedConfig sharedSetupConfig] spaceshipID];

    NSLog(@"Main ViewController: end");
}

- (NSMutableDictionary *)jsonToHashMap:(NSString *)jsonString {
    NSMutableDictionary *keyAndValues = [NSMutableDictionary dictionary];
    NSLog(@"Main ViewController::jsonToHashMap(): jsonString = [%@]", jsonString);

    //--------------------------------------------------------------------------
    // 앨거리듬 [key와 value를 nested json일수도 있는 json string으로부터 떼어내기]
    //--------------------------------------------------------------------------
//    [jobs setObject:@"Mary" forKey:@"Audi TT"];
    NSLog(@"Main ViewController::jsonToHashMap(): step #1");
    // Step #1: 양쪽의 { }를 떼어내기. 이때 whitespace조심할 것.
    NSString *trimmedJsonString = [jsonString stringByTrimmingCharactersInSet:
                              [NSCharacterSet whitespaceCharacterSet]];
    NSString *jsonStringBody = [trimmedJsonString substringWithRange:NSMakeRange(1, [trimmedJsonString length] - 2)];
    
//    NSString *searchString = @"Yadda";
//    NSRange searchCharRange;
//    
//    searchCharRange = NSMakeRange(3, [string length]);
//    thisCharRange = [string rangeOfString:searchString options:0 range:searchCharRange];
//    
//    NSRange range = [result rangeOfString:@"\"info\""];
//    range.location
//
//    NSString *trimmedPart1 = [part1 stringByTrimmingCharactersInSet:
//                              [NSCharacterSet whitespaceCharacterSet]];
//    NSString *part1Substring = [trimmedPart1 substringWithRange:NSMakeRange(1, [trimmedPart1 length] - 2)];
//
//    if ([val characterAtIndex:0] == '\"') {
    
    NSLog(@"Main ViewController::jsonToHashMap(): step #2");
    // Step #2: key의 시작 더블 쿼테이션을 찾기
//    NSRange keyStartDblQuot = [jsonStringBody rangeOfString:@"\""];
    NSRange keySearchRange =  NSMakeRange(0, [jsonStringBody length]);
    while (true) {
        NSLog(@"keySearchRange.location: %lu", keySearchRange.location);
        NSRange keyStartDblQuot = [jsonStringBody rangeOfString:@"\"" options:0 range:keySearchRange];

        if (keyStartDblQuot.location == NSNotFound) {
            // no more element!
            break;
        }
        
        NSLog(@"Main ViewController::jsonToHashMap(): step #3");
        // Step #3: key의 닫는 더블 쿼테이션을 찾기
        //     - 이때 더블 쿼테이션을 찾은 후, 만약 그 바로 전에 \가 있으면 무시한다.
        NSRange keyEndDblQuot;
        NSUInteger curStartIndex = keyStartDblQuot.location + 1;
        do {
            NSRange searchRange =  NSMakeRange(curStartIndex, [jsonStringBody length] - curStartIndex);
            keyEndDblQuot = [jsonStringBody rangeOfString:@"\"" options:0 range:searchRange];
            if ([jsonStringBody characterAtIndex:keyEndDblQuot.location-1] != '\\') {
                break;
            }
            curStartIndex = keyEndDblQuot.location + 1;
        } while (true);
        NSString *key = [jsonStringBody substringWithRange:NSMakeRange(keyStartDblQuot.location + 1, (keyEndDblQuot.location - 1) - (keyStartDblQuot.location))];
        
        NSLog(@"key = [%@]", key);
        
        NSLog(@"Main ViewController::jsonToHashMap(): step #4");
        // Step #4: value의 콜론을 찾기
        NSRange valSearchRange =  NSMakeRange(keyEndDblQuot.location + 1, [jsonStringBody length] - (keyEndDblQuot.location + 1));
        NSRange valStartColon = [jsonStringBody rangeOfString:@":" options:0 range:valSearchRange];
        
        // Step #5: 콜론 다음에 나오는 value가 "로 시작하는지 아니면 숫자인지 알기 위해, 콜론 이후의
        //          non-whitespace 캐릭터를 찾아내어, 그것이 "인지 아닌지 확인한 다음
        //          만약 "이 아니라면 ,나 }나 whitespace가 나올때까지 찾아서 거기까지가 value이고
        //          동시에 숫자이다.
        //          이때 이 숫자가 int일수도 있고 double일수도 있다. 따라서 점(point)을 찾아서
        //          있으면 double로 바꾸고, 없으면 int로 바꾼다.
        NSLog(@"Main ViewController::jsonToHashMap(): step #5");
        NSLog(@"valStartColon: %lu", valStartColon.location);

        NSString *val = @"";
        int indexValStart = -1;
        int indexValEnd = -1;
        for (int i = valStartColon.location + 1; i < [jsonStringBody length]; i++) {
            char curChar = [jsonStringBody characterAtIndex:i];
            // curChar가 non-whitespace
            if (curChar != ' ' &&
                curChar != '\r' &&
                curChar != '\n' &&
                curChar != '\t') {
                //--------------------------------------------------------------
                // curChar가 "이면.. -> String
                //--------------------------------------------------------------
                if (curChar == '\"') {
                    NSLog(@"Main ViewController::jsonToHashMap(): step #6a");
                    //----------------------------------------------------------
                    // Step #6a: value의 닫는 더블 쿼테이션을 찾기
                    //     - 이때 더블 쿼테이션을 찾은 후, 만약 그 바로 전에 \가 있으면 무시한다.
                    //----------------------------------------------------------
                    int indexValStartDblQuot = i;
                    
                    NSRange valEndDblQuot;
                    curStartIndex = indexValStartDblQuot + 1;
                    do {
                        NSRange searchRange =  NSMakeRange(curStartIndex, [jsonStringBody length] - curStartIndex);
                        valEndDblQuot = [jsonStringBody rangeOfString:@"\"" options:0 range:searchRange];
                        NSLog(@"Main ViewController::jsonToHashMap(): jsonStringBody = [%@]", jsonStringBody);
                        NSLog(@"Main ViewController::jsonToHashMap(): valEndDblQuot.location-1 = %lu", valEndDblQuot.location-1);
                        if ([jsonStringBody characterAtIndex:valEndDblQuot.location-1] != '\\') {
                            NSLog(@"Main ViewController::jsonToHashMap(): break");
                            break;
                        }
                        curStartIndex = valEndDblQuot.location + 1;
                    } while (true);
                    val = [jsonStringBody substringWithRange:NSMakeRange(indexValStartDblQuot + 1, (valEndDblQuot.location - 1) - indexValStartDblQuot)];
                    indexValEnd = valEndDblQuot.location;
                    break;
                }
                //--------------------------------------------------------------
                // curChar가 "가 아니면.. -> 숫자(int나 double) 
                //--------------------------------------------------------------
                else {
                    //----------------------------------------------------------
                    // Step #6a: ,나 }나 whitespace가 나올때까지 찾아서 거기까지가 value이고
                    //          동시에 숫자이다.
                    //----------------------------------------------------------
                    indexValStart = i;
                    
                    indexValEnd = -1;
                    for (int j = indexValStart + 1; j < [jsonStringBody length] && indexValEnd == -1; j++) {
                        char curChar = [jsonStringBody characterAtIndex:j];
                        // curChar가 non-whitespace
                        if (curChar == ',' ||
                            curChar == '}' ||
                            curChar == ' ' ||
                            curChar == '\r' ||
                            curChar == '\n' ||
                            curChar == '\t') {
                            indexValEnd = j;
                        }
                    }
                    val = [jsonStringBody substringWithRange:NSMakeRange(indexValStart, indexValEnd - indexValStart)];
                    break;
                }
            }
        }
        
        NSLog(@"val = [%@]", val);
    //01234567890123456789012345678901234567890123456789012345678901234567890123456789
    //"a1"="a1v \" ", "b1"="{\"a2\"=\"a2v \\\" \", \"b2\"=\"{\\\"a3\\\"=\\\"a3v\\\", \\\"b3\\\"=\\\"b3v\\\"}\"}"
        val = [self removeEscapeSequenceBackSlash:val];
        NSLog(@"val after removing backslash = [%@]", val);
        
        [keyAndValues setObject:val forKey:key];
        
        keySearchRange =  NSMakeRange(indexValEnd + 1, [jsonStringBody length] - (indexValEnd + 1));
    }
    return keyAndValues;
}

- (NSString *)removeEscapeSequenceBackSlash:(NSString *)str {
    NSString *resultStr = @"";
    
    for (int i = 0; i < [str length]; i++) {
        if ([str characterAtIndex:i] == '\\') {
            i++;
            resultStr = [NSString stringWithFormat:@"%@%c", resultStr, [str characterAtIndex:i]];
        }
        else {
            resultStr = [NSString stringWithFormat:@"%@%c", resultStr, [str characterAtIndex:i]];
        }
    }
    
    return resultStr;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)handleProgrammaticallyContentsViewerButton:(id)sender {
    NSLog(@"handleProgrammaticallyContentsViewerButton");

    // first try
//    UIViewController *_viewController=[self.storyboard instantiateViewControllerWithIdentifier:@"ContentsViewController"];
//    [self.view addSubview:_viewController.view];
    
    // second try
    [myMutableString appendString:@"MainView"];
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    UIViewController *vc = [sb instantiateViewControllerWithIdentifier:@"ContentsView"];
    vc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
    [self presentViewController:vc animated:YES completion:NULL];
}

- (IBAction)handleLoginButton:(id)sender {
    if (self.userIdTextField.text != nil && self.pwdTextField.text != nil && 
        ![self.userIdTextField.text isEqual: @""] && ![self.pwdTextField.text isEqual: @""]) {
        //---------------------------------------------------------
        //---------------------------------------------------------
        //---------------------------------------------------------
        // convert the ProtocolResponse object to the http request
        //---------------------------------------------------------
        //---------------------------------------------------------
        //---------------------------------------------------------
        ProtocolRequest *protocolRequest = [[ProtocolRequest alloc] init];
        [protocolRequest setOperation:@"login"];
        [protocolRequest setUserId:self.userIdTextField.text];
        [protocolRequest setPwd:self.pwdTextField.text];
        [protocolRequest setMessage:@""];
        [protocolRequest setPlatform:@"ios"];
        [protocolRequest print];
        
        NSString *jsonString = [protocolRequest toString];
        
        //NSString *jsonString = @"{\"operation\":\"login\",\"id\":\"qwe123\",\"pwd\":\"qwe123\",\"message\":\"\"}";
        //NSString *jsonString = [NSString stringWithFormat:@"{\"operation\":\"login\",\"id\":\"%@\",\"pwd\":\"%@\",\"message\":\"\"}", self.userIdTextField.text, self.pwdTextField.text];
    
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
                 NSLog(@"handleLoginButton(): error: %@", [error localizedDescription]);
             }
             else 
             {
                 NSString *result = [[NSString alloc] initWithData:data encoding:NSASCIIStringEncoding];
                 // json등과 유사한 프로토콜에서 "="을 http로 전송할 경우, \u003d로 바뀌므로, 다시 "="로 replace해줘야 한다.
                 // 하지만 json에서는 "="대신에 ":"을 사용한다. 
//                 result = [result stringByReplacingOccurrencesOfString:@"\\u003d"
//                                                          withString:@"="];
                 NSLog(@"handleLoginButton(): result=[%@]", result);
                 
                 NSLog(@"handleLoginButton(): hellooooooooooooooooooooo1");

                 NSMutableDictionary *resultDictionary = [self jsonToHashMap:result];
                 NSLog(@"handleLoginButton(): hellooooooooooooooooooooo2");
                 NSLog([resultDictionary objectForKey:@"id"]);
                 [spaceshipID setString:[resultDictionary objectForKey:@"id"]];

                 //=============================================================
                 // 다시 접속해서 inventory의 item list를 받아온다.
                 //=============================================================
//                 NSString *jsonString = @"{\"operation\":\"inventory_update\",\"id\":\"aaa\",\"pwd\":\"\",\"message\":\"\"}";
//                 NSString *jsonString = [NSString stringWithFormat:@"{\"operation\":\"shop_update_items\",\"id\":\"%@\",\"pwd\":\"\",\"message\":\"\",\"platform\":\"ios\"}", spaceshipID];
                 NSString *jsonString = [NSString stringWithFormat:@"{\"operation\":\"shop_update_items\",\"id\":\"%@\",\"pwd\":\"\",\"message\":\"\",\"platform\":\"ios\"}", spaceshipID];

                 NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:[NSURL URLWithString:@"http://localhost:8080/space_invaders_server/SpaceInvadersMerchantServer"]];
                 
                 [request setHTTPMethod:@"POST"];
                 [request setValue:@"application/json" forHTTPHeaderField:@"Content-type"];
                 
                 [request setValue:[NSString stringWithFormat:@"%d", [jsonString length]] forHTTPHeaderField:@"Content-length"];
                 
                 [request setHTTPBody:[jsonString dataUsingEncoding:NSUTF8StringEncoding]];
                 NSLog(@"handleLoginButton(): jsonString=[%@]", jsonString);

                 NSURLConnection *conn = [[NSURLConnection alloc] initWithRequest:request delegate:self];
                 
                 NSURLResponse * response = nil;
                 NSError * error = nil;
                 NSData * data = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];
                 
                 if (error == nil)
                 {
                     // Parse data here
                     NSString* myString;
                     myString = [[NSString alloc] initWithData:data encoding:NSASCIIStringEncoding];
                     //        logInSuccess = true;
                     NSLog(@"myString: |||%@|||", myString);
                     
//                     NSString *testJsonString = @"{\"operation\":\"update_merchant\",\"id\":\"aaa\",\"result\":\"true\",\"info\":{\"items\":[{\"name\":\"potion\",\"className\":\"Potion\",\"price\":10,\"imageFileName\":\"item_potion_hp.png\",\"description\":\"increases HP\",\"uuid\":\"\"},{\"name\":\"potion_speed\",\"className\":\"SpeedPotion\",\"price\":15,\"imageFileName\":\"item_potion_speed.png\",\"description\":\"increases speed\",\"uuid\":\"\"},{\"name\":\"gun\",\"className\":\"Gun\",\"price\":30,\"imageFileName\":\"item_gun.png\",\"description\":\"lets the character use gun\",\"uuid\":\"\"},{\"name\":\"potion_fly\",\"className\":\"FlyPotion\",\"price\":30,\"imageFileName\":\"item_potion_fly.png\",\"description\":\"lets the character float\",\"uuid\":\"\"},{\"name\":\"shield\",\"className\":\"Shield\",\"price\":20,\"imageFileName\":\"item_shield.png\",\"description\":\"protects the character\",\"uuid\":\"\"},{\"name\":\"empty\",\"className\":\"EmptyItem\",\"price\":0,\"uuid\":\"\"}]}}";

//                     NSString *testJsonString = @"{\"operation\":\"inventory_update\",\"id\":\"3\",\"result\":\"true\",\"info\":{\"numItems\":2,\"items\":[{\"name\":\"Skin Changing Item Green\",\"className\":\"SkinChangingItemGreen\",\"price\":1,\"imageFileName\":\"skin_changing_item_green.png\",\"description\":\"Skin Changing Item Green\",\"uuid\":\"a1\"},{\"name\":\"Double Point Item\",\"className\":\"DoublePointItem\",\"price\":5,\"imageFileName\":\"double_point_item.png\",\"description\":\"Double Point Item\",\"uuid\":\"a2\"}]}}";
//                     NSString *testJsonString = @"{\"operation\":\"inventory_update\",\"id\":\"aaa\",\"result\":\"true\",\"info\":{\"numItems\":2,\"items\":[{\"name\":\"Skin Changing Item Green\",\"className\":\"SkinChangingItemGreen\",\"price\":1,\"imageFileName\":\"skin_changing_item_green.png\",\"description\":\"Skin Changing Item Green\",\"uuid\":\"a1\"},{\"name\":\"Double Point Item\",\"className\":\"DoublePointItem\",\"price\":5,\"imageFileName\":\"double_point_item.png\",\"description\":\"Double Point Item\",\"uuid\":\"a2\"}]}}";
//                     NSData* testData = [testJsonString dataUsingEncoding:NSUTF8StringEncoding];
                     
//                     NSMutableDictionary *resultDictionaryOfInventoryUpdate = [self jsonToHashMap:myString];
                     NSDictionary *resultDictionaryOfInventoryUpdate = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&error];
//                     NSDictionary *resultDictionaryOfInventoryUpdate = [NSJSONSerialization JSONObjectWithData:testData options:kNilOptions error:&error];
                     NSLog(@"resultDictionaryOfInventoryUpdate: %@", resultDictionaryOfInventoryUpdate);

                     NSLog(@"handleLoginButton(): hellooooooooooooooooooooo2a");
                 
                     //=============================================================
                     //=============================================================
                     //============================================================
//                     NSLog(@"info: %@", [resultDictionaryOfInventoryUpdate valueForKey:@"info"]);
                     NSString *infoString = [resultDictionaryOfInventoryUpdate valueForKey:@"info"];
                     NSLog(@"infoString: %@", infoString);
//                     NSMutableDictionary *infoDictionary = [self jsonToHashMap:[resultDictionaryOfInventoryUpdate valueForKey:@"info"]];
                     NSLog(@"resultDictionaryOfInventoryUpdate: %@", resultDictionaryOfInventoryUpdate);

//                     NSString *testInfo = @"{\"numItems\":2,\"items\":[{\\\"name\\\":\\\"Skin Changing Item Green\\\",\\\"className\\\":\\\"SkinChangingItemGreen\\\",\\\"price\\\":1,\\\"imageFileName\\\":\\\"skin_changing_item_green.png\\\",\\\"description\\\":\\\"Skin Changing Item Green\\\",\\\"uuid\\\":\\\"a1\\\"},{\\\"name\\\":\\\"Double Point Item\\\",\\\"className\\\":\\\"DoublePointItem\\\",\\\"price\\\":5,\\\"imageFileName\\\":\\\"double_point_item.png\\\",\\\"description\\\":\\\"Double Point Item\\\",\\\"uuid\\\":\\\"a2\\\"}]}";
//                     NSLog(@"testInfo: %@", testInfo);
////                     NSData * infoData = [[resultDictionaryOfInventoryUpdate valueForKey:@"info"] dataUsingEncoding:NSUTF8StringEncoding];
//                     NSData * infoData = [testInfo dataUsingEncoding:NSUTF8StringEncoding];
//                     NSDictionary *infoDictionary = [NSJSONSerialization JSONObjectWithData:infoData options:kNilOptions error:&error];
//                     NSLog(@"infoDictionary: %@", infoDictionary);

//                     NSLog(@"handleLoginButton(): hellooooooooooooooooooooo3");
                     NSDictionary *itemsInfoDictionary = [resultDictionaryOfInventoryUpdate valueForKey:@"info"];
                     
                     NSArray *itemsArray = [itemsInfoDictionary valueForKey:@"items"];
                     NSLog(@"items: %@", [itemsInfoDictionary valueForKey:@"items"]);
                     NSLog(@"[itemsArray count]: %d", [itemsArray count]);
                     


                     for (int i = 0; i < [itemsArray count]; i++) {
                         NSLog(@"handleLoginButton(): hellooooooooooooooooooooo4: i = %d", i);

                         //---------------------------------------------------------
                         //"desc"="Skin Changing Item: Blue", "imgFileName"="skin_changing_item_blue.png"
                         //---------------------------------------------------------
                         NSMutableDictionary *itemsInfoDictionary = itemsArray[i];
//                         NSData* itemData = [itemsArray[i] dataUsingEncoding:NSUTF8StringEncoding];
//                         NSDictionary *itemsInfoDictionary = [NSJSONSerialization JSONObjectWithData:itemData options:kNilOptions error:&error];
                         NSLog(@"handleLoginButton(): hellooooooooooooooooooooo5");

                         Item *curItem = [Item new];
//                         
                         for(id key in itemsInfoDictionary) {
                             id value = [itemsInfoDictionary objectForKey:key];
                             NSLog(@"handleLoginButton(): Key   : [%@]", key);
                             NSLog(@"handleLoginButton(): Value : [%@]", value);
                             if ([key isEqualToString:@"name"]) {
                                 curItem.name = [itemsInfoDictionary objectForKey:key];
                             }
                             else if ([key isEqualToString:@"className"]) {
                                 curItem.className = [itemsInfoDictionary objectForKey:key];
                             }
                             else if ([key isEqualToString:@"price"]) {
//                                 curItem.price = [[itemsInfoDictionary objectForKey:key] intValue];
                                 curItem.price = [itemsInfoDictionary objectForKey:key];
                             }
                             else if ([key isEqualToString:@"imageFileName"]) {
                                 curItem.imageFileName = [itemsInfoDictionary objectForKey:key];
                             }
                             else if ([key isEqualToString:@"description"]) {
                                 curItem.description = [itemsInfoDictionary objectForKey:key];
                             }
                             else if ([key isEqualToString:@"uuid"]) {
                                 curItem.uuid = [itemsInfoDictionary objectForKey:key];
                             }
                         }
                         
                         [itemsInfo addObject:curItem];
                     }
                     
//                     NSLog(@"ViewController::itemsInfo started: [itemsInfo count] = %d", [itemsInfo count]);
//                     for (int i = 0; i < [itemsInfo count]; i++) {
//                         Item *itemInfo = itemsInfo[i];
//                         NSLog(@"Name: %@", itemInfo.name);
//                     }
//                     NSLog(@"ViewController::itemsInfo end");
                     
                     loginSuccess = true;
                 }
                 else {
                     NSLog(@"handleLoginButton(): error: inventory_update failed!!!");
                 }
                 
                 
                 /*
                 result = [[NSString alloc] initWithData:data encoding:NSASCIIStringEncoding];
                 // json등과 유사한 프로토콜에서 "="을 http로 전송할 경우, \u003d로 바뀌므로, 다시 "="로 replace해줘야 한다.
                 // 하지만 json에서는 "="대신에 ":"을 사용한다.
                 //                 result = [result stringByReplacingOccurrencesOfString:@"\\u003d"
                 //                                                          withString:@"="];
                 NSLog(@"handleLoginButton(): result=[%@]", result);
                 
                 NSLog(@"handleLoginButton(): hellooooooooooooooooooooo1");
                 
                 resultDictionary = [self jsonToHashMap:result];
                 NSLog(@"handleLoginButton(): hellooooooooooooooooooooo2");
                 NSLog([resultDictionary objectForKey:@"id"]);
                 [spaceshipID setString:[resultDictionary objectForKey:@"id"]];
                 
                 //=============================================================
                 // 다시 접속해서 inventory의 item list를 받아온다.
                 //=============================================================
                 //                 NSString *jsonString = @"{\"operation\":\"inventory_update\",\"id\":\"aaa\",\"pwd\":\"\",\"message\":\"\"}";
                 //                 NSString *jsonString = [NSString stringWithFormat:@"{\"operation\":\"shop_update_items\",\"id\":\"%@\",\"pwd\":\"\",\"message\":\"\",\"platform\":\"ios\"}", spaceshipID];
                 jsonString = [NSString stringWithFormat:@"{\"operation\":\"shop_update_items\",\"id\":\"%@\",\"pwd\":\"\",\"message\":\"\",\"platform\":\"ios\"}", spaceshipID];
                 
                 request = [[NSMutableURLRequest alloc] initWithURL:[NSURL URLWithString:@"http://localhost:8080/space_invaders_server/SpaceInvadersMerchantServer"]];
                 
                 [request setHTTPMethod:@"POST"];
                 [request setValue:@"application/json" forHTTPHeaderField:@"Content-type"];
                 
                 [request setValue:[NSString stringWithFormat:@"%d", [jsonString length]] forHTTPHeaderField:@"Content-length"];
                 
                 [request setHTTPBody:[jsonString dataUsingEncoding:NSUTF8StringEncoding]];
                 NSLog(@"handleLoginButton(): jsonString=[%@]", jsonString);
                 
                 conn = [[NSURLConnection alloc] initWithRequest:request delegate:self];
                 
                 response = nil;
                 error = nil;
                 data = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];
                 
                 if (error == nil)
                 {
                     // Parse data here
                     NSString* myString;
                     myString = [[NSString alloc] initWithData:data encoding:NSASCIIStringEncoding];
                     //        logInSuccess = true;
                     NSLog(@"myString: |||%@|||", myString);
                     
                     //                     NSString *testJsonString = @"{\"operation\":\"update_merchant\",\"id\":\"aaa\",\"result\":\"true\",\"info\":{\"items\":[{\"name\":\"potion\",\"className\":\"Potion\",\"price\":10,\"imageFileName\":\"item_potion_hp.png\",\"description\":\"increases HP\",\"uuid\":\"\"},{\"name\":\"potion_speed\",\"className\":\"SpeedPotion\",\"price\":15,\"imageFileName\":\"item_potion_speed.png\",\"description\":\"increases speed\",\"uuid\":\"\"},{\"name\":\"gun\",\"className\":\"Gun\",\"price\":30,\"imageFileName\":\"item_gun.png\",\"description\":\"lets the character use gun\",\"uuid\":\"\"},{\"name\":\"potion_fly\",\"className\":\"FlyPotion\",\"price\":30,\"imageFileName\":\"item_potion_fly.png\",\"description\":\"lets the character float\",\"uuid\":\"\"},{\"name\":\"shield\",\"className\":\"Shield\",\"price\":20,\"imageFileName\":\"item_shield.png\",\"description\":\"protects the character\",\"uuid\":\"\"},{\"name\":\"empty\",\"className\":\"EmptyItem\",\"price\":0,\"uuid\":\"\"}]}}";
                     
                     //                     NSString *testJsonString = @"{\"operation\":\"inventory_update\",\"id\":\"3\",\"result\":\"true\",\"info\":{\"numItems\":2,\"items\":[{\"name\":\"Skin Changing Item Green\",\"className\":\"SkinChangingItemGreen\",\"price\":1,\"imageFileName\":\"skin_changing_item_green.png\",\"description\":\"Skin Changing Item Green\",\"uuid\":\"a1\"},{\"name\":\"Double Point Item\",\"className\":\"DoublePointItem\",\"price\":5,\"imageFileName\":\"double_point_item.png\",\"description\":\"Double Point Item\",\"uuid\":\"a2\"}]}}";
                     //                     NSString *testJsonString = @"{\"operation\":\"inventory_update\",\"id\":\"aaa\",\"result\":\"true\",\"info\":{\"numItems\":2,\"items\":[{\"name\":\"Skin Changing Item Green\",\"className\":\"SkinChangingItemGreen\",\"price\":1,\"imageFileName\":\"skin_changing_item_green.png\",\"description\":\"Skin Changing Item Green\",\"uuid\":\"a1\"},{\"name\":\"Double Point Item\",\"className\":\"DoublePointItem\",\"price\":5,\"imageFileName\":\"double_point_item.png\",\"description\":\"Double Point Item\",\"uuid\":\"a2\"}]}}";
                     //                     NSData* testData = [testJsonString dataUsingEncoding:NSUTF8StringEncoding];
                     
                     //                     NSMutableDictionary *resultDictionaryOfInventoryUpdate = [self jsonToHashMap:myString];
                     NSDictionary *resultDictionaryOfInventoryUpdate = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&error];
                     //                     NSDictionary *resultDictionaryOfInventoryUpdate = [NSJSONSerialization JSONObjectWithData:testData options:kNilOptions error:&error];
                     NSLog(@"resultDictionaryOfInventoryUpdate: %@", resultDictionaryOfInventoryUpdate);
                     
                     NSLog(@"handleLoginButton(): hellooooooooooooooooooooo2a");
                     
                     //=============================================================
                     //=============================================================
                     //============================================================
                     //                     NSLog(@"info: %@", [resultDictionaryOfInventoryUpdate valueForKey:@"info"]);
                     NSString *infoString = [resultDictionaryOfInventoryUpdate valueForKey:@"info"];
                     NSLog(@"infoString: %@", infoString);
                     //                     NSMutableDictionary *infoDictionary = [self jsonToHashMap:[resultDictionaryOfInventoryUpdate valueForKey:@"info"]];
                     NSLog(@"resultDictionaryOfInventoryUpdate: %@", resultDictionaryOfInventoryUpdate);
                     
                     //                     NSString *testInfo = @"{\"numItems\":2,\"items\":[{\\\"name\\\":\\\"Skin Changing Item Green\\\",\\\"className\\\":\\\"SkinChangingItemGreen\\\",\\\"price\\\":1,\\\"imageFileName\\\":\\\"skin_changing_item_green.png\\\",\\\"description\\\":\\\"Skin Changing Item Green\\\",\\\"uuid\\\":\\\"a1\\\"},{\\\"name\\\":\\\"Double Point Item\\\",\\\"className\\\":\\\"DoublePointItem\\\",\\\"price\\\":5,\\\"imageFileName\\\":\\\"double_point_item.png\\\",\\\"description\\\":\\\"Double Point Item\\\",\\\"uuid\\\":\\\"a2\\\"}]}";
                     //                     NSLog(@"testInfo: %@", testInfo);
                     ////                     NSData * infoData = [[resultDictionaryOfInventoryUpdate valueForKey:@"info"] dataUsingEncoding:NSUTF8StringEncoding];
                     //                     NSData * infoData = [testInfo dataUsingEncoding:NSUTF8StringEncoding];
                     //                     NSDictionary *infoDictionary = [NSJSONSerialization JSONObjectWithData:infoData options:kNilOptions error:&error];
                     //                     NSLog(@"infoDictionary: %@", infoDictionary);
                     
                     //                     NSLog(@"handleLoginButton(): hellooooooooooooooooooooo3");
                     NSDictionary *itemsInfoDictionary = [resultDictionaryOfInventoryUpdate valueForKey:@"info"];
                     
                     NSArray *itemsArray = [itemsInfoDictionary valueForKey:@"items"];
                     NSLog(@"items: %@", [itemsInfoDictionary valueForKey:@"items"]);
                     NSLog(@"[itemsArray count]: %d", [itemsArray count]);
                     
                     
                     
                     for (int i = 0; i < [itemsArray count]; i++) {
                         NSLog(@"handleLoginButton(): hellooooooooooooooooooooo4: i = %d", i);
                         
                         //---------------------------------------------------------
                         //"desc"="Skin Changing Item: Blue", "imgFileName"="skin_changing_item_blue.png"
                         //---------------------------------------------------------
                         NSMutableDictionary *itemsInfoDictionary = itemsArray[i];
                         //                         NSData* itemData = [itemsArray[i] dataUsingEncoding:NSUTF8StringEncoding];
                         //                         NSDictionary *itemsInfoDictionary = [NSJSONSerialization JSONObjectWithData:itemData options:kNilOptions error:&error];
                         NSLog(@"handleLoginButton(): hellooooooooooooooooooooo5");
                         
                         Item *curItem = [Item new];
                         //
                         for(id key in itemsInfoDictionary) {
                             id value = [itemsInfoDictionary objectForKey:key];
                             NSLog(@"handleLoginButton(): Key   : [%@]", key);
                             NSLog(@"handleLoginButton(): Value : [%@]", value);
                             if ([key isEqualToString:@"name"]) {
                                 curItem.name = [itemsInfoDictionary objectForKey:key];
                             }
                             else if ([key isEqualToString:@"className"]) {
                                 curItem.className = [itemsInfoDictionary objectForKey:key];
                             }
                             else if ([key isEqualToString:@"price"]) {
                                 //                                 curItem.price = [[itemsInfoDictionary objectForKey:key] intValue];
                                 curItem.price = [itemsInfoDictionary objectForKey:key];
                             }
                             else if ([key isEqualToString:@"imageFileName"]) {
                                 curItem.imageFileName = [itemsInfoDictionary objectForKey:key];
                             }
                             else if ([key isEqualToString:@"description"]) {
                                 curItem.description = [itemsInfoDictionary objectForKey:key];
                             }
                             else if ([key isEqualToString:@"uuid"]) {
                                 curItem.uuid = [itemsInfoDictionary objectForKey:key];
                             }
                         }
                         
                         [itemsShopInfo addObject:curItem];
                     }
                     
                     //                     NSLog(@"ViewController::itemsInfo started: [itemsInfo count] = %d", [itemsInfo count]);
                     //                     for (int i = 0; i < [itemsInfo count]; i++) {
                     //                         Item *itemInfo = itemsInfo[i];
                     //                         NSLog(@"Name: %@", itemInfo.name);
                     //                     }
                     //                     NSLog(@"ViewController::itemsInfo end");
                     
                     loginSuccess = true;
                 }
                 else {
                     NSLog(@"handleLoginButton(): error: inventory_update failed!!!");
                 }
                 */
             } 
         }];
        if (timer == nil) {
            timer = [NSTimer scheduledTimerWithTimeInterval: 0.1 target:self selector:@selector(onTimer) userInfo:nil repeats:(YES)];
        }
    }
    
    NSLog(@"HI2");
}

- (void) onTimer {
    if (loginSuccess) {
        NSLog(@"onTimer");
        
        // login에서 맨 처음 진입하는 tabbar의 view index지정할 것. 꼭 0이 아닐수도 있으므로..
        [tabItemIndex setString:@"0"];

        UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
        UIViewController *vc = [sb instantiateViewControllerWithIdentifier:@"MainTabBar"];
        vc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
        [self presentViewController:vc animated:YES completion:NULL];
        
        if (timer != nil) {
            [timer invalidate];
            timer = nil;
        }
    }
}

@end
