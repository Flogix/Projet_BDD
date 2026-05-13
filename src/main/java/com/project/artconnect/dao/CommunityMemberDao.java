package com.project.artconnect.dao;

import com.project.artconnect.model.CommunityMember;
import java.util.List;

/**
 * DAO interface for CommunityMember entity.
 * Maps to the "visiteur" table in the database.
 */
public interface CommunityMemberDao {

    List<CommunityMember> findAll();

    void save(CommunityMember member);

    void update(CommunityMember member);

    void delete(String email);

    List<CommunityMember> findByEmail(String email);
}