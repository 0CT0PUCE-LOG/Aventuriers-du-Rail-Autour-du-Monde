package fr.umontpellier.iut.rails;

import fr.umontpellier.iut.rails.data.*;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

import javax.print.DocFlavor.STRING;

import com.fasterxml.jackson.databind.AnnotationIntrospector.ReferenceProperty.Type;

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
                true);

        if(!choix.equals("")){
            nbPionsWagonChoisi = Integer.valueOf(choix);
        }
        else{
            nbPionsWagonChoisi = 20;
        }

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

        //le joueur remplace les cartes transport visible au début de son tour si c'est possible
        if(jeu.getCartesTransportVisibles().size() < 6 && (!jeu.piocheBateauEstVide() || !jeu.piocheWagonEstVide())){
            for(int i=0; i<6-jeu.getCartesTransportVisibles().size(); i++){
                remplacerCarteTransportVisible();
            }
        }

        List<Bouton> boutons = new ArrayList<Bouton>();
        if(nbPionsBateau!=0 && nbPionsWagonEnReserve!=0){
            boutons.add(new Bouton("Échanger wagon","PIONS WAGON"));
        }
        if(nbPionsWagon!=0 && nbPionsBateauEnReserve!=0){
            boutons.add(new Bouton("Échanger bateau","PIONS BATEAU"));
        }
            

            


        List<String> options = new ArrayList<String>();
        if(!jeu.piocheDestinationEstVide()){
            options.add("DESTINATION");
        }

        if(!jeu.piocheWagonEstVide()){
            options.add("WAGON");
        }
        if(!jeu.piocheBateauEstVide()){
            options.add("BATEAU");
        }
        
        for(CarteTransport c : jeu.getCartesTransportVisibles()){
            options.add(c.getNom());
            carteVisibleNom.add(c.getNom());
        }
        for(Route r : jeu.getRoutesLibres()){
            options.add(r.getNom());
            routeNom.add(r.getNom());
        }
        if(ports.size()<3){
            for(Ville v : portLibre){
                options.add(v.nom());
                portLibreNom.add(v.getNom());
            }
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
                if(portLibreNom.contains(choix)){
                    if(peutConstruirePort(jeu.getPortFromNom(choix))){
                        construirePort(choix);
                        aJoue = true;
                    } 
                }
            }
        }while(!aJoue);

        //NE PAS SUPPRIMER MODELE
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
     * @param //nbCartePioche nombre de carte transport déja pioché
     * 
     * @return la carte qui a été piochée (ou null si aucune carte disponible)
     */
    private boolean piocherCarteTransport(String mode){
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
                        jeu.resetCartesTransportVisibles();
                    }

                }
                
            }

        }while(!choix.equals("") && nbCartePioche!=2);

        return !(nbCartePioche==0);
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
     * Gère la pioche de destination par le joueur
     */
    private void piocherCarteDestination(int nbCartePioche, int nbCarteMin){
        String choix;
        List<Destination> pioche = new ArrayList<Destination>();
        int nbCarteGarde = nbCartePioche;
        boolean peutPasser = true;

        for(int i=0; i<nbCartePioche; i++){
            if(!jeu.piocheDestinationEstVide()){
                pioche.add(this.jeu.piocheDestination());
            }
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

        for (Destination destination : pioche) {
            this.destinations.add(destination);
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
        int nbJokerDispo = nombreCarteTransport(TypeCarteTransport.JOKER);
        ArrayList<RouteTerrestre> sousRoutes= new ArrayList<>();
        Map<RouteTerrestre, Integer> nbMateriauxParRoute = new HashMap<RouteTerrestre, Integer>();
        boolean BateauNonDoubleJoue = false;

        //double le nombre de matériaux nécessaire si la route est double
        int nbMateriauNecessaire = routeChoisie.getLongueur();
        if(routeChoisie.getClass().getName().equals("fr.umontpellier.iut.rails.RoutePaire")){
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
            if(carteChoisie.getType() == TypeCarteTransport.BATEAU && !carteChoisie.estDouble()){
                BateauNonDoubleJoue = true;
            }
            
            compteur+= getMateriaux(carteChoisie);
            cartesTransportPosees.remove(carteChoisie);

            //retirer toutes les cartes non valide pour un choix
            if(!(routeChoisie instanceof RoutePaire)){
                for(CarteTransport c : cartesTransportPosees){
                    if(carteChoisie.getType()!= TypeCarteTransport.JOKER){
                        if(carteChoisie.getCouleur() != c.getCouleur() && c.getType()!=TypeCarteTransport.JOKER){
                            cartesTransport.add(c);
                        }
                        else if(carteChoisie.getCouleur() == c.getCouleur() && routeChoisie instanceof RouteMaritime){
                            if(contientCarteNonDoubleDeCouleur(cartesTransportPosees, c.getCouleur()) && c.estDouble() && routeChoisie.getLongueur()-compteur==1 && BateauNonDoubleJoue){
                                cartesTransport.add(c);
                            }
                            else if(nbMateriauNecessaire-compteur- nombreCarteTransportDeCouleurNonDouble(TypeCarteTransport.BATEAU, carteChoisie.getCouleur())>0){
                                if((nbMateriauNecessaire-compteur)%2==0 && nombreCarteTransportPoseesDeCouleurDouble(TypeCarteTransport.BATEAU, carteChoisie.getCouleur()) >= (nbMateriauNecessaire-compteur) && !c.estDouble() ){
                                    cartesTransport.add(c);
                                }
                            }

                        }
                        
                    }
                }
                cartesTransportPosees.removeAll(cartesTransport);
            }
            else{ // Si la routeChoisie est Paire
                
            //-----------------------------Attribution de la carte de transport à une sous route-----------------------------
                if(indexOfRouteDeCouleurVide(sousRoutes, nbMateriauxParRoute,carteChoisie.getCouleur()) == -1){
                    if(carteChoisie.getType()!=TypeCarteTransport.JOKER){
                        sousRoutes.add(new RouteTerrestre(null, null, carteChoisie.getCouleur(),2));
                        nbMateriauxParRoute.put(sousRoutes.get(sousRoutes.size()-1), 1);
                        if(nombreCarteTransportPoseesDeCouleurSansJoker(TypeCarteTransport.WAGON, sousRoutes.get(sousRoutes.size()-1).getCouleur()) == 0){
                            nbJokerDispo--;
                        }
                    }
                }
                else{
                    int indiceRouteVide = indexOfRouteDeCouleurVide(sousRoutes, nbMateriauxParRoute,carteChoisie.getCouleur());
                    nbMateriauxParRoute.put(sousRoutes.get(indiceRouteVide), nbMateriauxParRoute.get(sousRoutes.get(indiceRouteVide)) + 1);
                }

            //--------------------------------------------------------------------------------------------------------------------
            //--------------------------------------Retirer les cartes non compatible---------------------------------------------
                ArrayList<Couleur> couleursSousRoutesVides = getCouleurFromSousRoutesVide(sousRoutes,nbMateriauxParRoute);

                for(CarteTransport c : cartesTransportPosees){
                    if(c.getType() != TypeCarteTransport.JOKER){ //un joker est toujours compatible
                        if(sousRoutes.size()==routeChoisie.getLongueur() && !couleursSousRoutesVides.contains(c.getCouleur())){
                            cartesTransport.add(c);
                        }
                        else if(!couleursSousRoutesVides.contains(c.getCouleur()) && (nombreCarteTransportPoseesDeCouleurSansJoker(TypeCarteTransport.WAGON, c.getCouleur()) + nbJokerDispo) < 2 ){
                                cartesTransport.add(c);
                        }
                    }
                }

                cartesTransportPosees.removeAll(cartesTransport);
            }

            

            //defausser la carte choisie
            if(carteChoisie.getType() == TypeCarteTransport.WAGON || carteChoisie.getType() == TypeCarteTransport.JOKER){
                jeu.defausserWagon(carteChoisie);
            }
            else if(carteChoisie.getType() == TypeCarteTransport.BATEAU){
                jeu.defausserBateau(carteChoisie);
            }
            

        }while(nbMateriauNecessaire>compteur);

        //retrait des pions
        if(routeChoisie.getClass().getName().equals("fr.umontpellier.iut.rails.RouteTerrestre") || routeChoisie.getClass().getName().equals("fr.umontpellier.iut.rails.RoutePaire")){
            nbPionsBateau = nbPionsBateau - nbMateriauNecessaire;
        }
        else if(routeChoisie.getClass().getName().equals("fr.umontpellier.iut.rails.RouteMaritime")){
            nbPionsWagon = nbPionsWagon - nbMateriauNecessaire;
        }

        //remise de la carte restante dans Cartes Transport
        cartesTransport.addAll(cartesTransportPosees);
        cartesTransportPosees.removeAll(cartesTransport);

        //attribution de la route
        routes.add(routeChoisie);
        jeu.removeRoutesLibre(routeChoisie);

        //elimination de la route parallèle s'il y en a s'il y a moins de 4 joueurs
        if(jeu.getJoueurs().size()<4){
            if(routeChoisie.getRouteParallele()!=null){
                jeu.removeRoutesLibre(routeChoisie.getRouteParallele());
            }
        }

        //attribution du score
        score+= routeChoisie.getScore();

    }

    private static int indexOfRouteDeCouleurVide(ArrayList<RouteTerrestre> sousRoutes, Map<RouteTerrestre, Integer>sousRouteQuantite,Couleur coul){
        if(sousRoutes.size()!=0){
            for(Route r : sousRoutes){
                if(r.getCouleur() == coul && sousRouteQuantite.get(r) < r.getLongueur()){
                    return sousRoutes.indexOf(r);
                }
            }
        }
        
        return -1;
    }

    private static boolean contientRouteVide(ArrayList<RouteTerrestre> sousRoutes, Map<RouteTerrestre, Integer>sousRouteQuantite){
        if(sousRoutes.size()!=0){
            for(Route r : sousRoutes){
                if(sousRouteQuantite.get(r) < r.getLongueur()){
                    return true;
                }
            }
        }
        
        return false;
    }

    private static ArrayList<Couleur> getCouleurFromSousRoutesVide(ArrayList<RouteTerrestre> sousRoutes, Map<RouteTerrestre, Integer>sousRouteQuantite){
        ArrayList<Couleur> couleurs = new ArrayList<Couleur>();
        for(RouteTerrestre r : sousRoutes){
            if(!couleurs.contains(r.getCouleur()) && sousRouteQuantite.get(r)<r.getLongueur()){
                couleurs.add(r.getCouleur());
            }
        }
        return couleurs;
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

    private boolean contientCarteNonDoubleDeCouleur(List<CarteTransport> liste, Couleur coul){
        for(CarteTransport c : liste){
            if(c.getCouleur() == coul && !c.estDouble()){
                return true;
            }
        }
        return false;
    }

    private int nombreCarteNonDoubleDeCouleur(List<CarteTransport> liste, Couleur coul){
        int compteur = 0;
        for(CarteTransport c : liste){
            if(c.getCouleur() == coul && !c.estDouble()){
                compteur++;
            }
        }
        return compteur;
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
        if(route.getRouteParallele() != null && routes.contains(route.getRouteParallele())){
            estPossible = false;
        }
        else if(route instanceof RoutePaire){
            if(route.getLongueur()> nbPionsWagon){
                estPossible = false;
            }
            else if(route.getLongueur() > nbCombinaisonCarteTransportMin(TypeCarteTransport.WAGON,2)){
                estPossible = false;
            }
        }

        else if(route instanceof RouteTerrestre){
            if(route.getLongueur()> nbPionsWagon){
                estPossible = false;
            }
            else if(route.getCouleur() == Couleur.GRIS && route.getLongueur() > nbCombinaisonCarteTransportMax(TypeCarteTransport.WAGON)){
                estPossible = false;
            }
            else if(route.getCouleur() != Couleur.GRIS && route.getLongueur() > nombreCarteTransportDeCouleur(TypeCarteTransport.WAGON, route.getCouleur())){
                estPossible = false;
            }
        }

        else if(route instanceof RouteMaritime ){
            if(route.getLongueur()> nbPionsBateau){
                estPossible = false;
            }
            else if(route.getCouleur() == Couleur.GRIS && route.getLongueur() > nbCombinaisonCarteTransportMax(TypeCarteTransport.BATEAU)){
                estPossible = false;
            }
            else if(route.getCouleur() != Couleur.GRIS && route.getLongueur() > nombreCarteTransportDeCouleur(TypeCarteTransport.BATEAU, route.getCouleur())){
                estPossible = false;
            }
        }
        return estPossible;
    }

    private int nbCombinaisonCarteTransportMin(TypeCarteTransport type,int min){//TODO vérifier qu'un joker ne compte pas dans plusieurs
        List<Integer> listeCombinaison = Arrays.asList(nombreCarteTransportDeCouleurSansJoker(type, Couleur.BLANC),nombreCarteTransportDeCouleurSansJoker(type, Couleur.JAUNE),nombreCarteTransportDeCouleurSansJoker(type, Couleur.NOIR), nombreCarteTransportDeCouleurSansJoker(type, Couleur.ROUGE), nombreCarteTransportDeCouleurSansJoker(type, Couleur.VERT),nombreCarteTransportDeCouleurSansJoker(type, Couleur.VIOLET));
        int compteur=0;
        int nbJoker = nombreCarteTransport(TypeCarteTransport.JOKER);
        for(int i : listeCombinaison){
            if(i>=min){
                compteur += (int) (i/min);
            }
            else if((i+nbJoker)>=min){
                compteur ++;
                nbJoker-= min-i;
            }
        }
        return compteur;
    }

    private int nbCombinaisonCarteTransportMax(TypeCarteTransport type){
        List<Integer> listeCombinaison = Arrays.asList(nombreCarteTransportDeCouleur(type, Couleur.BLANC),nombreCarteTransportDeCouleur(type, Couleur.JAUNE),nombreCarteTransportDeCouleur(type, Couleur.NOIR), nombreCarteTransportDeCouleur(type, Couleur.ROUGE), nombreCarteTransportDeCouleur(type, Couleur.VERT),nombreCarteTransportDeCouleur(type, Couleur.VIOLET));
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

    private void poserCarteTransportCompatible(Route route){ //TODO route paire à implémenter
        ArrayList<Couleur> couleurValide;

        if(route instanceof RouteTerrestre){
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

        else if(route instanceof RouteMaritime){
            if(route.getCouleur() == Couleur.GRIS){
                couleurValide = combinaisonCouleurCarteTransport(TypeCarteTransport.BATEAU, route.getLongueur());
                for(CarteTransport c : cartesTransport){
                    if(c.getType() == TypeCarteTransport.BATEAU && couleurValide.contains(c.getCouleur())){
                        if(!c.estDouble()){
                            if(nombreCarteTransport(TypeCarteTransport.JOKER)>0 || route.getLongueur()%2!=0 || route.getLongueur()-nombreCarteTransportDeCouleurDouble(TypeCarteTransport.BATEAU, c.getCouleur()) > 0){
                                cartesTransportPosees.add(c);
                            }
                        }
                        else{
                            cartesTransportPosees.add(c);
                        }
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
                        if(!c.estDouble()){
                            if(nombreCarteTransport(TypeCarteTransport.JOKER)>0 || route.getLongueur()%2!=0 || route.getLongueur()-nombreCarteTransportDeCouleurDouble(TypeCarteTransport.BATEAU, c.getCouleur()) > 0){
                                cartesTransportPosees.add(c);
                            }
                        }
                        else{
                            cartesTransportPosees.add(c);
                        }
                    }
                    else if(c.getType() == TypeCarteTransport.JOKER){
                        cartesTransportPosees.add(c);
                    }
                }
                cartesTransport.removeAll(cartesTransportPosees);
            }
        }
        else if(route instanceof RoutePaire){
            int nbJoker = nombreCarteTransport(TypeCarteTransport.JOKER);
            couleurValide = combinaisonCouleurCarteTransport(TypeCarteTransport.WAGON, 2-nbJoker);
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

    private int nombreCarteTransportDeCouleurDouble(TypeCarteTransport type ,Couleur couleur){
        int compteur = 0;
        for(int i = 0; i<cartesTransport.size();i++){
            if(((cartesTransport.get(i).getCouleur() == couleur && cartesTransport.get(i).getType() == type)) || cartesTransport.get(i).getType() == TypeCarteTransport.JOKER){
                if(cartesTransport.get(i).estDouble()){
                    compteur+= 2;
                }
            }
        }
        return compteur;
    }

    private int nombreCarteTransportPoseesDeCouleurDouble(TypeCarteTransport type ,Couleur couleur){
        int compteur = 0;
        for(int i = 0; i<cartesTransportPosees.size();i++){
            if(((cartesTransportPosees.get(i).getCouleur() == couleur && cartesTransportPosees.get(i).getType() == type)) || cartesTransportPosees.get(i).getType() == TypeCarteTransport.JOKER){
                if(cartesTransportPosees.get(i).estDouble()){
                    compteur+= 2;
                }
            }
        }
        return compteur;
    }

    private int nombreCarteTransportDeCouleurNonDouble(TypeCarteTransport type ,Couleur couleur){
        int compteur = 0;
        for(int i = 0; i<cartesTransport.size();i++){
            if(((cartesTransport.get(i).getCouleur() == couleur && cartesTransport.get(i).getType() == type)) || cartesTransport.get(i).getType() == TypeCarteTransport.JOKER){
                if(!cartesTransport.get(i).estDouble()){
                    compteur++;
                }
            }
        }
        return compteur;
    }

    private int nombreCarteTransportDeCouleurSansJoker(TypeCarteTransport type ,Couleur couleur){
        int compteur = 0;
        for(int i = 0; i<cartesTransport.size();i++){
            if((cartesTransport.get(i).getCouleur() == couleur && cartesTransport.get(i).getType() == type)){
                compteur++;
                if(cartesTransport.get(i).estDouble()){
                    compteur++;
                }
            }
        }
        return compteur;
    }

    private int nombreCarteTransportPoseesDeCouleurSansJoker(TypeCarteTransport type ,Couleur couleur){
        int compteur = 0;
        for(int i = 0; i<cartesTransportPosees.size();i++){
            if((cartesTransportPosees.get(i).getCouleur() == couleur && cartesTransportPosees.get(i).getType() == type)){
                compteur++;
                if(cartesTransportPosees.get(i).estDouble()){
                    compteur++;
                }
            }
        }
        return compteur;
    }

    private void echangerPion(String mode){
        String choix;
        boolean fini = false;
        if(mode.equals("PIONS WAGON")){
            int nbWagon = 0;
            log(String.format("ECHANGE PIONS WAGON",toLog()));
            List<String> nombreWagonOption = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25");
            do{
                choix = choisir(
                        "Entrez dans la zone de texte la quantité à échanger. Attention vous perdrez 1 point de score pour chaque pion échangé.",
                        nombreWagonOption,
                        null,
                        false);
                nbWagon = Integer.valueOf(choix);
                if(nbWagon <= this.nbPionsBateau){
                    if(nbWagon <= this.nbPionsWagonEnReserve){
                        this.nbPionsWagon+=nbWagon;
                        this.nbPionsBateau-=nbWagon;
                        this.nbPionsWagonEnReserve-=nbWagon;
                        this.nbPionsBateauEnReserve+=nbWagon;
                        this.score-=nbWagon;
                        log(String.format("Vous venez d'échanger "+nbWagon+" pions Bateau\n contre "+nbWagon+" pions Wagon.",toLog()));
                        fini =true;
                    }
                    else{
                        log(String.format("L'échange est impossible, pas assez de Pions Wagon\n en réserve.",toLog()));
                    }
                }
                else{
                    log(String.format("L'échange est impossible, pas assez de Pions Bateau\n à échanger.",toLog()));
                }
            }while(!fini);
        }
        if(mode.equals("PIONS BATEAU")){
            int nbBateau = 0;
            List<String> nombreBateauOption = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25");
            do{
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
                        fini=true;
                    }
                    else{
                        log(String.format("L'échange est impossible, pas assez de Pions Bateau\n en réserve.",toLog()));
                    }
                }
                else{
                    log(String.format("L'échange est impossible, pas assez de Pions Bateau\n à échanger.",toLog()));
                }
            }while (!fini);
        }
    }


    private void construirePort(String nomPort){
        //capturer le port qui est donné en paramètre et qui est dans la liste des ports, si il est déjà dans la liste c'est qu'il est capturable
        Ville portChoisi = jeu.getPortFromNom(nomPort);
        String choix;
        int nbCarteChoisi = 0;
        int[] repartitionCarteTransport = new int[2];
        //liste des noms cartes que le joueur peut choisir
        ArrayList<String> options = new ArrayList<>();
        CarteTransport carteChoisie;


        //poser cartes transport
        ArrayList<Couleur> listeCouleurCompatible = combinaisonCouleurCarteTransportCompatibleConstructionPort();
        for(CarteTransport c : cartesTransport){
            if(listeCouleurCompatible.contains(c.getCouleur()) && c.getAncre()){
                //poser carte transport
                cartesTransportPosees.add(c);
            }
            else if(c.getType()==TypeCarteTransport.JOKER){
                //poser le joker
                cartesTransportPosees.add(c);
            }
        }
        cartesTransport.removeAll(cartesTransportPosees);

        //joueur choisi les cartes à utiliser
        do {
            options.clear();
            for (CarteTransport c : cartesTransportPosees) {
                options.add(c.getNom());

            }

            choix = choisir("Choisissez les cartes a utiliser pour construire ce port", options, null, false);
            carteChoisie = getCarteTransportPoseFromNom(choix);


            nbCarteChoisi++;
            if(carteChoisie.getType() == TypeCarteTransport.BATEAU){
                repartitionCarteTransport[0]++;
                jeu.defausserBateau(carteChoisie);
            }
            else if(carteChoisie.getType() == TypeCarteTransport.WAGON){
                repartitionCarteTransport[1]++;
                jeu.defausserWagon(carteChoisie);
            }
            else if(carteChoisie.getType() == TypeCarteTransport.JOKER){
                jeu.defausserWagon(carteChoisie);
            }

            cartesTransportPosees.remove(carteChoisie);

            mettreAJourCartePosePort(carteChoisie, repartitionCarteTransport);

        }while(nbCarteChoisi<4);


        //remise des cartes restantes dans Cartes Transport
        cartesTransport.addAll(cartesTransportPosees);
        cartesTransportPosees.removeAll(cartesTransport);

        //attribution du port
        this.ports.add(portChoisi);
        jeu.removePortsLibres(portChoisi);
        log(String.format("Vous venez de capturer le port de "+nomPort+".",toLog()));
    
    }


    private void mettreAJourCartePosePort(CarteTransport derniereCarte,int[] repartitionCarteTransport){
        if(derniereCarte.getType() != TypeCarteTransport.JOKER){
            for(CarteTransport c : cartesTransportPosees){
                if(c.getType() != TypeCarteTransport.JOKER){ //un joker est toujours compatible
                    if(derniereCarte.getCouleur()!=c.getCouleur()){
                        cartesTransport.add(c);
                    }
                    else if(c.getType()==TypeCarteTransport.BATEAU && repartitionCarteTransport[0]==2){
                        cartesTransport.add(c);
                    }
                    else if(c.getType()==TypeCarteTransport.WAGON && repartitionCarteTransport[1]==2){
                        cartesTransport.add(c);
                    }
                }
            }
            cartesTransportPosees.removeAll(cartesTransport);
        }
    }


    private ArrayList<Couleur> combinaisonCouleurCarteTransportCompatibleConstructionPort(){
        List<Couleur> listeCouleur = Arrays.asList(Couleur.BLANC,Couleur.JAUNE,Couleur.NOIR,Couleur.ROUGE,Couleur.VERT, Couleur.VIOLET);
        List<Integer> listeCombinaisonBateau = Arrays.asList(nombreAncreCarteTransportCouleur(TypeCarteTransport.BATEAU, Couleur.BLANC),nombreAncreCarteTransportCouleur(TypeCarteTransport.BATEAU, Couleur.JAUNE),nombreAncreCarteTransportCouleur(TypeCarteTransport.BATEAU, Couleur.NOIR), nombreAncreCarteTransportCouleur(TypeCarteTransport.BATEAU, Couleur.ROUGE), nombreAncreCarteTransportCouleur(TypeCarteTransport.BATEAU, Couleur.VERT),nombreAncreCarteTransportCouleur(TypeCarteTransport.BATEAU, Couleur.VIOLET));
        List<Integer> listeCombinaisonWagon = Arrays.asList(nombreAncreCarteTransportCouleur(TypeCarteTransport.WAGON, Couleur.BLANC),nombreAncreCarteTransportCouleur(TypeCarteTransport.WAGON, Couleur.JAUNE),nombreAncreCarteTransportCouleur(TypeCarteTransport.WAGON, Couleur.NOIR), nombreAncreCarteTransportCouleur(TypeCarteTransport.WAGON, Couleur.ROUGE), nombreAncreCarteTransportCouleur(TypeCarteTransport.WAGON, Couleur.VERT),nombreAncreCarteTransportCouleur(TypeCarteTransport.WAGON, Couleur.VIOLET));

        int nbJoker = nombreCarteTransport(TypeCarteTransport.JOKER);
        ArrayList<Couleur> couleursValides = new ArrayList<>();
        for(Couleur coul : listeCouleur){
            if(listeCombinaisonBateau.get(listeCouleur.indexOf(coul)) >=2 && listeCombinaisonWagon.get(listeCouleur.indexOf(coul)) >=2){
                couleursValides.add(coul);
            }
            else if(listeCombinaisonBateau.get(listeCouleur.indexOf(coul)) + nbJoker >=2 && listeCombinaisonWagon.get(listeCouleur.indexOf(coul)) >=2){
                couleursValides.add(coul);
            }
            else if(listeCombinaisonBateau.get(listeCouleur.indexOf(coul)) >=2 && listeCombinaisonWagon.get(listeCouleur.indexOf(coul)) +nbJoker >=2){
                couleursValides.add(coul);
            }
            else if(nbJoker>1 && listeCombinaisonBateau.get(listeCouleur.indexOf(coul)) + listeCombinaisonWagon.get(listeCouleur.indexOf(coul)) >= 4-nbJoker ){
                couleursValides.add(coul);
            }
        }
        return couleursValides;
    }

    private int nombreAncreCarteTransportCouleur(TypeCarteTransport type, Couleur coul){
        int compteur = 0;
        for(CarteTransport c : cartesTransport){
            if(c.getType()== type && c.getCouleur() == coul && c.getAncre()){
                compteur++;
                if(c.estDouble()){
                    compteur++;
                }
            }
        }
        return compteur;
    }

    private boolean peutConstruirePort(Ville port){

        boolean result = false;
    
        //----------------------------Vérification si le joueur a une route qui mène au port----------------------
        for(Route r : this.routes){
            if(r.getVille1().equals(port) || r.getVille2().equals(port)){
                result = true;
            }
        }
        //--------------------------------------------------------------------------------------------------------
        //------------------------Vérification si le joueur possède les cartes nécessaires------------------------
        if(result == true){
            ArrayList<Couleur> CouleursCompatible = combinaisonCouleurCarteTransportCompatibleConstructionPort();
            if(CouleursCompatible.size()==0){
                result = false;
            }
        }
        
        return result;
    }







    /**
     * Attend une entrée de la part du joueur (au clavier ou sur la websocket) et
     * renvoie le choix du joueur.<Destination> destinations = TestUtils.getDestinations(joueur1);
        destinations.clear();

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
        return destinationEstCompleteRec(d.getVilles(), new ArrayList<String>(), d.getVilles().get(0));
    }

    private boolean destinationEstCompleteRec(ArrayList<String> listeVilles, ArrayList<String> dejaVu, String derniereVille){
        ArrayList<String> predecesseurs = getListePredecesseurs(derniereVille, dejaVu);
        if(dejaVu.containsAll(listeVilles)){
            return true;
        }
        else if(predecesseurs.size()==0){
            return false;
        }
        else{
            boolean result = false;
            int i = 0;
            while(i<predecesseurs.size() && result==false){
                dejaVu.add(predecesseurs.get(i));
                result = result || destinationEstCompleteRec(listeVilles, dejaVu, predecesseurs.get(i));
                i++;
            }
            return result;
        }
    }


    private ArrayList<String> getListePredecesseurs(String ville, ArrayList<String> dejaVu){
        ArrayList<String> predecesseurs = new ArrayList<>();
        if(ville != null){
            for(Route r : routes){
                if(!predecesseurs.contains(r.getVille1().getNom()) && !Objects.equals(r.getVille1().getNom(), ville) && Objects.equals(r.getVille2().getNom(), ville) && !dejaVu.contains(r.getVille1().getNom())){
                    predecesseurs.add(r.getVille1().getNom());
                }
                else if(!predecesseurs.contains(r.getVille2().getNom()) && !Objects.equals(r.getVille2().getNom(), ville) && Objects.equals(r.getVille1().getNom(), ville) && !dejaVu.contains(r.getVille2().getNom())){
                    predecesseurs.add(r.getVille2().getNom());
                }
            }
        }
        
        return predecesseurs;
    }

    /**
     * @param d est un itinéraire (au moins 3 ville) et est Complet
     * @return le nombrer de points qu'il faudra attribuer au joueur
     */
    private int nbPointsAttribuerItinneraire(Destination d){ // innutile dans la partie 1
        boolean estDansLeBonOrdre = itinéraireEstDansLeBonOrdre(d.getVilles(), new ArrayList<String>(), d.getVilles().get(0));
        if(estDansLeBonOrdre){
            return d.getValeurMax();
        }
        else{
            return d.getValeurSimple();
        }
    }

    //fonction innutile dans la Partie 1
    private boolean itinéraireEstDansLeBonOrdre(ArrayList<String> listeVilles, ArrayList<String> dejaVu, String derniereVille){
        ArrayList<String> predecesseurs = getListePredecesseurs(derniereVille, dejaVu);
        if(estDansLeBonOrdre(dejaVu, listeVilles)){
            return true;
        }
        else if(predecesseurs.size()==0){
            return false;
        }
        else{
            boolean result = false;
            int i = 0;
            while(i<predecesseurs.size() && result==false){
                dejaVu.add(predecesseurs.get(i));
                result = result || destinationEstCompleteRec(listeVilles, dejaVu, predecesseurs.get(i));
                i++;
            }
            return result;
        }
    }

    //sous fonction de itinéraireEstDansLeBonOrdre innutile dans la partie 1
    private boolean estDansLeBonOrdre(ArrayList<String> listeEssaie, ArrayList<String> listeReference){
        boolean result=false;
        if(listeReference.containsAll(listeEssaie)){
            result = true;
            int dernierIndice = -1;
            int i;
            for(String v : listeReference){
                i=0;
                while(i<listeEssaie.size()){
                    if(Objects.equals(listeEssaie.get(i), v)){
                        if(dernierIndice<i){
                            dernierIndice = i;
                        }
                        else{
                            result = false;
                        }
                    }
                    i++;
                }
            }
        }
        return result;
    }

    //fonction qui calcule et renvoie le score du joueur
    public int calculerScoreFinal() {
        int scoreFinal = this.score;
        //ajoute score des destinations
        for(Destination d : destinations){
            if(destinationEstComplete(d)){
                scoreFinal+=d.getValeurSimple();
            }
            else{
                scoreFinal-=d.getPenalite();
            }
        }

        //enlève 4 points par ports non construits parmi les 3 ports possibles
        scoreFinal -= (3 - (this.ports.size())) * 4;

        //ajoute le score des ports construits
        //var pour le nombre de points ajouté par chaque port
        int scorePort = 0;
        for(Ville v : ports){
            for(Destination d : destinations){
                if(d.getVilles().contains(v.getNom()) && destinationEstComplete(d)){
                    scorePort++;
                }
            }
            if(scorePort==1){
                scoreFinal+=20;
            }
            else if(scorePort==2){
                scoreFinal+=30;
            }
            else if(scorePort>=3){
                scoreFinal+=40;
            }
        }
        return scoreFinal;
    }

    //renvoie le score du joueur
    public int getScore(){
        return this.score;
    }

    //fonctions get pour fin de partie

    public int getNbPions(){
        return nbPionsWagon + nbPionsBateau;
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
