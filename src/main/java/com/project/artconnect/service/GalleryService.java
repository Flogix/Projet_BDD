package com.project.artconnect.service;

import com.project.artconnect.model.Gallery;
import com.project.artconnect.model.Exhibition;
import java.util.List;
import java.util.Optional;

public interface GalleryService {
    List<Gallery> getAllGalleries();

    Optional<Gallery> getGalleryByName(String name);

    List<Exhibition> getExhibitionsForGallery(Gallery gallery);

    void saveGallery(Gallery gallery);

    void updateGallery(Gallery gallery);

    void deleteGallery(String name);

    void saveExhibition(Exhibition exhibition);

    void updateExhibition(Exhibition exhibition);

    void deleteExhibition(String title);

    List<Exhibition> getAllExhibitions();
}
