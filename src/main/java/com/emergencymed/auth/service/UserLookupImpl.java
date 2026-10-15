package com.emergencymed.auth.service;

import com.emergencymed.common.dto.UserDto;

import com.emergencymed.auth.entity.User;
import com.emergencymed.auth.repository.DeliveryPartnerProfileRepository;
import com.emergencymed.auth.repository.UserRepository;
import com.emergencymed.common.contract.UserLookup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserLookupImpl implements UserLookup {

    private final UserRepository userRepository;
    private final DeliveryPartnerProfileRepository deliveryPartnerProfileRepository;

    public UserLookupImpl(UserRepository userRepository,
                          DeliveryPartnerProfileRepository deliveryPartnerProfileRepository) {
        this.userRepository = userRepository;
        this.deliveryPartnerProfileRepository = deliveryPartnerProfileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findById(Long userId) {
        return userRepository.findById(userId)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> activeDeliveryPartnerIds() {
        return deliveryPartnerProfileRepository.findActiveAndVerifiedDeliveryPartnerUserIds();
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFullName(),
                user.getPhone(),
                user.isActive() ? "ACTIVE" : "INACTIVE"
        );
    }
}
