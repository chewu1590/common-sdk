# common-sdk
project includes common third library,such as imagePicker etc

#To get a Git project into your build:
 在 build.gradle 中添加依赖

	dependencies {
	        //x.y.z -> 1.0.8
	        //core contain:updater,takePhoto
	        implementation 'cn.woochen:sdk-core:x.y.z'
	        //option:share
	        implementation 'cn.woochen:sdk-share:x.y.z'
	        annotationProcessor 'cn.woochen:sdk-compiler:x.y.z'
	        //option:scan
	        implementation 'cn.woochen:sdk-scan:x.y.z'
	}

#THANKS
[TakePhoto](https://github.com/crazycodeboy/TakePhoto)
