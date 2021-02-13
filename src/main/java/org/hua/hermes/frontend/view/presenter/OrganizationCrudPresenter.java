package org.hua.hermes.frontend.view.presenter;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hua.hermes.frontend.repository.OrganizationRepository;
import org.hua.hermes.frontend.view.HasNotifications;
import org.hua.hermes.keycloak.client.exception.ConflictException;
import org.keycloak.representations.idm.GroupRepresentation;

import javax.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

@AllArgsConstructor
@Log4j2
public class OrganizationCrudPresenter
{
    private final OrganizationRepository repository;
    private final HasNotifications view;

    public List<GroupRepresentation> findAll(int offset, int limit) {
        try {
            return execute(() -> repository.findAll(offset, limit));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Optional<GroupRepresentation> findById(String id) throws Exception
    {
        try {
            return execute(() -> repository.findById(id));
        } catch (NotFoundException ex) {
            return Optional.empty();
        }
    }

    public int count() {
        try {
            return execute(() -> repository.count());
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean save(GroupRepresentation entity) {
        try {
            return execute(() -> repository.save(entity));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean update(GroupRepresentation entity) {
        try {
            return execute(() -> repository.update(entity));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean delete(GroupRepresentation entity) {
        try {
            return execute(() -> repository.delete(entity));
        } catch (Exception e) {
            return false;
        }
    }

    protected <V> V execute(Callable<V> callable) throws Exception
    {
        if(view == null) throw new IllegalStateException("View has not been set");
        try{
            return callable.call();
        } catch (ConflictException ex){
            //In case of conflict inform the user about it.
            //Unfortunately, keycloak's API doesn't return any info on what caused the conflict.
            view.showNotification("A conflict has occurred. " + ex.getMessage());
            throw ex;
        } catch (NotFoundException ex) {
            throw ex; //We don't want to show a notification in this case.
        } catch (Exception ex){
            log.error(ex);
            view.showNotification("Something went wrong. Please try executing the same action again.");
            throw ex;
        }
    }

}
