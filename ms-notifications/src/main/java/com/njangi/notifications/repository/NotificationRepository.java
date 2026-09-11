package com.njangi.notifications.repository;

import com.njangi.notifications.entity.Notification;
import com.njangi.notifications.entity.Notification.StatutNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n WHERE n.destinatreId = :destId ORDER BY n.creeLe DESC")
    List<Notification> findByDestinatreIdOrderByCreeLe(@Param("destId") UUID destinatreId);

    @Query("SELECT n FROM Notification n WHERE n.destinatreId = :destId AND n.lue = false ORDER BY n.creeLe DESC")
    List<Notification> findNonLuesByDestinatreId(@Param("destId") UUID destinatreId);

    @Query("SELECT n FROM Notification n WHERE n.destinatreId = :destId AND n.statut = :statut")
    List<Notification> findByDestinatreIdAndStatut(
            @Param("destId") UUID destinatreId,
            @Param("statut") StatutNotification statut);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.destinatreId = :destId AND n.lue = false")
    long countNonLuesByDestinatreId(@Param("destId") UUID destinatreId);
}
