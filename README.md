# tgvmax-auto-confirm

Tools to confirm TGV Max travels instead of going manually to the following web page : https://www.tgvmax.fr/trainline/fr-FR/reservation

Works with travels from Trainline and Oui.SNCF !

## Usage

Zip archive can be found here : https://github.com/varsq/tgvmax-auto-confirm/releases

Unzip the archive, your login/password can be put in the ```config.properties``` file located at the root of the archive.

```
login=YOURLOGIN
password=YOURPASSWORD
```
The tool is then launched with java with the jar located at the root of the archive.
```
java -jar tgvmax-auto-confirm-1.1.jar
```

Login/Password can also be defined directly in the command line, in that case the ```config.properties``` file is not used.

```
java -jar tgvmax-auto-confirm-1.1.jar login password
```

The tools only execute one time for now, use a timer to execute it every day for exemple :) (systemd timer/cron on linux, windows task scheduler)

## Notification

Use a [Matrix](https://matrix.org/blog/home/) room to send notification for:

- Error during logging
- Confirmation of bookings

Your matrix token and the room ID needs to be put in the  ```config.properties``` file 

```
login=YOURLOGIN
password=YOURPASSWORD
matrix_token=
matrix_room_id=
```

Only work with  https://matrix.org homeserver for now.

The token of your matrix account can be found in your account settings  if you use [Riot client](https://riot.im/app/) , the room ID can be found on the room settings.

![alt text](notification.png?raw=true "example")

## Log Example

```
INFO  com.github.varsq.Authentification - Starting auth
INFO  com.github.varsq.Authentification - Auth done :)
INFO  com.github.varsq.TgvMaxApi - Check TRAINLINE travels
INFO  com.github.varsq.TgvMaxApi - 4 travels ; 2 travels to confirm 
INFO  com.github.varsq.TgvMaxApi - 4 travels ; 1 travels to confirm 
INFO  com.github.varsq.TgvMaxApi - Travel confirmed : ANGERS ST LAUD -> ST PIERRE DES CORPS on 2018-11-22 05:38 
INFO  com.github.varsq.TgvMaxApi - 4 travels ; 0 travels to confirm 
INFO  com.github.varsq.TgvMaxApi - Travel confirmed : PARIS MONTPARNASSE 1 ET 2 -> ANGERS ST LAUD on 2018-11-22 06:37 
INFO  com.github.varsq.TgvMaxApi - Check OUISNCF travels
INFO  com.github.varsq.TgvMaxApi - 0 travels ; 0 travels to confirm 
```

Log level can be changed with the system variable ```-DrootLevel```
```
java -DrootLevel="DEBUG" java.jar
```

## Build

This project use Maven, zip archive with all the dependencies can be build with ```mvn package```
