package com.project.artconnect.service.impl;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import java.util.*;
import java.util.stream.Collectors;

import com.project.artconnect.dao.ArtworkDao;

public class InMemoryArtworkService implements ArtworkService {
    private final Map<String, Artwork> artworks = new LinkedHashMap<>();
    private ArtworkDao artworkDao;

    public InMemoryArtworkService() {
        // We will call initData separately or from constructor if no DAO
    }

    public InMemoryArtworkService(ArtworkDao artworkDao) {
        this.artworkDao = artworkDao;
    }

    public void clear() {
        artworks.clear();
    }

    public void initData(ArtistService artistService) {
        if (artistService == null || artworkDao != null) return;

        addArtwork("Starry Night", 1889, "Oil on canvas", 1000000.0, artistService.getArtistByName("Leonardo Vinci").orElse(null));
        addArtwork("Water Lilies", 1919, "Oil on canvas", 500000.0, artistService.getArtistByName("Claude Monet").orElse(null));
        addArtwork("The Thinker", 1902, "Bronze sculpture", 250000.0, artistService.getArtistByName("Auguste Rodin").orElse(null));
    }

    private void addArtwork(String title, int year, String type, double price, Artist artist) {
        Artwork a = new Artwork(title, year, type, price, artist);
        artworks.put(title, a);
    }

    @Override
    public List<Artwork> getAllArtworks() {
        return new ArrayList<>(artworks.values());
    }

    @Override
    public Optional<Artwork> getArtworkByTitle(String title) {
        return Optional.ofNullable(artworks.get(title));
    }

    @Override
    public List<Artwork> getArtworksByArtist(Artist artist) {
        if (artist == null) return Collections.emptyList();
        return artworks.values().stream()
                .filter(a -> a.getArtist() != null && a.getArtist().getName().equals(artist.getName()))
                .collect(Collectors.toList());
    }

    public void loadArtwork(Artwork artwork) {
        artworks.put(artwork.getTitle(), artwork);
    }

    @Override
    public void createArtwork(Artwork artwork) {
        artworks.put(artwork.getTitle(), artwork);
        if (artworkDao != null) {
            artworkDao.save(artwork);
        }
    }

    @Override
    public void updateArtwork(Artwork artwork) {
        artworks.put(artwork.getTitle(), artwork);
        if (artworkDao != null) {
            artworkDao.update(artwork);
        }
    }

    @Override
    public void deleteArtwork(String title) {
        artworks.remove(title);
        if (artworkDao != null) {
            artworkDao.delete(title);
        }
    }
}
