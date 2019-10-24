# common-sdk
project includes common third library,such as imagePicker etc

#To get a Git project into your build:
 在 build.gradle 中添加依赖

	dependencies {
         //a.b.c -> 1.1.0
         //core contain:updater,takePhoto
         implementation 'cn.woochen:sdk-core:a.b.c'
        
         // androidx version (over 1.1.0 can support)
         implementation 'cn.woochen:sdk-core:a.b.c-x'
	}

#THANKS
[TakePhoto](https://github.com/crazycodeboy/TakePhoto)
