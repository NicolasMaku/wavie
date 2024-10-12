# Bienvenu dans le framework Wavie

Les etapes à suivres sont:

Sprint0:
- Creez votre fichier web.xml
- Declarer servlet: FrontController(classe: **mg.itu.prom16.FrontController**) avec l'url : "/" dans web.xml

Sprint1:
- initialiser parametre package des controller dans web.xml:
    - nom: package-controller
    - nom du servlet sur lequel l'associer: FrontController
- Annotez vos controller avec l'annotation @controller

Sprint2:
- Declarez le nom de votre projet en tant que paramettre dans le fichier web.xml
  - nom: project-name
  - nom du servlet sur lequel l'associer: FrontController
- Ajouter une value dans l'annotation @Get de vos methode de controller qui permettrons de les acceder par url

Sprint3(sujet):
- lister dans un hashMap les methods de controller
- Montrer la methode dans un url

Sprint4(sujet):
- invoquer la methode (String, ModelView)

Sprint5(sujet):
- Gestion des erreurs

Sprint6(sujet):
-formulaire vers controller et printer dans page les variables demandee

Sprint7:

Controle:
- Ajouter une exception lorsqu'il n'y a aucune annotation sur l'argument de la methode

Sprin8:
- Creer un moyen d'acceder à la session sans y toucher directement dans les methodes annoté controller
- Creer la classe customSession qui sera notre intelocuteur entre l'HttpSession et notre methode
- Un nouvel argument possible CustomSession session

Sprint10:
- Si meme url mais classe different alors erreur

Sprint11:
- gestion d'exception ajout de status code

Sprint 12:
- Upload de fichier?
Un fichier attaché à une requete