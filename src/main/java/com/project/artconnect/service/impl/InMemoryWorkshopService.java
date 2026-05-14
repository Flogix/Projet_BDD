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
        // Basic init if no DAO
    }

    public void clear() {
        workshops.clear();
    }

    public void initData(ArtistService artistService) {
        if (workshopDao != null || artistService == null) return;
        // Dummy data for memory-only mode
    }

    public void loadWorkshop(Workshop w) {
        workshops.put(w.getTitle(), w);
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
        return new ArrayList<>(workshops.values());
    }

    @Override
    public Optional<Workshop> getWorkshopByTitle(String title) {
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
        workshops.put(workshop.getTitle(), workshop);
        if (workshopDao != null) {
            workshopDao.save(workshop);
        }
    }

    @Override
    public void updateWorkshop(Workshop workshop) {
        workshops.put(workshop.getTitle(), workshop);
        if (workshopDao != null) {
            workshopDao.update(workshop);
        }
    }

    @Override
    public void deleteWorkshop(String title) {
        workshops.remove(title);
        if (workshopDao != null) {
            workshopDao.delete(title);
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
