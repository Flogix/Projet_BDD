package com.project.artconnect.util;

import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.persistence.JdbcArtworkDao;
import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;
import com.project.artconnect.model.*;

/**
 * Point d'accès unique à tous les services de l'application.
 */
public class ServiceProvider {

    private static final JdbcArtistDao  jdbcArtistDao  = new JdbcArtistDao();
    private static final JdbcArtworkDao jdbcArtworkDao = new JdbcArtworkDao();
    private static final com.project.artconnect.persistence.JdbcGalleryDao jdbcGalleryDao = new com.project.artconnect.persistence.JdbcGalleryDao();
    private static final com.project.artconnect.persistence.JdbcExhibitionDao jdbcExhibitionDao = new com.project.artconnect.persistence.JdbcExhibitionDao();
    private static final com.project.artconnect.persistence.JdbcWorkshopDao jdbcWorkshopDao = new com.project.artconnect.persistence.JdbcWorkshopDao();
    private static final com.project.artconnect.persistence.JdbcCommunityMemberDao jdbcCommunityMemberDao = new com.project.artconnect.persistence.JdbcCommunityMemberDao();

    private static final InMemoryArtistService artistService;
    private static final InMemoryArtworkService artworkService;
    private static final InMemoryGalleryService galleryService;
    private static final InMemoryWorkshopService workshopService;
    private static final InMemoryCommunityService communityService;

    static {
        // Initialize services with DAOs (this disables their initData dummy data)
        artistService  = new InMemoryArtistService(jdbcArtistDao);
        artworkService = new InMemoryArtworkService(jdbcArtworkDao);
        galleryService = new InMemoryGalleryService(jdbcGalleryDao, jdbcExhibitionDao);
        workshopService = new InMemoryWorkshopService(jdbcWorkshopDao);
        communityService = new InMemoryCommunityService(jdbcCommunityMemberDao);

        // Load DB data synchronously at startup to ensure UI has data
        try {
            loadFromDatabase();
            
            // Initialize cross-service links after loading
            artworkService.initData(artistService);
            galleryService.initData(artworkService);
            workshopService.initData(artistService);
            communityService.initData(artworkService);
            
            System.out.println("[ServiceProvider] Chargement initial terminé.");
        } catch (Exception e) {
            System.err.println("[ServiceProvider] Erreur lors du chargement : " + e.getMessage());
        }
    }

    private static void loadFromDatabase() {
        // Clear maps to avoid duplicates if re-called
        artistService.clear();
        artworkService.clear();

        try {
            // 1. Load Artists
            jdbcArtistDao.findAll().forEach(artistService::loadArtist);
            System.out.println("[ServiceProvider] " + artistService.getAllArtists().size() + " artistes chargés.");
        } catch (Exception e) {
            System.err.println("[ServiceProvider] Erreur artistes : " + e.getMessage());
        }

        try {
            // 2. Load Artworks and link to existing Artists in memory
            jdbcArtworkDao.findAll().forEach(a -> {
                if (a.getArtist() != null) {
                    artistService.getArtistByName(a.getArtist().getName()).ifPresent(a::setArtist);
                }
                artworkService.loadArtwork(a);
            });
            System.out.println("[ServiceProvider] " + artworkService.getAllArtworks().size() + " œuvres chargées.");
        } catch (Exception e) {
            System.err.println("[ServiceProvider] Erreur œuvres : " + e.getMessage());
        }
        
        try {
            // 3. Load Galleries
            jdbcGalleryDao.findAll().forEach(galleryService::loadGallery);
        } catch (Exception e) {
            System.err.println("[ServiceProvider] Erreur galeries : " + e.getMessage());
        }

        try {
            // 4. Load Visitors
            jdbcCommunityMemberDao.findAll().forEach(communityService::loadMember);
        } catch (Exception e) {
            System.err.println("[ServiceProvider] Erreur visiteurs : " + e.getMessage());
        }

        try {
            // 5. Load Exhibitions and link to Galleries
            jdbcExhibitionDao.findAll().forEach(e -> {
                if (e.getGallery() != null) {
                    galleryService.getGalleryByName(e.getGallery().getName()).ifPresent(e::setGallery);
                }
                galleryService.loadExhibition(e);
            });
            System.out.println("[ServiceProvider] " + galleryService.getAllExhibitions().size() + " expositions chargées.");
        } catch (Exception e) {
            System.err.println("[ServiceProvider] Erreur expositions : " + e.getMessage());
        }

        try {
            // 6. Load Workshops and link to Artists
            jdbcWorkshopDao.findAll().forEach(w -> {
                if (w.getInstructor() != null) {
                    artistService.getArtistByName(w.getInstructor().getName()).ifPresent(w::setInstructor);
                }
                workshopService.loadWorkshop(w);
            });
            System.out.println("[ServiceProvider] " + workshopService.getAllWorkshops().size() + " ateliers chargés.");
        } catch (Exception e) {
            System.err.println("[ServiceProvider] Erreur ateliers : " + e.getMessage());
        }
    }

    public static ArtistService    getArtistService()    { return artistService; }
    public static ArtworkService   getArtworkService()   { return artworkService; }
    public static GalleryService   getGalleryService()   { return galleryService; }
    public static WorkshopService  getWorkshopService()  { return workshopService; }
    public static CommunityService getCommunityService() { return communityService; }
}
