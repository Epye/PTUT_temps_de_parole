# Projet Tuteuré de Licence Professionelle IEM à l'université Lyon1 à Bourg en Bresse
## Sujet : Application de Temps de Parole

Il y a deux applications : une application Android Record_Audio_Test et une application desktop Java Recognito.

L'application Android utilise le volume pour gérer le temps de parole de la personne principale.

L'aplication Recognito nécessite un enregistrement préalable de l'interlocuter "référent" d'environ 1min pour permettre d'initialiser la librairie "Recognito". L'application ensuite enregistrera le son et analysera les voix pour déterminer si l'interlocuteur "référent" parle. Elle met à jour le chronomètre toute les 7 secondes.