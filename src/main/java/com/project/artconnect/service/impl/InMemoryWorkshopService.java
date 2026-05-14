package com.project.artconnect.service.impl;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.service.ArtistService;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryWorkshopService implements WorkshopService {
    private final Map<String, Workshop> workshops = new LinkedHashMap<>();
    private final WorkshopDao workshopDao;

    public InMemoryWorkshopService(WorkshopDao workshopDao) {
        this.workshopDao = workshopDao;
    }

    public InMemoryWorkshopService() {
        this.workshopDao = null;
    }

    public void initData(ArtistService artistService) {
        if (workshopDao == null) {
            addWorkshop("Mastering Oil Painting", LocalDateTime.now().plusDays(5),
                    artistService.getArtistByName("Leonardo Vinci").orElse(null), 150.0, "Intermediate", "Florence Studio");
            addWorkshop("Impressionist Landscapes", LocalDateTime.now().plusDays(10),
                    artistService.getArtistByName("Claude Monet").orElse(null), 120.0, "Beginner", "Giverny Gardens");
            addWorkshop("Sculpting Modernity", LocalDateTime.now().plusDays(15),
                    artistService.getArtistByName("Auguste Rodin").orElse(null), 200.0, "Advanced", "Paris Workshop");
        }
    }

    private void addWorkshop(String title, LocalDateTime date, Artist instructor, double price, String level,
            String location) {
        if (instructor == null)
            return;
        Workshop w = new Workshop(title, date, instructor, price);
        w.setLevel(level);
        w.setLocation(location);
        w.setDurationMinutes(180);
        w.setMaxParticipants(10);
        workshops.put(title, w);
    }

    @Override
    public List<Workshop> getAllWorkshops() {
        if (workshopDao != null) {
            return workshopDao.findAll();
        }
        return new ArrayList<>(workshops.values());
    }

    @Override
    public Optional<Workshop> getWorkshopByTitle(String title) {
        if (workshopDao != null) {
            return workshopDao.findAll().stream().filter(w -> w.getTitle().equals(title)).findFirst();
        }
        return Optional.ofNullable(workshops.get(title));
    }

    @Override
    public void bookWorkshop(Workshop workshop, CommunityMember member) {
        if (workshop == null || member == null)
            return;
        Booking b = new Booking(workshop, member);
        member.addBooking(b);
    }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        if (member == null)
            return Collections.emptyList();
        return member.getBookings();
    }

    @Override
    public void saveWorkshop(Workshop workshop) {
        if (workshopDao != null) {
            workshopDao.save(workshop);
        } else {
            workshops.put(workshop.getTitle(), workshop);
        }
    }

    @Override
    public void updateWorkshop(Workshop workshop) {
        if (workshopDao != null) {
            workshopDao.update(workshop);
        } else {
            workshops.put(workshop.getTitle(), workshop);
        }
    }

    @Override
    public void deleteWorkshop(String title) {
        if (workshopDao != null) {
            workshopDao.delete(title);
        } else {
            workshops.remove(title);
        }
    }

    @Override
    public double calculateMaxRevenue(String title) {
        if (workshopDao != null) {
            return workshopDao.calculateMaxRevenue(title);
        }
        Optional<Workshop> w = getWorkshopByTitle(title);
        return w.map(workshop -> workshop.getPrice() * workshop.getMaxParticipants()).orElse(0.0);
    }
}
