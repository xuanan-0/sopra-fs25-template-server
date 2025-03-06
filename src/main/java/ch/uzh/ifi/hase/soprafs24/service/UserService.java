package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User getUserById(Long userId) {
    return userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "User with ID " + userId + " was not found"));
  }

  public User createUser(User newUser) {
    if (newUser.getUsername() == null || newUser.getUsername().isEmpty() ||
        newUser.getPassword() == null || newUser.getPassword().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
              "Username and password cannot be empty");
    }
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.ONLINE);
    newUser.setCreationDate(new Date());

    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    User savedUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", savedUser);
    return savedUser;
  }

  public User loginUser(User userInput) {
    User userByUsername = userRepository.findByUsername(userInput.getUsername());

    if (userByUsername == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with username " + 
              userInput.getUsername() + " was not found");
    }
    
    if (!userByUsername.getPassword().equals(userInput.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong password");
    }
    
    userByUsername.setStatus(UserStatus.ONLINE);
    userByUsername = userRepository.save(userByUsername);
    userRepository.flush();
    
    return userByUsername;
  }

  public User updateUser(Long userId, User userInput, String token) {
    User user = getUserById(userId);
    if (user.getToken() == null || token == null || !user.getToken().equals(token)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to modify this profile");
    }
    if (userInput.getUsername() != null && !userInput.getUsername().equals(user.getUsername())) {
      User existingUser = userRepository.findByUsername(userInput.getUsername());
      if (existingUser != null) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Username " + userInput.getUsername() + " is already taken");
      }
      user.setUsername(userInput.getUsername());
    }

    if (userInput.getBirthday() != null) {
      user.setBirthday(userInput.getBirthday());
    }
    
    userRepository.save(user);
    userRepository.flush(); 

    return userRepository.findById(userId).orElseThrow(() ->
    new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User update failed"));
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());


    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, 
              "Username " + userToBeCreated.getUsername() + " is already taken");
    }
  }
}