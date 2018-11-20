# tgvmax-auto-confirm

Tools to confirm TGV Max travels instead of going manually to the following web page : https://www.tgvmax.fr/trainline/fr-FR/reservation

Works with travels from Trainline and Oui.SNCF

## Usage

```
java -jar java.jar username password
```

The tools only execute one time for now, use a timer to execute it every day for exemple :) (systemd timer/cron on linux, windows task scheduler)

Java Executable can be found here : https://github.com/varsq/tgvmax-auto-confirm/releases

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
```$xslt
java -DrootLevel="DEBUG" java.jar
```

## Build

This project use Maven, executable jar with all the dependencies can be build with ```mvn package```
