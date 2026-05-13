package com.project.artconnect.dao;

import com.project.artconnect.model.Artwork;
import java.util.List;

/**
 * DAO interface for Artwork entity.
 * Implemented by JdbcArtworkDao (persistence layer).
 */
public interface ArtworkDao {

    List<Artwork> findAll();

    void save(Artwork artwork);

    void update(Artwork artwork);

    void delete(String title);

    List<Artwork> findByArtistName(String artistName);
}