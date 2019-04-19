# common-sdk
project includes common third library,such as imagePicker etc

#To get a Git project into your build:
Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.chewu1590:common-sdk:1.0.0'
	}



	dependencies {
	        implementation 'cn.woochen:sdk-core:1.0.1'
	        //option:share
	        implementation 'cn.woochen:sdk-share:1.0.1'
	        annotationProcessor 'cn.woochen:sdk-compiler:1.0.1'
	}

#THANKS
[TakePhoto](https://github.com/crazycodeboy/TakePhoto)
