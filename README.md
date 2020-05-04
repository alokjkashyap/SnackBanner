# SnackBanner
A material component to show small alert text-only banner.
It is completely based on Snackbar from Material Design.
### Previews
![SuccessImage](https://user-images.githubusercontent.com/21008628/80981084-15ee9180-8e47-11ea-8dd0-96b1c72f5e3a.jpg)

![ErrorImage](https://user-images.githubusercontent.com/21008628/80981164-374f7d80-8e47-11ea-8a2c-9af196b42f23.jpg)


### Things you can customise

+ Text (obviously)
+ Background color
+ Animation Duration
+ more cooming soon...

### Description

Well, It was made during one of my projects.
I first saw this element on a instagram page of a developer but as it was paid element
so, I thought to make it and after 2-3 weeks of researching I made it.

It is completely based on snackbar and has some methods from [TSnackBar](https://github.com/AndreiD/TSnackBar)

More customisation are comming soon :)

### TODO
+ Custom Typeface
+ you can suggest


## Implementation

#### Step 1: Add the JitPack repository to your build file



Add it in your root build.gradle at the end of repositories:

    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  
 Add the dependencies
 
     dependencies {
	        implementation 'com.github.alokjkashyap:SnackBanner:<version>'
            implementation 'com.google.android.material:material:1.1.0'
	}
  
  
#### Step 2: Code 

    SnackBanner.make(View,text,Duration).show();
    
Other methods


      .setBackgroundColor(R.color.successGreen)
      .setText(text)
      .setDuration(Duration)
      
      
That's all you have to do :)
