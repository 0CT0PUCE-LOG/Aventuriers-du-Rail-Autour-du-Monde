package fr.umontpellier.iut.rails;

import fr.umontpellier.iut.rails.data.*;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

import javax.print.DocFlavor.STRING;

public class Joueur {
    public enum CouleurJouer {
        JAUNE, ROUGE, BLEU, VERT, ROSE;
    }

    /**
     * Jeu auquel le joueur est rattaché
     */
    private final Jeu jeu;
    /**
     * Nom du joueur
     */
    private final String nom;
    /**
     * CouleurJouer du joueur (pour représentation sur le plateau)
     */
    private final CouleurJouer couleur;
    /**
     * Liste des villes sur lesquelles le joueur a construit un port
     */
    private final List<Ville> ports;
    /**
     * Liste des routes capturées par le joueur
     */
    private final List<Route> routes;
    /**
     * Nombre de pions wagons que le joueur peut encore poser sur le plateau
     */
    private int nbPionsWagon;
    /**
     * Nombre de pions wagons que le joueur a dans sa réserve (dans la boîte)
     */
    private int nbPionsWagonEnReserve;
    /**
     * Nombre de pions bateaux que le joueur peut encore poser sur le plateau
     */
    private int nbPionsBateau;
    /**
     * Nombre de pions bateaux que le joueur a dans sa réserve (dans la boîte)
     */
    private int nbPionsBateauEnReserve;
    /**
     * Liste des destinations à réaliser pendant la partie
     */
    private final List<Destination> destinations;
    /**
     * Liste des cartes que le joueur a en main
     */
    private final List<CarteTransport> cartesTransport;
    /**
     * Liste temporaire de cartes transport que le joueur est en train de jouer pour
     * payer la capture d'une route ou la construction d'un port
     */
    private final List<CarteTransport> cartesTransportPosees;
    /**
     * Score courant du joueur (somme des valeurs des routes capturées, et points
     * perdus lors des échanges de pions)
     */
    private int score;

    public Joueur(String nom, Jeu jeu, CouleurJouer couleur) {
        this.nom = nom;
        this.jeu = jeu;
        this.couleur = couleur;
        this.ports = new ArrayList<>();
        this.routes = new ArrayList<>();
        this.nbPionsWagon = 0;
        this.nbPionsWagonEnReserve = 25;
        this.nbPionsBateau = 0;
        this.nbPionsBateauEnReserve = 50;
        this.cartesTransport = new ArrayList<>();
        this.cartesTransportPosees = new ArrayList<>();
        this.destinations = new ArrayList<>();
        this.score = 0;
    }

    public String getNom() {
        return nom;
    }

    public void setUp(){
        for(int i=0; i<3;i++){
            cartesTransport.add(jeu.piocherCarteWagon());
        }
        for(int i=0; i<7;i++){
            cartesTransport.add(jeu.piocherCarteBateau());
        }

        piocherCarteDestination(5, 3);
        
        repartitionPions();
        
    }

    private void repartitionPions(){
        List<String> nombreWagonOption = Arrays.asList("10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25");
        int nbPionsWagonChoisi;
        int nbPionsBateauChoisi;
        String choix;

        choix = choisir(
                "Entrez le nombre de pion wagon à conserver parmis les " + nbPionsWagonEnReserve + " en réserve (la somme des wagon et des bateau devras être égale à 60)",
                nombreWagonOption,
                null,
                false);
        nbPionsWagonChoisi = Integer.valueOf(choix);

        nbPionsBateauChoisi = 60 - nbPionsWagonChoisi;

        nbPionsBateauEnReserve -= nbPionsBateauChoisi;
        nbPionsBateau += nbPionsBateauChoisi;

        nbPionsWagonEnReserve -= nbPionsWagonChoisi;
        nbPionsWagon += nbPionsWagonChoisi;
    }



    /**
     * Cette méthode est appelée à tour de rôle pour chacun des joueurs de la partie.
     * Elle doit réaliser un tour de jeu, pendant lequel le joueur a le choix entre 5 actions possibles :
     *  - piocher des cartes transport (visibles ou dans la pioche)
     *  - échanger des pions wagons ou bateau
     *  - prendre de nouvelles destinations
     *  - capturer une route
     *  - construire un port
     */
    void jouerTour() {
        String choix;
        boolean aJoue = false;
        ArrayList<String> carteVisibleNom = new ArrayList<String>();
        ArrayList<String> routeNom = new ArrayList<String>();

        List<Ville> portLibre = jeu.getPortsLibres();
        ArrayList<String> portLibreNom = new ArrayList<String>();




        List<Bouton> boutons = Arrays.asList(
                new Bouton("Échanger wagon","PIONS WAGON"),
                new Bouton("Échanger bateau","PIONS BATEAU"),
                new Bouton("Construire un port"));


        List<String> options = new ArrayList<String>();
        options.add("DESTINATION");
        options.add("PIONS WAGON");
        options.add("PIONS BATEAU");
        if(!jeu.piocheWagonEstVide()){
            options.add("WAGON");
        }
        if(!jeu.piocheBateauEstVide()){
            options.add("BATEAU");
        }
        options.add("PORT");
        
        for(CarteTransport c : jeu.getCartesTransportVisibles()){
            options.add(c.getNom());
            carteVisibleNom.add(c.getNom());
        }
        for(Route r : jeu.getRoutesLibres()){
            options.add(r.getNom());
            routeNom.add(r.getNom());
        }

        for(Ville v : portLibre){
            options.add(v.nom());
            portLibreNom.add(v.nom());
        }


        do{
            choix = choisir(
                    "Choisissez l'action à exécuter durant votre tour :",
                    options,
                    boutons,
                    false);

            if (choix.equals("")) {
                log(String.format("%s passe son tour", toLog()));
            }
            else {
                log(String.format("%s a choisi %s", toLog(), choix));

                if(choix.equals("BATEAU") || choix.equals("WAGON") || carteVisibleNom.contains(choix) ){
                    aJoue = this.piocherCarteTransport(choix);
                }
                if(choix.equals("PIONS WAGON") || choix.equals("PIONS BATEAU")) {
                    echangerPion(choix);
                    aJoue = true;
                }
                if(choix.equals("DESTINATION")){
                    piocherCarteDestination(4,1);
                    aJoue = true;
                }
                if(routeNom.contains(choix)) {
                    //TODO
                    log(String.format("%b",peutPrendreRoute(jeu.getRouteFromNom(choix))));
                    if(peutPrendreRoute(jeu.getRouteFromNom(choix))){
                        prendreRoute(choix);
                        aJoue = true;
                    }
                    
                }
                if(choix.equals("PORT")){//Condition d'entrée pas bonne il faut mettre un truc du genre portNom.contains(choix)
                    capturerPort(choix);
                    aJoue = true;
                }
            }
        }while(!aJoue);

        //NE PAS SUPPRIMER
        // IMPORTANT : Le corps de cette fonction est à réécrire entièrement
        // Un exemple très simple est donné pour illustrer l'utilisation de certaines méthodes
        /*
        List<String> optionsVilles = new ArrayList<>();
        for (Ville ville : jeu.getPortsLibres()) {
            optionsVilles.add(ville.nom());
        }
        List<Bouton> boutons = Arrays.asList(
                new Bouton("Montpellier"),
                new Bouton("Sète"));

        String choix = choisir(
                "Choisissez votre ville préférée",
                optionsVilles,
                boutons,
                true);

        if (choix.equals("")) {
            log(String.format("%s n'aime aucune ville", toLog()));
        } else {
            log(String.format("%s a choisi %s", toLog(), choix));
        }
        */
    }

    /**
     * Gère la pioche de carte Transport par le joueur
     * 
     * @param nbCartePioche nombre de carte transport déja pioché
     * 
     * @return la carte qui a été piochée (ou null si aucune carte disponible)
     */
    private boolean piocherCarteTransport(String mode){
        //TODO cette métode ne prend pas en charge les jo
        int nbCartePioche = 0;
        String choix;
        do{
            if(nbCartePioche>0){
                List<String> optionPioche = new ArrayList<String>();
                if(!jeu.piocheWagonEstVide()){
                    optionPioche.add("WAGON");
                }
                if(!jeu.piocheBateauEstVide()){
                    optionPioche.add("BATEAU");
                }
                for(int i=0; i<jeu.getCartesTransportVisibles().size();i++){
                    optionPioche.add(jeu.getCartesTransportVisibles().get(i).getNom());
                }
                choix = choisir("Choisissez quel carte piocher", optionPioche, null,true);
            }
            else{
                choix = mode;
            }
            
            
            if(nbCartePioche<2 && !choix.equals("")){
                log(String.format("%s %s", toLog(), choix));
                if(choix.equals("WAGON")){
                    this.cartesTransport.add(this.jeu.piocherCarteWagon());
                    nbCartePioche++;
                }
                else if(choix.equals("BATEAU")){
                    this.cartesTransport.add(this.jeu.piocherCarteBateau());
                    nbCartePioche++;
                }
                else{
                    CarteTransport carte = jeu.getCarteTransportVisiblesFromNom(choix);
                    if(carte.getType()==TypeCarteTransport.JOKER){
                        if(nbCartePioche == 0){
                            this.cartesTransport.add(carte);
                            nbCartePioche +=2;
                            jeu.removeCarteTransportVisibles(carte);
                            remplacerCarteTransportVisible();

                        }
                    }
                    else{
                        if(nbCartePioche<2){
                            this.cartesTransport.add(carte);
                            nbCartePioche++;
                            jeu.removeCarteTransportVisibles(carte);
                            remplacerCarteTransportVisible();
                        }
                    }

                    if(!jeu.cartesTransportVisiblesSontValide()){
                        jeu.clearCartesTransportVisibles();
                        for(int i=0; i<6;i++){
                            remplacerCarteTransportVisible();
                        }
                    }

                }
                
            }

        }while(!choix.equals("") && nbCartePioche!=2);

        boolean aPioche = !(nbCartePioche==0);
        return aPioche;
    }

    private void remplacerCarteTransportVisible(){
        if(!jeu.piocheBateauEstVide() || !jeu.piocheWagonEstVide()){

            String choix = choisir("Choisissez dans quel deck piocher pour remplacer la carte visible",Arrays.asList("WAGON","BATEAU") , null, false);
            if(choix.equals("WAGON")){
                jeu.addCartesTransportVisibles(this.jeu.piocherCarteWagon());
            }
            else if(choix.equals("BATEAU")){
                jeu.addCartesTransportVisibles(this.jeu.piocherCarteBateau());
            }
        }
    }

    /**
     * Gère la pioche de destination par le joueurs
     */
    private void piocherCarteDestination(int nbCartePioche, int nbCarteMin){
        String choix;
        List<Destination> pioche = new ArrayList<Destination>();
        int nbCarteGarde = nbCartePioche;
        boolean peutPasser = true;

        for(int i=0; i<nbCartePioche; i++){
            pioche.add(this.jeu.piocheDestination());
        }

        List<Bouton> boutons = new ArrayList<Bouton>();

        for(int i=0; i<nbCartePioche; i++){
            if(pioche.get(i) != null){
                boutons.add(new Bouton(pioche.get(i).toString(), pioche.get(i).getNom()));
            }
        }

        do{
            choix = choisir("Choisissez la/les destination(s) que vous ne souhaitez pas conserver", null, boutons, peutPasser);
            
            if(!choix.equals("")){
                
                if(nbCarteGarde>nbCarteMin){

                    log(String.format("%s remet une carte au bas de la pile", toLog()));

                    for(int i=0; i<pioche.size(); i++){
                        if(choix.equals(pioche.get(i).getNom())){
                            boutons.remove(i);
                            jeu.replacerDestination(pioche.get(i));
                            pioche.remove(i);
                            nbCarteGarde--;
                        }
                    }
                }
                
            }
            else{
                log(String.format("%s ajoute les cartes destination à son jeu", toLog()));
            }

        }while(!choix.equals("") && nbCarteGarde!=nbCarteMin);

        for(int i=0; i<pioche.size(); i++){
            this.destinations.add(pioche.get(i));
        }

    }

    /**
     * Gère la prise d'une route par un joueur
     */
    private void prendreRoute(String nomChoisie){
        Route routeChoisie = jeu.getRouteFromNom(nomChoisie);
        String choix;
        int compteur=0;
        ArrayList<String> options = new ArrayList<String>();
        CarteTransport carteChoisie;

        //double le nombre de matériaux nécessaire si la route est double
        int nbMateriauNecessaire = routeChoisie.getLongueur();
        if(routeChoisie.getClass().getName() == "fr.umontpellier.iut.rails.RoutePaire"){
            nbMateriauNecessaire = nbMateriauNecessaire*2;
        }


        poserCarteTransportCompatible(routeChoisie);
        do{
            options.clear();
            for(CarteTransport c : cartesTransportPosees){
                options.add(c.getNom());
            }

            choix = choisir("Choisissez les cartes que vous souhaitez utiliser", options, null, false);
            carteChoisie = getCarteTransportPoseFromNom(choix);
            
            compteur+= getMateriaux(carteChoisie);
            cartesTransportPosees.remove(carteChoisie);
            

            //retirer toutes les cartes non valide pour un choix
            for(CarteTransport c : cartesTransportPosees){
                if(carteChoisie.getType()!= TypeCarteTransport.JOKER){
                    if(carteChoisie.getCouleur() != c.getCouleur() && c.getType()!=TypeCarteTransport.JOKER){
                        cartesTransport.add(c);
                    }
                }
            }
            cartesTransportPosees.removeAll(cartesTransport);

            //defausser la carte choisie
            if(carteChoisie.getType() == TypeCarteTransport.WAGON || carteChoisie.getType() == TypeCarteTransport.JOKER){
                jeu.defausserWagon(carteChoisie);
            }
            else if(carteChoisie.getType() == TypeCarteTransport.BATEAU){
                jeu.defausserBateau(carteChoisie);
            }
            

        }while(nbMateriauNecessaire>compteur);

        //retrait des pions
        if(routeChoisie.getClass().getName() == "fr.umontpellier.iut.rails.RouteTerrestre" || routeChoisie.getClass().getName() == "fr.umontpellier.iut.rails.RoutePaire"){
            nbPionsBateau = nbPionsBateau - nbMateriauNecessaire;
        }
        else if(routeChoisie.getClass().getName() == "fr.umontpellier.iut.rails.RouteMaritime"){
            nbPionsWagon = nbPionsWagon - nbMateriauNecessaire;
        }

        //remise des carte restantes dans Cartes Transport
        cartesTransport.addAll(cartesTransportPosees);
        cartesTransportPosees.removeAll(cartesTransport);

        //attribution de la route
        routes.add(routeChoisie);
        jeu.removeRoutesLibre(routeChoisie);

        //attribution du score
        score+= routeChoisie.getScore();

    }

    private CarteTransport getCarteTransportPoseFromNom(String nom){
        CarteTransport result = null;
        for(CarteTransport c : cartesTransportPosees){
            if(c.getNom().equals(nom)){
                result = c;
            }
        }
        return result;
    }

    private int getMateriaux(CarteTransport c){
        if(c.estDouble()){
            return 2;
        }
        else{
            return 1;
        }
    }

    /**
     * @param route une route voulant être prise par la joueur
     * @return true si le joueur peut prendre la route
     */
    private boolean peutPrendreRoute(Route route){
        boolean estPossible = true;

        if(route.getClass().getName() == "fr.umontpellier.iut.rails.RoutePaire"){
            if(route.getLongueur()> nbPionsWagon){
                estPossible = false;
            }
            else if((route.getLongueur()*2) > nbCombinaisonCarteTransportMax(TypeCarteTransport.WAGON)){
                estPossible = false;
            }
        }

        else if(route.getClass().getName() == "fr.umontpellier.iut.rails.RouteTerrestre"){
            if(route.getLongueur()> nbPionsWagon){
                estPossible = false;
            }
            else if(route.getCouleur() == Couleur.GRIS && route.getLongueur() > nbCombinaisonCarteTransportMax(TypeCarteTransport.WAGON)){
                estPossible = false;
            }
            else if(route.getLongueur() > nombreCarteTransportDeCouleur(TypeCarteTransport.WAGON, route.getCouleur())){
                estPossible = false;
            }
        }

        else if(route.getClass().getName() == "fr.umontpellier.iut.rails.RouteMaritime"){
            if(route.getLongueur()> nbPionsBateau){
                estPossible = false;
            }
            else if(route.getCouleur() == Couleur.GRIS && route.getLongueur() > nbCombinaisonCarteTransportMax(TypeCarteTransport.BATEAU)){
                estPossible = false;
            }
            else if(route.getLongueur() > nombreCarteTransportDeCouleur(TypeCarteTransport.BATEAU, route.getCouleur())){
                estPossible = false;
            }
        }
        return estPossible;
    }

    private int nbCombinaisonCarteTransportMax(TypeCarteTransport type){
        List<Integer> listeCombinaison = Arrays.asList(nombreCarteTransportDeCouleur(type, Couleur.BLANC),nombreCarteTransportDeCouleur(type, Couleur.JAUNE),nombreCarteTransportDeCouleur(type, Couleur.NOIR), nombreCarteTransportDeCouleur(type, Couleur.ROUGE), nombreCarteTransportDeCouleur(type, Couleur.VERT),nombreCarteTransportDeCouleur(type, Couleur.VIOLET));
        System.out.println(Collections.max(listeCombinaison));
        return Collections.max(listeCombinaison);
    }

    private int nombreCarteTransport(TypeCarteTransport type){
        int compteur = 0;
        for(int i = 0; i<cartesTransport.size();i++){
            if(cartesTransport.get(i).getType() == type || cartesTransport.get(i).getType() == TypeCarteTransport.JOKER){
                compteur++;
                if(cartesTransport.get(i).estDouble()){
                    compteur++;
                }
            }
        }
        return compteur;
    }


    private ArrayList<Couleur> combinaisonCouleurCarteTransport(TypeCarteTransport type, int min){
        List<Couleur> listeCouleur = Arrays.asList(Couleur.BLANC,Couleur.JAUNE,Couleur.NOIR,Couleur.ROUGE,Couleur.VERT, Couleur.VIOLET);
        List<Integer> listeCombinaison = Arrays.asList(nombreCarteTransportDeCouleur(type, Couleur.BLANC),nombreCarteTransportDeCouleur(type, Couleur.JAUNE),nombreCarteTransportDeCouleur(type, Couleur.NOIR), nombreCarteTransportDeCouleur(type, Couleur.ROUGE), nombreCarteTransportDeCouleur(type, Couleur.VERT),nombreCarteTransportDeCouleur(type, Couleur.VIOLET));
        ArrayList<Couleur> couleursValides = new ArrayList<Couleur>();
        for(int i=0; i<listeCouleur.size(); i++){
            if(listeCombinaison.get(i)>=min){
                couleursValides.add(listeCouleur.get(i));
            }
        }
        return couleursValides;
    }

    private void poserCarteTransportCompatible(Route route){
        ArrayList<Couleur> couleurValide;

        if(route.getClass().getName() == "fr.umontpellier.iut.rails.RouteTerrestre" || route.getClass().getName() == "fr.umontpellier.iut.rails.RoutePaire"){
            if(route.getCouleur() == Couleur.GRIS){
                couleurValide = combinaisonCouleurCarteTransport(TypeCarteTransport.WAGON, route.getLongueur());
                for(CarteTransport c : cartesTransport){
                    if(c.getType() == TypeCarteTransport.WAGON && couleurValide.contains(c.getCouleur())){
                        cartesTransportPosees.add(c);
                    }
                    else if(c.getType()==TypeCarteTransport.JOKER){
                        cartesTransportPosees.add(c);
                    }
                }
                cartesTransport.removeAll(cartesTransportPosees);
            }
            else{
                for(CarteTransport c : cartesTransport){
                    if(c.getType() == TypeCarteTransport.WAGON && c.getCouleur() == route.getCouleur()){
                        cartesTransportPosees.add(c);
                    }
                    else if(c.getType()==TypeCarteTransport.JOKER){
                        cartesTransportPosees.add(c);
                    }
                }
                cartesTransport.removeAll(cartesTransportPosees);
            }
            
        }

        else if(route.getClass().getName() == "fr.umontpellier.iut.rails.RouteMaritime"){
            if(route.getCouleur() == Couleur.GRIS){
                couleurValide = combinaisonCouleurCarteTransport(TypeCarteTransport.BATEAU, route.getLongueur());
                for(CarteTransport c : cartesTransport){
                    if(c.getType() == TypeCarteTransport.BATEAU && couleurValide.contains(c.getCouleur())){
                        cartesTransportPosees.add(c);
                    }
                    else if(c.getType() == TypeCarteTransport.JOKER){
                        cartesTransportPosees.add(c);
                    }
                }
                cartesTransport.removeAll(cartesTransportPosees);
            }
            else{
                for(CarteTransport c : cartesTransport){
                    if(c.getType() == TypeCarteTransport.BATEAU && c.getCouleur() == route.getCouleur()){
                        cartesTransportPosees.add(c);
                    }
                    else if(c.getType() == TypeCarteTransport.JOKER){
                        cartesTransportPosees.add(c);
                    }
                }
                cartesTransport.removeAll(cartesTransportPosees);
            }
        }
    }


    private int nombreCarteTransportDeCouleur(TypeCarteTransport type ,Couleur couleur){
        int compteur = 0;
        for(int i = 0; i<cartesTransport.size();i++){
            if(((cartesTransport.get(i).getCouleur() == couleur && cartesTransport.get(i).getType() == type)) || cartesTransport.get(i).getType() == TypeCarteTransport.JOKER){
                compteur++;
                if(cartesTransport.get(i).estDouble()){
                    compteur++;
                }
            }
        }
        return compteur;
    }

    private void echangerPion(String mode){
        String choix;
        if(mode.equals("PIONS WAGON")){
            int nbWagon = 0;
            log(String.format("ECHANGE PIONS WAGON",toLog()));
            List<String> nombreWagonOption = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25");
            choix = choisir(
                    "Entrez dans la zone de texte la quantité à échanger. Attention vous perdrez 1 point de score pour chaque pion échangé.",
                    nombreWagonOption,
                    null,
                    false);
            while(nbWagon<1 || nbWagon>25){
                nbWagon = Integer.valueOf(choix);
            }
            if(nbWagon <= this.nbPionsBateau){
                if(nbWagon <= this.nbPionsWagonEnReserve){
                    this.nbPionsWagon+=nbWagon;
                    this.nbPionsBateau-=nbWagon;
                    this.nbPionsWagonEnReserve-=nbWagon;
                    this.nbPionsBateauEnReserve+=nbWagon;
                    this.score-=nbWagon;
                    log(String.format("Vous venez d'échanger "+nbWagon+" pions Bateau\n contre "+nbWagon+" pions Wagon.",toLog()));
                }
                else{
                    log(String.format("L'échange est impossible, pas assez de Pions Wagon\n en réserve.",toLog()));
                }
            }
            else{
                log(String.format("L'échange est impossible, pas assez de Pions Bateau\n à échanger.",toLog()));
            }
        }
        if(mode.equals("PIONS BATEAU")){
            int nbBateau = 0;
            List<String> nombreBateauOption = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25");
            choix = choisir(
                    "Entrez dans la zone de texte la quantité à échanger. Attention vous perdrez 1 point de score pour chaque pion échangé.",
                    nombreBateauOption,
                    null,
                    false);
            nbBateau = Integer.valueOf(choix);
            if(nbBateau <= this.nbPionsWagon){
                if(nbBateau <= this.nbPionsBateauEnReserve){
                    this.nbPionsWagon-=nbBateau;
                    this.nbPionsBateau+=nbBateau;
                    this.nbPionsWagonEnReserve+=nbBateau;
                    this.nbPionsBateauEnReserve-=nbBateau;
                    this.score-=nbBateau;
                    log(String.format("Vous venez d'échanger "+nbBateau+" pions Bateau\n contre "+nbBateau+" pions Wagon.",toLog()));
                }
                else{
                    log(String.format("L'échange est impossible, pas assez de Pions Bateau\n en réserve.",toLog()));
                }
            }
            else{
                log(String.format("L'échange est impossible, pas assez de Pions Bateau\n à échanger.",toLog()));
            }
        }
    }


    private void capturerPort(String port){
        //capturer le port qui est donné en paramètre et qui est dans la liste des ports, si il est déjà dans la liste c'est qu'il est capturable
        List<Ville> portLibre = jeu.getPortsLibres();
        if(routeRelieAPort(port) != null){
            if(routeRelieAPort(port).getLongueur() <= nombreCarteTransport(TypeCarteTransport.BATEAU)){
                this.ports.add(portLibre.get(ports.indexOf(port)));
                log(String.format("Vous venez de capturer le port de "+port+".",toLog()));
            }
            else{
                log(String.format("Vous n'avez pas assez de cartes bateau pour capturer le port de "+port+".",toLog()));
            }
        }
        else{
            log(String.format("Vous n'avez pas de route maritime qui relie le port de "+port+".",toLog()));
        }


    }

    private Route routeRelieAPort(String port){
        //vérifie si le joueur possède une route terrestre ou maritime qui est relié au port
        for(Route r : this.routes){
            if(r.getVille1().equals(port) || r.getVille2().equals(port)){
                return r;
            }
        }
        return null;
    }


    /**
     * Attend une entrée de la part du joueur (au clavier ou sur la websocket) et
     * renvoie le choix du joueur.
     *
     * Cette méthode lit les entrées du jeu (`Jeu.lireligne()`) jusqu'à ce
     * qu'un choix valide (un élément de `choix` ou de `boutons` ou
     * éventuellement la chaîne vide si l'utilisateur est autorisé à passer) soit
     * reçu.
     * Lorsqu'un choix valide est obtenu, il est renvoyé par la fonction.
     *
     * Exemple d'utilisation pour demander à un joueur de répondre à une question
     * par "oui" ou "non" :
     *
     * ```
     * List<String> choix = Arrays.asList("Oui", "Non");
     * String input = choisir("Voulez-vous faire ceci ?", choix, null, false);
     * ```
     *
     * Si par contre on voulait proposer les réponses à l'aide de boutons, on
     * pourrait utiliser :
     *
     * ```
     * List<Bouton> boutons = Arrays.asList(new Bouton("Un", "1"), new Bouton("Deux", "2"), new Bouton("Trois", "3"));
     * String input = choisir("Choisissez un nombre.", null, boutons, false);
     * ```
     *
     * @param instruction message à afficher à l'écran pour indiquer au joueur la
     *                    nature du choix qui est attendu
     * @param choix       une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur
     * @param boutons     une collection de `Bouton` représentés par deux String (label,
     *                    valeur) correspondant aux choix valides attendus du joueur
     *                    qui doivent être représentés par des boutons sur
     *                    l'interface graphique (le label est affiché sur le bouton,
     *                    la valeur est ce qui est envoyé au jeu quand le bouton est
     *                    cliqué)
     * @param peutPasser  booléen indiquant si le joueur a le droit de passer sans
     *                    faire de choix. S'il est autorisé à passer, c'est la
     *                    chaîne de caractères vide ("") qui signifie qu'il désire
     *                    passer.
     * @return le choix de l'utilisateur (un élement de `choix`, ou la valeur
     * d'un élément de `boutons` ou la chaîne vide)
     */
    public String choisir(
            String instruction,
            Collection<String> choix,
            Collection<Bouton> boutons,
            boolean peutPasser) {
        if (choix == null)
            choix = new ArrayList<>();
        if (boutons == null)
            boutons = new ArrayList<>();

        HashSet<String> choixDistincts = new HashSet<>(choix);
        choixDistincts.addAll(boutons.stream().map(Bouton::valeur).toList());
        if (peutPasser || choixDistincts.isEmpty()) {
            choixDistincts.add("");
        }

        String entree;
        // Lit l'entrée de l'utilisateur jusqu'à obtenir un choix valide
        while (true) {
            jeu.prompt(instruction, boutons, peutPasser);
            entree = jeu.lireLigne();
            // si une réponse valide est obtenue, elle est renvoyée
            if (choixDistincts.contains(entree)) {
                return entree;
            }
        }
    }

    /**
     * Affiche un message dans le log du jeu (visible sur l'interface graphique)
     *
     * @param message le message à afficher (peut contenir des balises html pour la
     *                mise en forme)
     */
    public void log(String message) {
        jeu.log(message);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("=== %s (%d pts) ===", nom, score));
        joiner.add(String.format("  Wagons: %d  Bateaux: %d", nbPionsWagon, nbPionsBateau));
        return joiner.toString();
    }

    /**
     * @return une chaîne de caractères contenant le nom du joueur, avec des balises
     * HTML pour être mis en forme dans le log
     */
    public String toLog() {
        return String.format("<span class=\"joueur\">%s</span>", nom);
    }

    boolean destinationEstComplete(Destination d) {
        // Cette méthode pour l'instant renvoie false pour que le jeu puisse s'exécuter.
        // À vous de modifier le corps de cette fonction pour qu'elle retourne la valeur attendue.
        //TODO
        return false;
    }

    public int calculerScoreFinal() {
        //TODO
        throw new RuntimeException("Méthode pas encore implémentée !");
    }

    /**
     * Renvoie une représentation du joueur sous la forme d'un dictionnaire de
     * valeurs sérialisables
     * (qui sera converti en JSON pour l'envoyer à l'interface graphique)
     */
    Map<String, Object> dataMap() {
        return Map.ofEntries(
                Map.entry("nom", nom),
                Map.entry("couleur", couleur),
                Map.entry("score", score),
                Map.entry("pionsWagon", nbPionsWagon),
                Map.entry("pionsWagonReserve", nbPionsWagonEnReserve),
                Map.entry("pionsBateau", nbPionsBateau),
                Map.entry("pionsBateauReserve", nbPionsBateauEnReserve),
                Map.entry("destinationsIncompletes",
                        destinations.stream().filter(d -> !destinationEstComplete(d)).toList()),
                Map.entry("destinationsCompletes", destinations.stream().filter(this::destinationEstComplete).toList()),
                Map.entry("main", cartesTransport.stream().sorted().toList()),
                Map.entry("inPlay", cartesTransportPosees.stream().sorted().toList()),
                Map.entry("ports", ports.stream().map(Ville::nom).toList()),
                Map.entry("routes", routes.stream().map(Route::getNom).toList()));
    }
}
