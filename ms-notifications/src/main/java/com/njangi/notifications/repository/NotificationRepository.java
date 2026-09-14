package com.njangi.notifications.repository;

import com.njangi.notifications.entity.Notification;
import com.njangi.notifications.entity.StatutNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByDestinataireIdOrderByCreeLeDesc(UUID destinataireId);

    List<Notification> findByDestinataireIdAndLueFalseOrderByCreeLeDesc(UUID destinataireId);

    List<Notification> findByDestinataireIdAndStatut(UUID destinataireId, StatutNotification statut);

    long countByDestinataireIdAndLueFalse(UUID destinataireId);

    @Modifying
    @Query("UPDATE Notification n SET n.lue = true WHERE n.destinataireId = :destId AND n.lue = false")
    int marquerToutesLues(@Param("destId") UUID destinataireId);
}
