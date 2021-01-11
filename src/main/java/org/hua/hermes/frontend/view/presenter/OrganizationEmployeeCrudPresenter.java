package org.hua.hermes.frontend.view.presenter;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.hua.hermes.frontend.error.exception.InternalServerErrorException;
import org.hua.hermes.frontend.repository.OrganizationEmployeesRepository;
import org.hua.hermes.frontend.view.HasNotifications;
import org.hua.hermes.keycloak.client.exception.ConflictException;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import javax.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

@Log4j2
public class OrganizationEmployeeCrudPresenter {
    protected final OrganizationEmployeesRepository repository;

    @Setter
    protected HasNotifications view;

    public OrganizationEmployeeCrudPresenter(OrganizationEmployeesRepository repository) {
        this.repository = repository;
    }

    public Optional<UserRepresentation> findById(GroupRepresentation organization, String empId) {
        try {
            return execute(() -> repository.findById(organization, empId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<UserRepresentation> findAll(GroupRepresentation organization, int offset, int limit) {
        try {
            return execute(() -> repository.findAll(organization, offset, limit));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public int count(GroupRepresentation organization) {
        try {
            return execute(() -> repository.count(organization));
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean save(GroupRepresentation organization, UserRepresentation userRepresentation) {
        try {
            return execute(() -> repository.save(organization, userRepresentation));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean update(GroupRepresentation organization, UserRepresentation userRepresentation) {
        try {
            return execute(() -> repository.update(organization, userRepresentation));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean delete(GroupRepresentation organization, UserRepresentation userRepresentation) {
        try {
            return execute(() -> repository.delete(organization, userRepresentation));
        } catch (Exception e) {
            return false;
        }
    }

    protected <V> V execute(Callable<V> callable)
    {
        if(view == null) throw new IllegalStateException("View has not been set");
        try{
            return callable.call();
        } catch (NotFoundException ex){
            //If resource is not found then go to the beautiful 404 page
            log.error(ex);
            throw new com.vaadin.flow.router.NotFoundException();
        }
        catch (ConflictException ex){
            //In case of conflict inform the user about it.
            //Unfortunately, keycloak's API doesn't return any info on what caused the conflict.
            log.error(ex);
            consumeError("A conflict has occurred. " + ex.getMessage());
            throw ex;
        } catch (Exception ex){
            //We treat any other exception as a 500 because that is not intended behavior.
            log.error(ex);
            throw new InternalServerErrorException(ex);
        }
    }

    private void consumeError(String message) {
        view.showNotification(message);
    }

}
