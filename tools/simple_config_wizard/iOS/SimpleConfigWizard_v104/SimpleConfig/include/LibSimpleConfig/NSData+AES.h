//
//  NSData+AES.h
//  Smile
//
//  Created by 周 敏 on 12-11-24.
//  Copyright (c) 2012年 BOX. All rights reserved.
//

#import <Foundation/Foundation.h>

@class NSString;

@interface NSData (Encryption)

- (NSData *)AES256EncryptWithKey:(NSString *)key;   //encrypt internal
- (NSData *)AES256DecryptWithKey:(NSString *)key;   //decrypt internal
- (NSData *)encryptAESData:(NSString*)string key:(NSString *)key; //encrypt API

@end
