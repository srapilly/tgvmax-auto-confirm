# tgvmax-auto-confirm

Tools to confirm TGV Max travels instead of going manually to the following web page : https://www.tgvmax.fr/trainline/fr-FR/reservation

Works with travels from Trainline and Oui.SNCF

## Usage

```
java -jar java.jar username password
```

Java Executable can be found here : https://github.com/varsq/tgvmax-auto-confirm/releases


## Log Example

```
[main] INFO com.github.varsq.TgvMaxApi - 4 travels ; 1 travels to confirm 
[main] INFO com.github.varsq.TgvMaxApi - Travel confirmed : LE MANS -> PARIS MONTPARNASSE 1 ET 2 on 2018-11-20 06:22 
[main] INFO com.github.varsq.TgvMaxApi - 4 travels ; 0 travels to confirm 
[main] INFO com.github.varsq.TgvMaxApi - Travel confirmed : PARIS MONTPARNASSE 1 ET 2 -> ST PIERRE DES CORPS on 2018-11-20 09:12 
```
## Build

This project use Maven, executable jar with all the dependencies can be build with ```mvn package```
