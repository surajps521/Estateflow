package com.realestate.crm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties={
    "spring.datasource.url=jdbc:h2:mem:crm-registration-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "app.bootstrap.admin-email=integration-admin@crm.local",
    "app.bootstrap.admin-password=ChangeMe123!"
})
@Transactional
class RegistrationIntegrationTest {
    @Autowired CrmService service;
    @Autowired UserRepository users;
    @Autowired AgentProfileRepository agentProfiles;
    @Autowired CustomerProfileRepository customerProfiles;
    @Autowired AssignmentRequestRepository assignmentRequests;
    @Autowired LeadRepository leads;
    @Autowired PropertyRepository properties;
    @Autowired MockMvc mockMvc;

    @Test
    void unauthenticatedPropertyListingReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/properties"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void registersSuperAdminOwnerAgentAndCustomerRoles() {
        User superAdmin=service.register(new RegisterRequest("integration-admin2@crm.local","Password123!",Role.SUPER_ADMIN));
        User owner=service.register(new RegisterRequest("integration-owner@crm.local","Password123!",Role.OWNER));
        User agent=service.register(new RegisterRequest("integration-agent@crm.local","Password123!",Role.AGENT));
        User customer=service.registerCustomer(new CustomerRegisterRequest(
            "integration-customer@crm.local","Password123!","Integration Customer","1234567890",
            "Bengaluru",new BigDecimal("5000000"),"Apartment"));

        assertEquals(Role.SUPER_ADMIN,superAdmin.getRole());
        assertEquals(Role.OWNER,owner.getRole());
        assertEquals(Role.AGENT,agent.getRole());
        assertEquals(Role.CUSTOMER,customer.getRole());
        assertEquals(customer,customerProfiles.findByUserId(customer.getId()).orElseThrow().getUser());
        assertEquals(agent,agentProfiles.findByUserId(agent.getId()).orElseThrow().getUser());
        assertNotNull(users.findByEmail(customer.getEmail()));
    }

    @Test
    void customerSelfRegistrationCannotCreateAdminOrAgent() {
        User customer=service.registerCustomer(new CustomerRegisterRequest(
            "forced-customer@crm.local","Password123!","Forced Customer","1234567890",
            "Bengaluru",new BigDecimal("5000000"),"House"));

        assertEquals(Role.CUSTOMER,customer.getRole());
    }

    @Test
    void ownerSelfRegistrationCreatesOwnerRole() {
        User owner=service.registerOwner(new OwnerRegisterRequest(
            "owner-registration@crm.local","Password123!","Owner Name","1234567890",
            "Bengaluru",new BigDecimal("50000000"),"Commercial properties"));

        assertEquals(Role.OWNER,owner.getRole());
        assertEquals("owner-registration@crm.local",owner.getEmail());
    }

    @Test
    void rejectsDuplicateCustomerEmail() {
        CustomerRegisterRequest request=new CustomerRegisterRequest(
            "duplicate-customer@crm.local","Password123!","Duplicate Customer","1234567890",
            "Bengaluru",new BigDecimal("5000000"),"Apartment");
        service.registerCustomer(request);

        assertThrows(ResponseStatusException.class,()->service.registerCustomer(request));
    }

    @Test
    void customerRequestIsVisibleToSelectedAgentBeforeAcceptance() {
        User agent=service.register(new RegisterRequest(
            "meera-request-test@crm.local","Password123!",Role.AGENT));
        User customer=service.registerCustomer(new CustomerRegisterRequest(
            "ram-request-test@crm.local","Password123!","Ram","1234567890",
            "Bengaluru",new BigDecimal("5000000"),"Apartment"));
        Property property=new Property();
        property.title="Request Test Residence";
        property.type="Apartment";
        property.price=new BigDecimal("5000000");
        property.areaSqft=1200;
        property.location="Bengaluru";
        property=properties.save(property);

        AssignmentRequestResponse created=service.createAssignment(
            new AgentRequest(agent.getId(),property.getId(),"Looking for a 2 BHK"),customer.getId());

        assertEquals("PENDING",created.status());
        assertEquals(1,service.pendingAssignments(agent.getId()).size());
        assertEquals(created.id(),service.pendingAssignments(agent.getId()).get(0).id());
        assertEquals(0,leads.findByAgentId(agent.getId()).size());
        assertEquals(1,service.myAssignments(customer.getId()).size());
        assertEquals(created.id(),service.myAssignments(customer.getId()).get(0).id());

        AssignmentRequestResponse accepted=service.updateAssignment(
            created.id(),agent.getId(),AssignmentRequestStatus.ACCEPTED);

        assertEquals("ACCEPTED",accepted.status());
        assertEquals(0,service.pendingAssignments(agent.getId()).size());
        assertEquals(1,leads.findByAgentId(agent.getId()).size());
        assertEquals(customer.getId(),leads.findByAgentId(agent.getId()).get(0).getCustomer().getId());
        assertNotNull(assignmentRequests.findById(created.id()).orElseThrow().getLead());
    }

    @Test
    void ownerPropertySupportsBasePriceAndImage() {
        User owner=service.register(new RegisterRequest(
            "property-owner-test@crm.local","Password123!",Role.OWNER));
        PropertyRequest request=new PropertyRequest(
            "Owner Property","Villa",new BigDecimal("30000000"),
            new BigDecimal("27500000"),2400,"Whitefield","4 BHK",
            "data:image/png;base64,property-photo");

        PropertyResponse created=service.createProperty(request);

        assertEquals(owner.getRole(),Role.OWNER);
        assertEquals(new BigDecimal("27500000"),created.basePrice());
        assertEquals("data:image/png;base64,property-photo",created.imageUrl());
        assertEquals(created.basePrice(),properties.findById(created.id()).orElseThrow().getBasePrice());
    }

    @Test
    void propertyCanBeUpdatedAndArchivedWithoutDeletingItsHistoryRecord() {
        PropertyResponse created=service.createProperty(new PropertyRequest(
            "Editable Property","Apartment",new BigDecimal("8000000"),
            new BigDecimal("7500000"),1100,"Bengaluru","2 BHK",
            "data:image/png;base64,old-photo"));

        PropertyResponse updated=service.updateProperty(created.id(),new PropertyUpdateRequest(
            "Updated Property","Villa",new BigDecimal("12000000"),
            new BigDecimal("11000000"),1800,"Whitefield","3 BHK",
            "data:image/png;base64,new-photo",PropertyStatus.RESERVED));

        assertEquals("Updated Property",updated.title());
        assertEquals("Villa",updated.type());
        assertEquals(new BigDecimal("11000000"),updated.basePrice());
        assertEquals("data:image/png;base64,new-photo",updated.imageUrl());
        assertEquals("RESERVED",updated.status());

        PropertyResponse archived=service.archiveProperty(created.id());

        assertEquals("ARCHIVED",archived.status());
        assertEquals("ARCHIVED",properties.findById(created.id()).orElseThrow().getStatus().name());
    }
}
