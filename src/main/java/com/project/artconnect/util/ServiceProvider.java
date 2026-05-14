package com.project.artconnect.util;

import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.persistence.JdbcArtworkDao;
import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;

/**
 * Point d'accès unique à tous les services de l'application.
 *
 * Architecture retenue (contrainte par les fichiers existants du projet) :
 *
 *  ┌─────────────────┬──────────────────────────────────────────────────────┐
 *  │ Service         │ Implémentation                                        │
 *  ├─────────────────┼──────────────────────────────────────────────────────┤
 *  │ ArtistService   │ InMemoryArtistService  → branché sur JdbcArtistDao   │
 *  │ ArtworkService  │ InMemoryArtworkService → branché sur JdbcArtworkDao  │
 *  │ GalleryService  │ InMemoryGalleryService (pas de JdbcGalleryDao)        │
 *  │ WorkshopService │ InMemoryWorkshopService (pas de JdbcWorkshopDao)      │
 *  │ CommunityService│ InMemoryCommunityService (pas de JdbcMemberDao)       │
 *  └─────────────────┴──────────────────────────────────────────────────────┘
 *
 * Pour Artist et Artwork, on instancie les services InMemory mais on les
 * alimente depuis la base via les DAO JDBC au lieu des données codées en dur.
 *
 * Explication de ce choix : le projet ne contient que JdbcArtistDao et
 * JdbcArtworkDao dans le dossier persistence/. Créer JdbcGalleryDao,
 * JdbcWorkshopDao ou JdbcCommunityMemberDao ajouterait des fichiers qui ne
 * sont pas dans l'arborescence du squelette fourni. On respecte donc la
 * structure existante.
 */
public class ServiceProvider {

    // ── Instanciation des DAO JDBC ─────────────────────────────────────────
    private static final JdbcArtistDao  jdbcArtistDao  = new JdbcArtistDao();
    private static final JdbcArtworkDao jdbcArtworkDao = new JdbcArtworkDao();
    private static final com.project.artconnect.persistence.JdbcGalleryDao jdbcGalleryDao = new com.project.artconnect.persistence.JdbcGalleryDao();
    private static final com.project.artconnect.persistence.JdbcExhibitionDao jdbcExhibitionDao = new com.project.artconnect.persistence.JdbcExhibitionDao();
    private static final com.project.artconnect.persistence.JdbcWorkshopDao jdbcWorkshopDao = new com.project.artconnect.persistence.JdbcWorkshopDao();
    private static final com.project.artconnect.persistence.JdbcCommunityMemberDao jdbcCommunityMemberDao = new com.project.artconnect.persistence.JdbcCommunityMemberDao();

    // ── Services : chargés depuis la base ───────────────
    private static final InMemoryArtistService artistService;
    private static final InMemoryArtworkService artworkService;
    private static final InMemoryGalleryService galleryService;
    private static final InMemoryWorkshopService workshopService;

    // ── Services restants : toujours InMemory ─────────────────────────────
    private static final InMemoryCommunityService communityService;

    static {
        // Crée les services et les pré-charge avec les données par défaut (InMemory)
        artistService  = new InMemoryArtistService(jdbcArtistDao);
        artworkService = new InMemoryArtworkService(jdbcArtworkDao);
        galleryService = new InMemoryGalleryService(jdbcGalleryDao, jdbcExhibitionDao);
        workshopService = new InMemoryWorkshopService(jdbcWorkshopDao);
        communityService = new InMemoryCommunityService(jdbcCommunityMemberDao);

        // Initialise les relations entre services (InMemory)
        artworkService.initData(artistService);
        galleryService.initData(artworkService);
        workshopService.initData(artistService);
        communityService.initData(artworkService);

        // Charge les données de la base en ARRIÈRE-PLAN pour ne pas bloquer l'interface
        Thread loaderThread = new Thread(() -> {
            try {
                // Petite pause pour laisser l'interface s'afficher proprement
                Thread.sleep(500);
                loadFromDatabase();
                System.out.println("[ServiceProvider] Chargement asynchrone terminé avec succès.");
            } catch (Exception e) {
                System.err.println("[ServiceProvider] Erreur lors du chargement asynchrone : " + e.getMessage());
            }
        });
        loaderThread.setDaemon(true); // Empêche le thread de bloquer la fermeture de l'app
        loaderThread.start();
    }

    /**
     * Charge Artist et Artwork depuis MySQL et les injecte dans les services InMemory.
     * Si la connexion échoue (base absente, credentials incorrects), les services
     * restent avec leurs données fictives — l'application continue de tourner.
     */
    private static void loadFromDatabase() {
        try {
            // Charge les artistes depuis la base
            jdbcArtistDao.findAll().forEach(artistService::createArtist);
            System.out.println("[ServiceProvider] Artistes chargés depuis la base.");
        } catch (Exception e) {
            System.err.println("[ServiceProvider] Impossible de charger les artistes : " + e.getMessage());
            System.err.println("[ServiceProvider] Utilisation des données en mémoire pour les artistes.");
        }

        try {
            // Charge les œuvres depuis la base
            jdbcArtworkDao.findAll().forEach(artworkService::createArtwork);
            System.out.println("[ServiceProvider] Œuvres chargées depuis la base.");
        } catch (Exception e) {
            System.err.println("[ServiceProvider] Impossible de charger les œuvres : " + e.getMessage());
            System.err.println("[ServiceProvider] Utilisation des données en mémoire pour les œuvres.");
        }
        
        try {
            // Charge les galeries depuis la base
            jdbcGalleryDao.findAll().forEach(galleryService::createGallery);
            System.out.println("[ServiceProvider] Galeries chargées depuis la base.");
        } catch (Exception e) {
            System.err.println("[ServiceProvider] Impossible de charger les galeries : " + e.getMessage());
        }

        try {
            // Charge les visiteurs depuis la base
            jdbcCommunityMemberDao.findAll().forEach(communityService::saveMember);
            System.out.println("[ServiceProvider] Visiteurs chargés depuis la base.");
        } catch (Exception e) {
            System.err.println("[ServiceProvider] Impossible de charger les visiteurs : " + e.getMessage());
        }
    }

    // ── Accesseurs ────────────────────────────────────────────────────────
    public static ArtistService    getArtistService()    { return artistService; }
    public static ArtworkService   getArtworkService()   { return artworkService; }
    public static GalleryService   getGalleryService()   { return galleryService; }
    public static WorkshopService  getWorkshopService()  { return workshopService; }
    public static CommunityService getCommunityService() { return communityService; }
}