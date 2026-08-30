package com.signature.autosave.modules.user.service;

import com.signature.autosave.modules.user.domain.entity.User;
import com.signature.autosave.modules.user.domain.entity.node.UserNode;
import com.signature.autosave.modules.user.domain.repository.UserNodeRepository;
import com.signature.autosave.modules.user.domain.repository.UserRepository;
import com.signature.autosave.modules.user.service.events.UserCreatedEvent;
import com.signature.autosave.modules.user.service.events.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Service
public class UserNodeService {
    private final UserRepository userRepository;
    private final UserNodeRepository userNodeRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void registerUserNode(UserCreatedEvent event){
        User user = userRepository.findByIdAndIsActiveTrue(event.user())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserNode userNode = new UserNode(user.getId(), user.getName());
        userNodeRepository.save(userNode);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteUserNode(UserDeletedEvent event){
        userNodeRepository.findById(event.user())
                .orElseThrow(() -> new RuntimeException("User node not found"));

        userNodeRepository.deleteById(event.user());
    }
}
