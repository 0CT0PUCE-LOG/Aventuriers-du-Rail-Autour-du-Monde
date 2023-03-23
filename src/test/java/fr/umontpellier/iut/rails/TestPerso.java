package fr.umontpellier.iut.rails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.umontpellier.iut.rails.data.CarteTransport;
import fr.umontpellier.iut.rails.data.Couleur;
import fr.umontpellier.iut.rails.data.Destination;
import fr.umontpellier.iut.rails.data.TypeCarteTransport;
import fr.umontpellier.iut.rails.data.Ville;

public class TestPerso {

    private IOJeu jeu;
    private List<CarteTransport> piocheWagon;
    private List<CarteTransport> defausseWagon;
    private List<CarteTransport> piocheBateau;
    private List<CarteTransport> defausseBateau;
    private List<CarteTransport> cartesTransportVisibles;
    private List<Destination> pileDestinations;
    private List<Route> routes;
    private List<Ville> ports;
    private List<Joueur> joueurs;
    private Joueur joueur1;
    private List<CarteTransport> cartesJoueur1;
    private List<Route> routesJoueur1;
    private List<Ville> portsJoueur1;;


    @BeforeAll
    static void staticInit() {
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
    }

    @BeforeEach
    void setUp() {
        // réinitialisation des compteurs
        TestUtils.setAttribute(CarteTransport.class, "compteur", 1);
        TestUtils.setAttribute(Destination.class, "compteur", 1);
        TestUtils.setAttribute(Route.class, "compteur", 1);

        jeu = new IOJeu(new String[] { "Guybrush", "Largo", "LeChuck", "Elaine" });
        PilesCartesTransport pilesWagon = (PilesCartesTransport) TestUtils.getAttribute(jeu,
                "pilesDeCartesWagon");
        piocheWagon = (List<CarteTransport>) TestUtils.getAttribute(pilesWagon, "pilePioche");
        defausseWagon = (List<CarteTransport>) TestUtils.getAttribute(pilesWagon, "pileDefausse");
        PilesCartesTransport pilesBateau = (PilesCartesTransport) TestUtils.getAttribute(jeu,
                "pilesDeCartesBateau");
        piocheBateau = (List<CarteTransport>) TestUtils.getAttribute(pilesBateau, "pilePioche");
        defausseBateau = (List<CarteTransport>) TestUtils.getAttribute(pilesBateau, "pileDefausse");
        cartesTransportVisibles = (List<CarteTransport>) TestUtils.getAttribute(jeu,
                "cartesTransportVisibles");
        pileDestinations = (List<Destination>) TestUtils.getAttribute(jeu, "pileDestinations");
        routes = (List<Route>) TestUtils.getAttribute(jeu, "routesLibres");
        ports = (List<Ville>) TestUtils.getAttribute(jeu, "portsLibres");
        joueurs = (List<Joueur>) TestUtils.getAttribute(jeu, "joueurs");
        joueur1 = joueurs.get(0);
        cartesJoueur1 = (List<CarteTransport>) TestUtils.getAttribute(joueur1, "cartesTransport");
        routesJoueur1 = (List<Route>) TestUtils.getAttribute(joueur1, "routes");
        portsJoueur1 = (List<Ville>) TestUtils.getAttribute(joueur1, "ports");

        // initialisation des pions wagon et bateau du joueur 1
        TestUtils.setAttribute(joueur1, "nbPionsWagon", 20);
        TestUtils.setAttribute(joueur1, "nbPionsWagonEnReserve", 5);
        TestUtils.setAttribute(joueur1, "nbPionsBateau", 40);
        TestUtils.setAttribute(joueur1, "nbPionsBateauEnReserve", 10);
    }

    @Test
    void testDestinationEstCompleteTaille2() {

        List<Destination> destinations = TestUtils.getDestinations(joueur1);
        destinations.clear();

        Ville villeA = new Ville("A", false);
        Ville villeB = new Ville("B", false);
        Ville villeC = new Ville("C",false);
        Ville villeD = new Ville("D", false);

        Destination dest1 = new Destination("A", "D", 5);
        destinations.add(dest1);

        RouteTerrestre route1 = new RouteTerrestre(villeA, villeB, null, 0);
        RouteTerrestre route2 = new RouteTerrestre(villeC, villeA, null, 0);
        RouteTerrestre route3 = new RouteTerrestre(villeC, villeD, null, 0);
        routesJoueur1.add(route1);
        routesJoueur1.add(route2);
        routesJoueur1.add(route3);

        assertTrue(joueur1.destinationEstComplete(dest1));
    }

    @Test
    void testDestinationEstIncompleteTaille2() {

        List<Destination> destinations = TestUtils.getDestinations(joueur1);
        destinations.clear();

        Ville villeA = new Ville("A", false);
        Ville villeB = new Ville("B", false);
        Ville villeC = new Ville("C",false);
        Ville villeD = new Ville("D", false);

        Destination dest1 = new Destination("A", "D", 5);
        destinations.add(dest1);

        RouteTerrestre route1 = new RouteTerrestre(villeA, villeB, null, 0);
        RouteTerrestre route2 = new RouteTerrestre(villeC, villeA, null, 0);
        RouteTerrestre route3 = new RouteTerrestre(villeC, villeB, null, 0);
        routesJoueur1.add(route1);
        routesJoueur1.add(route2);
        routesJoueur1.add(route3);

        assertFalse(joueur1.destinationEstComplete(dest1));
    }

    @Test
    void testDestinationEstCompleteTaille3() {

        List<Destination> destinations = TestUtils.getDestinations(joueur1);
        destinations.clear();

        Ville villeA = new Ville("A", false);
        Ville villeB = new Ville("B", false);
        Ville villeC = new Ville("C",false);
        Ville villeD = new Ville("D", false);
        Ville villeE = new Ville("E", false);
        Ville villeF = new Ville("F", false);
        Ville villeG = new Ville("G", false);

        ArrayList<String> listeVilleDestination = new ArrayList<String>();
        listeVilleDestination.add("A");
        listeVilleDestination.add("E");
        listeVilleDestination.add("G");
        Destination dest1 = new Destination(listeVilleDestination,0,0,0);
        destinations.add(dest1);

        RouteTerrestre route1 = new RouteTerrestre(villeA, villeB, null, 0);
        RouteTerrestre route2 = new RouteTerrestre(villeC, villeA, null, 0);
        RouteTerrestre route3 = new RouteTerrestre(villeC, villeB, null, 0);
        RouteTerrestre route4 = new RouteTerrestre(villeC, villeF, null, 0);
        RouteTerrestre route5 = new RouteTerrestre(villeF, villeE, null, 0);
        RouteTerrestre route6 = new RouteTerrestre(villeG, villeF, null, 0);
        routesJoueur1.add(route1);
        routesJoueur1.add(route2);
        routesJoueur1.add(route3);
        routesJoueur1.add(route4);
        routesJoueur1.add(route5);
        routesJoueur1.add(route6);

        assertTrue(joueur1.destinationEstComplete(dest1));
    }

    @Test
    void testDestinationEstIncompleteTaille3() {

        List<Destination> destinations = TestUtils.getDestinations(joueur1);
        destinations.clear();

        Ville villeA = new Ville("A", false);
        Ville villeB = new Ville("B", false);
        Ville villeC = new Ville("C",false);
        Ville villeD = new Ville("D", false);
        Ville villeE = new Ville("E", false);
        Ville villeF = new Ville("F", false);
        Ville villeG = new Ville("G", false);

        ArrayList<String> listeVilleDestination = new ArrayList<String>();
        listeVilleDestination.add("A");
        listeVilleDestination.add("E");
        listeVilleDestination.add("G");
        Destination dest1 = new Destination(listeVilleDestination,0,0,0);
        destinations.add(dest1);

        RouteTerrestre route1 = new RouteTerrestre(villeA, villeB, null, 0);
        RouteTerrestre route2 = new RouteTerrestre(villeC, villeA, null, 0);
        RouteTerrestre route3 = new RouteTerrestre(villeC, villeB, null, 0);
        RouteTerrestre route4 = new RouteTerrestre(villeC, villeF, null, 0);
        RouteTerrestre route5 = new RouteTerrestre(villeF, villeE, null, 0);
        RouteTerrestre route6 = new RouteTerrestre(villeD, villeF, null, 0);
        routesJoueur1.add(route1);
        routesJoueur1.add(route2);
        routesJoueur1.add(route3);
        routesJoueur1.add(route4);
        routesJoueur1.add(route5);
        routesJoueur1.add(route6);

        assertFalse(joueur1.destinationEstComplete(dest1));
    }

    @Test
    void testCapturerRouteMaritime1() {
        cartesJoueur1.clear();
        CarteTransport c1 = new CarteTransport(TypeCarteTransport.BATEAU, Couleur.VERT, false, true); // C141
        CarteTransport c2 = new CarteTransport(TypeCarteTransport.BATEAU, Couleur.VERT, false, true); // C142
        CarteTransport c3 = new CarteTransport(TypeCarteTransport.BATEAU, Couleur.VERT, true, false); // C143
        CarteTransport c4 = new CarteTransport(TypeCarteTransport.BATEAU, Couleur.VERT, true, false); // C144
        CarteTransport c5 = new CarteTransport(TypeCarteTransport.BATEAU, Couleur.BLANC, true, false); // C145
        cartesJoueur1.addAll(List.of(c1, c2, c3, c4, c5));

        jeu.setInput(
                "R30", // route maritime Buenos Aires - Valparaiso (couleur VERT, longueur 3)
                "C141", // (ok)
                "C142", // pas possible car cela engendre une perte
                "C143"  // double bateau, et aucune carte n'est inutile
        );

        joueur1.jouerTour();

        assertEquals("R30", routesJoueur1.get(0).getNom());
        assertTrue(cartesJoueur1.contains(c2));
        assertEquals(4, TestUtils.getScore(joueur1));
    }

    @Test
    void testCapturerRouteMaritime2() {
        cartesJoueur1.clear();
        CarteTransport c1 = new CarteTransport(TypeCarteTransport.BATEAU, Couleur.ROUGE, false, true); // C141
        CarteTransport c2 = new CarteTransport(TypeCarteTransport.BATEAU, Couleur.ROUGE, false, true); // C142
        CarteTransport c3 = new CarteTransport(TypeCarteTransport.BATEAU, Couleur.ROUGE, true, false); // C143
        CarteTransport c4 = new CarteTransport(TypeCarteTransport.BATEAU, Couleur.ROUGE, true, false); // C144
        CarteTransport c5 = new CarteTransport(TypeCarteTransport.BATEAU, Couleur.BLANC, true, false); // C145
        cartesJoueur1.addAll(List.of(c1, c2, c3, c4, c5));

        jeu.setInput(
                "R36", // route maritime Buenos Aires - Valparaiso (couleur VERT, longueur 3)
                "C141", // (ok)
                "C142", // pas possible car cela engendre une perte
                "C143", // double bateau (ok)
                "C144"  // double bateau, et aucune carte n'est inutile (ok)
        );

        joueur1.jouerTour();

        assertEquals("R36", routesJoueur1.get(0).getNom());
        assertTrue(cartesJoueur1.contains(c2));
        assertEquals(10, TestUtils.getScore(joueur1));
    }
}
