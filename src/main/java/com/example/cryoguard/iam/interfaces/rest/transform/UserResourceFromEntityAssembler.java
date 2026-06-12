package com.example.cryoguard.iam.interfaces.rest.transform;

import com.example.cryoguard.iam.domain.model.aggregates.User;
import com.example.cryoguard.iam.domain.model.entities.Role;
import com.example.cryoguard.iam.interfaces.acl.LogisticsQueryService;
import com.example.cryoguard.iam.interfaces.rest.resources.UserResource;
import com.example.cryoguard.logistics.interfaces.acl.RouteStatsDto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserResourceFromEntityAssembler {

    private static LogisticsQueryService logisticsQueryService;

    public static void setLogisticsQueryService(LogisticsQueryService service) {
        logisticsQueryService = service;
    }

    public static UserResource toResourceFromEntity(User user) {
        return toResourceFromEntity(user, null);
    }

    public static UserResource toResourceFromEntity(User user, RouteStatsDto stats) {
        // Get the primary role (first one) as lowercase string
        String role = user.getRoles().stream()
                .map(Role::getStringName)
                .findFirst()
                .orElse("operator");

        // Convert status to lowercase string
        String status = user.getStatus().name().toLowerCase();

        // Compute pinBloqueado: true when status is LOCKED
        Boolean pinBloqueado = user.getStatus() == User.UserStatus.LOCKED;

        // Get trip stats from logistics (or zero if unavailable)
        int viajesAsignados = 0;
        int viajesCompletados = 0;
        if (stats != null) {
            viajesAsignados = stats.activeCount();
            viajesCompletados = stats.completedCount();
        } else if (logisticsQueryService != null) {
            try {
                RouteStatsDto statsDto = logisticsQueryService.getStatsByOperator(user.getId());
                viajesAsignados = statsDto.activeCount();
                viajesCompletados = statsDto.completedCount();
            } catch (Exception e) {
                // Fallback to zero if logistics is unavailable
            }
        }

        // Format ultimaActividad: dd/MM/yyyy HH:mm (es-PE), "Sin actividad" when null
        String ultimaActividad = formatUltimaActividad(user.getLastLogin());

        return new UserResource(
            user.getId(),
            user.getUsername(), // name field maps to username
            user.getEmail(),
            role,
            status,
            user.getLastLogin(),
            user.getCreatedAt(),
            user.getTelefono(), // telefono from entity
            pinBloqueado,
            viajesAsignados,
            viajesCompletados,
            ultimaActividad
        );
    }

    private static String formatUltimaActividad(Date lastLogin) {
        if (lastLogin == null) {
            return "Sin actividad";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("es", "PE"));
        return sdf.format(lastLogin);
    }
}