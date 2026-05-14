package com.project.artconnect.service.impl;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import java.util.*;
import java.util.stream.Collectors;

import com.project.artconnect.dao.ArtistDao;

public class InMemoryArtistService implements ArtistService {
    private final Map<String, Artist> artists = new LinkedHashMap<>();
    private final Map<String, Discipline> disciplines = new LinkedHashMap<>();
    private ArtistDao artistDao;

    public InMemoryArtistService() {
        initData();
    }

    public InMemoryArtistService(ArtistDao artistDao) {
        this.artistDao = artistDao;
        initDisciplines();
    }

    private void initDisciplines() {
        addDiscipline("Painting");
        addDiscipline("Sculpture");
        addDiscipline("Photography");
        addDiscipline("Digital Art");
        addDiscipline("Music");
    }

    public void clear() {
        artists.clear();
    }

    private void initData() {
        initDisciplines();

        // Artists
        addArtist("Leonardo Vinci", "Renaissance master and polymath.", 1452, "leo@vincistudio.it", "Florence",
                "Painting", "Sculpture");
        addArtist("Claude Monet", "Founder of French Impressionist painting.", 1840, "claude@monet.fr", "Giverny",
                "Painting");
        addArtist("Ansel Adams", "American landscape photographer and environmentalist.", 1902, "ansel@adams.co",
                "San Francisco", "Photography");
        addArtist("Frida Kahlo", "Mexican painter known for her many portraits and self-portraits.", 1907,
                "frida@kahlo.mx", "Mexico City", "Painting");
        addArtist("Auguste Rodin", "French sculptor, generally considered the founder of modern sculpture.", 1840,
                "auguste@rodin.fr", "Paris", "Sculpture");
    }

    private void addDiscipline(String name) {
        disciplines.put(name, new Discipline(name));
    }

    private void addArtist(String name, String bio, int year, String email, String city, String... disciplineNames) {
        Artist a = new Artist(name, bio, year, email, city);
        for (String dName : disciplineNames) {
            if (disciplines.containsKey(dName)) {
                a.getDisciplines().add(disciplines.get(dName));
            }
        }
        artists.put(name, a);
    }

    @Override
    public List<Artist> getAllArtists() {
        return new ArrayList<>(artists.values());
    }

    @Override
    public Optional<Artist> getArtistByName(String name) {
        return Optional.ofNullable(artists.get(name));
    }

    /**
     * Load an artist from DB into memory WITHOUT saving back to DB.
     */
    public void loadArtist(Artist artist) {
        artists.put(artist.getName(), artist);
    }

    @Override
    public void createArtist(Artist artist) {
        artists.put(artist.getName(), artist);
        if (artistDao != null) {
            artistDao.save(artist);
        }
    }

    @Override
    public void updateArtist(Artist artist) {
        artists.put(artist.getName(), artist);
        if (artistDao != null) {
            artistDao.update(artist);
        }
    }

    @Override
    public void deleteArtist(String name) {
        artists.remove(name);
        if (artistDao != null) {
            artistDao.delete(name);
        }
    }

    @Override
    public List<Discipline> getAllDisciplines() {
        return new ArrayList<>(disciplines.values());
    }

    @Override
    public List<Artist> searchArtists(String query, String disciplineName, String city) {
        return artists.values().stream()
                .filter(a -> query == null || a.getName().toLowerCase().contains(query.toLowerCase()))
                .filter(a -> city == null || city.isEmpty() || a.getCity().equalsIgnoreCase(city))
                .filter(a -> disciplineName == null
                        || a.getDisciplines().stream().anyMatch(d -> d.getName().equals(disciplineName)))
                .collect(Collectors.toList());
    }
}
