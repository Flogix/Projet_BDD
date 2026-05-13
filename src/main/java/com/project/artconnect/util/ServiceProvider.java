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

    // ── Services : Artist et Artwork chargés depuis la base ───────────────
    private static final InMemoryArtistService artistService;
    private static final InMemoryArtworkService artworkService;

    // ── Services restants : toujours InMemory ─────────────────────────────
    private static final InMemoryGalleryService   galleryService   = new InMemoryGalleryService();
    private static final InMemoryWorkshopService  workshopService  = new InMemoryWorkshopService();
    private static final InMemoryCommunityService communityService = new InMemoryCommunityService();

    static {
        // Crée les services et les pré-charge avec les données de la base
        artistService  = new InMemoryArtistService();
        artworkService = new InMemoryArtworkService();

        // Remplace les données en mémoire par celles issues de la base
        loadFromDatabase();

        // Initialise les services dépendants (Gallery, Workshop, Community)
        // avec les données artistes/œuvres déjà chargées
        artworkService.initData(artistService);
        galleryService.initData(artworkService);
        workshopService.initData(artistService);
        communityService.initData(artworkService);
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
    }

    // ── Accesseurs ────────────────────────────────────────────────────────
    public static ArtistService    getArtistService()    { return artistService; }
    public static ArtworkService   getArtworkService()   { return artworkService; }
    public static GalleryService   getGalleryService()   { return galleryService; }
    public static WorkshopService  getWorkshopService()  { return workshopService; }
    public static CommunityService getCommunityService() { return communityService; }
}