1. First clone the git repository into your choice of folder on your device.
> - use command "[git clone https://git.cs.usask.ca/team-32/Project32.git]" 
![ALT](./images_readme/1.png)
2. Project32 installation process on android studio
> First install Android Studio from Internet
>![ALT](./images_readme/2.png) 
>![ALT](./images_readme/3.png) 
> - Open the journal app folder inside Project32 ![ALT](./images_readme/4.png) 
>![ALT](./images_readme/5.png)

3. How to make an emulator
> - Click on Device Manager on the write side of the window![ALT](./images_readme/6.png)
> - Click on Create Device ![ALT](./images_readme/7.png) ![ALT](./images_readme/8.png) ![ALT](./images_readme/9.png)
>   ![ALT](./images_readme/10.png) ![ALT](./images_readme/11.png)

4. How to get Maps API key and to use it
>   - Go to android manifest file and use the link highlighted(make sure the google account you used to sign in on android studio is same as the one you used to register the api key) ![ALT](./images_readme/12.png)
>   - Now follow the steps on the website ![ALT](./images_readme/13.png)
>  - Click on credentials page ![ALT](./images_readme/14.png) 
> - Agree to terms and conditions![ALT](./images_readme/15.png) 
> - Go to keys and credentials and create a project 
> ![ALT](./images_readme/16.png) 
> - Enter similar information as shown![ALT](./images_readme/17.png) 
> - Again go to keys and credentials![ALT](./images_readme/18.png) 
> - Enter Payment information ![ALT](./images_readme/19.png) 
> ![ALT](./images_readme/20.png) 
> - Enter project information ![ALT](./images_readme/21.png) 
> ![ALT](./images_readme/22.png) 
> ![ALT](./images_readme/22.png) 
> ![ALT](./images_readme/23.png) 
> ![ALT](./images_readme/24.png) 
> ![ALT](./images_readme/25.png)
> - Here you click on go to google maps platform and copy the key![ALT](./images_readme/26.png)
> - Click on keys and credentials to view information related to key ![ALT](./images_readme/27.png)
> - Now go to res>values>strings.xml ![ALT](./images_readme/28.png)
> - Copy paste the Key similar to highlighted format![ALT](./images_readme/29.png)
> - Now go to Android manifest file and look for maps meta data tag![ALT](./images_readme/30.png)
> - Write the name of the key as highlighted![ALT](./images_readme/31.png)
> - Maps appear like this![ALT](./images_readme/32.png)

4. Note whenever the map opens , it will never show correct device location(on the emulator) due to privacy reasons and the device location is by default set to google headquarters. However, correcct device can be seen on an actual android phone. Click on round circle to go to device location and click on location icon to go to saved location.





