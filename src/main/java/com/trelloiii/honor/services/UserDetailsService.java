package com.trelloiii.honor.services;

import com.trelloiii.honor.repository.UserRepository;
import com.trelloiii.honor.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UserRepository userRepository;
    private final Logger logger= LoggerFactory.getLogger(UserDetailsService.class);
    public UserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        logger.info("try to authorize user with username {}",s);
        User user= userRepository.findByUsername(s);
        if(user==null){
            logger.error("user with username {} not found",s);
            throw new UsernameNotFoundException("user not found");
        }
        return user;
    }
}
