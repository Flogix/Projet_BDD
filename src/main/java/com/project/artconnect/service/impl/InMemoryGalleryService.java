package com.project.artconnect.service.impl;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.service.ArtworkService;
import java.time.LocalDate;
import java.util.*;

public class InMemoryGalleryService implements GalleryService {
    private final Map<String, Gallery> galleries = new LinkedHashMap<>();
    private final GalleryDao galleryDao;
    private final com.project.artconnect.dao.ExhibitionDao exhibitionDao;

    public InMemoryGalleryService(GalleryDao galleryDao, com.project.artconnect.dao.ExhibitionDao exhibitionDao) {
        this.galleryDao = galleryDao;
        this.exhibitionDao = exhibitionDao;
    }

    public InMemoryGalleryService() {
        this.galleryDao = null;
        this.exhibitionDao = null;
        // Basic init if no DAO
        Gallery louvre = addGallery("Louvre Art House", "Rue de Rivoli, Paris", 4.9);
    }

    public void clear() {
        galleries.clear();
    }

    public void initData(ArtworkService artworkService) {
        if (galleryDao != null || artworkService == null) return;
        // Dummy data only for memory-only mode
    }
    
    public void loadGallery(Gallery g) {
        galleries.put(g.getName(), g);
    }

    public void loadExhibition(Exhibition e) {
        if (e.getGallery() != null) {
            e.getGallery().addExhibition(e);
        }
    }

    private Gallery addGallery(String name, String address, double rating) {
        Gallery g = new Gallery(name, address, rating);
        galleries.put(name, g);
        return g;
    }

    @Override
    public List<Gallery> getAllGalleries() {
        return new ArrayList<>(galleries.values());
    }

    @Override
    public Optional<Gallery> getGalleryByName(String name) {
        return Optional.ofNullable(galleries.get(name));
    }

    @Override
    public List<Exhibition> getExhibitionsForGallery(Gallery gallery) {
        if (gallery == null) return Collections.emptyList();
        return gallery.getExhibitions();
    }

    @Override
    public void saveGallery(Gallery gallery) {
        galleries.put(gallery.getName(), gallery);
        if (galleryDao != null) {
            galleryDao.save(gallery);
        }
    }

    @Override
    public void updateGallery(Gallery gallery) {
        galleries.put(gallery.getName(), gallery);
        if (galleryDao != null) {
            galleryDao.update(gallery);
        }
    }

    @Override
    public void deleteGallery(String name) {
        galleries.remove(name);
        if (galleryDao != null) {
            galleryDao.delete(name);
        }
    }

    @Override
    public void saveExhibition(Exhibition exhibition) {
        if (exhibition.getGallery() != null) {
            exhibition.getGallery().addExhibition(exhibition);
        }
        if (exhibitionDao != null) {
            exhibitionDao.save(exhibition);
        }
    }

    @Override
    public void updateExhibition(Exhibition exhibition) {
        if (exhibitionDao != null) {
            exhibitionDao.update(exhibition);
        }
    }

    @Override
    public void deleteExhibition(String title) {
        for (Gallery g : galleries.values()) {
            g.getExhibitions().removeIf(e -> e.getTitle().equals(title));
        }
        if (exhibitionDao != null) {
            exhibitionDao.delete(title);
        }
    }

    @Override
    public List<Exhibition> getAllExhibitions() {
        List<Exhibition> all = new ArrayList<>();
        for (Gallery g : galleries.values()) {
            all.addAll(g.getExhibitions());
        }
        return all;
    }
}
