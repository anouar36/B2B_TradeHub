package com.logitrack.b2b_tradehub.mapper;

import com.logitrack.b2b_tradehub.dto.client.ClientCreateRequest;
import com.logitrack.b2b_tradehub.dto.client.ClientResponse;
import com.logitrack.b2b_tradehub.dto.client.ClientUpdateRequest;
import com.logitrack.b2b_tradehub.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

// @Mapper annotation marks this interface for MapStruct code generation.
// componentModel="spring" makes the generated mapper a Spring Bean, ready for injection.
@Mapper(componentModel = "spring")
public interface ClientMapper {

    // Singleton instance (useful if not using Spring component model)
    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    // --- Mappings from DTO to Entity ---

    /**
     * Converts ClientCreateRequest DTO to Client Entity.
     * Note: We ignore 'user' field as it must be set manually in the Service layer
     * after the User entity is created.
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalOrders", ignore = true)
    @Mapping(target = "totalSpent", ignore = true)
    @Mapping(target = "tier", ignore = true) // Tier is set to BASIC in the Service logic
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Client toEntity(ClientCreateRequest dto);

    // --- Mappings for Updates ---

    /**
     * Updates an existing Client entity with data from ClientUpdateRequest DTO.
     * @param dto Source DTO with new data.
     * @param client Target entity to be updated.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "totalOrders", ignore = true)
    @Mapping(target = "totalSpent", ignore = true)
    @Mapping(target = "tier", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true) // Handled by @PreUpdate in entity
    @Mapping(target = "orders", ignore = true)
    void updateEntityFromDto(ClientUpdateRequest dto, @MappingTarget Client client);


    // --- Mappings from Entity to Response DTO ---

    /**
     * Converts Client Entity to ClientResponse DTO.
     * MapStruct handles field-to-field copies automatically where names match (e.g., nom, email).
     * @param client The source Client entity.
     * @return The resulting ClientResponse DTO.
     */
    ClientResponse toResponse(Client client);
}